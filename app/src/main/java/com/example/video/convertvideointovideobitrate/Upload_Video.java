package com.example.video.convertvideointovideobitrate;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.example.video.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Upload_Video extends AppCompatActivity {

    private static final int VIDEO_PICK_GALLERY_CODE = 100;
    private static final int VIDEO_PICK_CAMERA_CODE = 200;
    private static final int CAMERA_REQUEST_CODE = 300;
    private static final int STORAGE_REQUEST_CODE = 400;

    private String[] camerapermission;
    private FloatingActionButton create;
    private Button upload;
    private EditText description;
    private VideoView videoView;
    private Uri videoUri;
    private String transcodedVideoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        create = findViewById(R.id.create);
        upload = findViewById(R.id.upload);
        description = findViewById(R.id.Description);
        videoView = findViewById(R.id.vidoeview);

        camerapermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        create.setOnClickListener(v -> showUploadOptions());
        upload.setOnClickListener(v -> {
            if (videoUri != null) {
                uploadVideoToFirebase(videoUri, description.getText().toString().trim());
            } else {
                Toast.makeText(this, "Please select a video first!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUploadOptions() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Upload Video from")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (checkCameraPermission()) {
                            pickFromCamera();
                        } else {
                            requestCameraPermission();
                        }
                    } else if (which == 1) {
                        if (hasStoragePermission()) {
                            pickFromGallery();
                        } else {
                            requestStoragePermission();
                        }
                    }
                })
                .show();
    }



    private void uploadVideoToFirebase(Uri videoUri, String videoDescription) {
        if (videoDescription.isEmpty()) {
            Toast.makeText(this, "Please provide a description!", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Processing Video...");
        progressDialog.setMessage("Converting to HLS format...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String timeStamp = String.valueOf(System.currentTimeMillis());
        String outputDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES) + "/HLS_" + timeStamp; // Temporary storage
        File outputDirFile = new File(outputDir);
        if (!outputDirFile.exists()) outputDirFile.mkdirs();
        String inputPath = getPathFromUri(Upload_Video.this, videoUri);
        Log.d("UploadVideo", "Input Path: " + inputPath);
        if (inputPath == null || inputPath.isEmpty()) {
            Toast.makeText(this, "Failed to retrieve video path!", Toast.LENGTH_SHORT).show();
            return;
        }

        String hlsOutputPath = outputDir + "/output.m3u8";
        String[] command = {
                "-i", inputPath,
                "-codec", "copy",
                "-start_number", "0",
                "-hls_time", "10",
                "-hls_list_size", "0",
                "-f", "hls",
                hlsOutputPath
        };
        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            Toast.makeText(this, "Input file does not exist!", Toast.LENGTH_SHORT).show();
            Log.e("UploadVideo", "Input file not found: " + inputPath);
            return;
        }

        FFmpeg.executeAsync(command, (executionId, returnCode) -> {
            if (returnCode == RETURN_CODE_SUCCESS) {
                progressDialog.setMessage("Uploading to Firebase...");
                uploadHLSToFirebase(outputDir, videoDescription, timeStamp, progressDialog);
            } else if (returnCode == RETURN_CODE_CANCEL) {
                progressDialog.dismiss();
                Toast.makeText(this, "HLS conversion cancelled!", Toast.LENGTH_SHORT).show();
            } else {
                progressDialog.dismiss();
                Toast.makeText(this, "HLS conversion failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadHLSToFirebase(String hlsDir, String videoDescription, String timeStamp, ProgressDialog progressDialog) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("Videos/HLS_" + timeStamp);
        File hlsDirFile = new File(hlsDir);

        AtomicInteger uploadedCount = new AtomicInteger(0);
        List<File> files = new ArrayList<>(List.of(hlsDirFile.listFiles()));
        int totalFiles = files.size();

        for (File file : files) {
            String mimeType = getMimeTypeForHLS(file.getName());
            if (mimeType == null) {
                mimeType = "application/octet-stream"; // Fallback MIME type if detection fails
            }

            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType(mimeType)
                    .build();

            StorageReference fileRef = storageRef.child(file.getName());
            fileRef.putFile(Uri.fromFile(file), metadata)
                    .addOnSuccessListener(taskSnapshot -> {
                        if (uploadedCount.incrementAndGet() == totalFiles) {
                            saveHLSToDatabase(storageRef.child("output.m3u8").getDownloadUrl(), videoDescription, timeStamp, progressDialog);
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Failed to upload HLS files: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private String getMimeTypeForHLS(String fileName) {
        if (fileName.endsWith(".m3u8")) {
            return "application/vnd.apple.mpegurl";
        } else if (fileName.endsWith(".ts")) {
            return "video/MP2T";
        }
        return null;
    }
    /*
    private String getMimeType(Context context, Uri uri) {
        String mimeType = null;

        // Check if it's a valid m3u8 or ts file
        String fileName = getFileName(context, uri);
        if (fileName != null) {
            if (fileName.endsWith(".m3u8")) {
                mimeType = "application/vnd.apple.mpegurl";
            } else if (fileName.endsWith(".ts")) {
                mimeType = "video/MP2T";
            }
        }

        if (mimeType == null) {
            mimeType = super.getMimeType(context, uri); // Fallback to default MIME type detection
        }

        return mimeType;
    }


     */
    @SuppressLint("Range")
    private String getFileName(Context context, Uri uri) {
        String fileName = null;

        // Query the file name from the URI
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver contentResolver = context.getContentResolver();
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } catch (Exception e) {
                Log.e("UploadVideo", "Error retrieving file name: " + e.getMessage());
            }
        }

        return fileName;
    }


    private void saveHLSToDatabase(Task<Uri> downloadUrlTask, String description, String timeStamp, ProgressDialog progressDialog) {
        downloadUrlTask.addOnSuccessListener(m3u8Uri -> {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Videos");
            String videoId = databaseReference.push().getKey();

            HashMap<String, Object> videoData = new HashMap<>();
            videoData.put("videoId", videoId);
            videoData.put("hlsUrl", m3u8Uri.toString());
            videoData.put("description", description);
            videoData.put("timeStamp", timeStamp);

            databaseReference.child(timeStamp).setValue(videoData)
                    .addOnSuccessListener(unused -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Video uploaded successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Failed to save metadata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to get HLS URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private String getMimeType(Context context, Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver contentResolver = context.getContentResolver();
            mimeType = contentResolver.getType(uri);
        }
        if (mimeType == null) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            if (extension != null) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
        }
        return mimeType;
    }
    private String getPathFromUri(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use InputStream for Android Q and above
                try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
                    File tempFile = new File(context.getCacheDir(), "temp_video.mp4");
                    try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                        return tempFile.getAbsolutePath();
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // Fallback for older versions
                //    return RealPathUtil.getRealPath(context, uri);
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }


    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, camerapermission, CAMERA_REQUEST_CODE);
    }

    private boolean hasStoragePermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager();
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
    }

    private void pickFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, VIDEO_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent, VIDEO_PICK_GALLERY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            videoUri = data.getData();
            if (videoUri != null) {
                videoView.setVideoURI(videoUri);
                videoView.start();
            }
        }
    }
}