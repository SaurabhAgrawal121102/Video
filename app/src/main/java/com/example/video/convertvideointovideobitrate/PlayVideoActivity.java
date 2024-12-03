package com.example.video.convertvideointovideobitrate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.ui.PlayerView;

import com.example.video.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PlayVideoActivity extends AppCompatActivity {

    private String timestamp, videourl;
    private PlayerView playerView;
    private ExoPlayer exoPlayer;
    private String mimeType = "application/x-mpegurl"; // Default for HLS (.m3u8)
    private ImageView exo_quality;
    public  boolean isTrackSelectionDialogVisible;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        playerView = findViewById(R.id.player_view);
        exo_quality = playerView.findViewById(R.id.exo_quality);
        // Get the video URL from the intent (ensure it's the HTTP URL, not the GS URL)
        Intent intent = getIntent();
         timestamp = intent.getStringExtra("timestamp"); // Make sure this is the HTTP URL
         videourl = intent.getStringExtra("videourl"); // Make sure this is the HTTP URL
       //  videourl = intent.getStringExtra("videourl"); // Make sure this is the HTTP URL

        /*
        //  String videourl = " https://firebasestorage.googleapis.com/v0/b/video-6900d.appspot.com/o/Videos%2FHLS_123456789%2Foutput.m3u8?alt=media&token=xyz";
        // Check if the videourl is null or empty
        if (videourl == null || videourl.isEmpty()) {
            Toast.makeText(this, "Invalid or empty video URL", Toast.LENGTH_SHORT).show();
            return;
        }

        // Log the video URL for debugging
        Log.d("VideoURL", "Video URL: " + videourl);

        // Initialize ExoPlayer


        String testurl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8";
        // Create an ExoPlayer instance
        exoPlayer = new SimpleExoPlayer.Builder(this).build();
        exoPlayer.addAnalyticsListener(new EventLogger());
        // Set the player to PlayerView
        playerView.setPlayer(exoPlayer);

        // Create a media item from the HTTP URL
        MediaItem mediaItem = MediaItem.fromUri(testurl);
        // Use an HLS media source since the video URL is an HLS stream


        // Prepare and play the video
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);

        // Set an error listener to catch playback errors
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                Log.e("ExoPlayerError", "Error: " + error.getMessage());
                Toast.makeText(PlayVideoActivity.this, "Playback Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Player.Listener.super.onPlayerStateChanged(playWhenReady, playbackState);
                Log.d("ExoPlayerState", "State: " + playbackState);
            }
        });

        //  playHLSVideo(timestamp);


        // Optional: Add a track selection button if needed
        exo_quality.setOnClickListener(v -> {
            if (!isTrackSelectionDialogVisible && TrackSelectionDialog.willHaveContent(exoPlayer)) {
                isTrackSelectionDialogVisible = true;
                TrackSelectionDialog trackSelectionDialog = TrackSelectionDialog.createForPlayer(
                        exoPlayer,
                        dismissedDialog -> isTrackSelectionDialogVisible = false
                );
                trackSelectionDialog.show(getSupportFragmentManager(), null);
            }
        });



         */

        playHLSVideo(videourl);
        /*
          StorageReference storageRef = FirebaseStorage.getInstance().getReference("Videos/HLS_" + timestamp + "/output.m3u8");
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Log.d("VideoURL", "HLS URL: " + uri.toString());

        }).addOnFailureListener(e -> {
            Log.e("VideoURL", "Error retrieving video URL: " + e.getMessage());
            Toast.makeText(this, "Failed to fetch video URL.", Toast.LENGTH_SHORT).show();
        });
         */



    }

    @OptIn(markerClass = UnstableApi.class)
    private void playHLSVideo(String videoUrl) {
        if (videoUrl == null || videoUrl.isEmpty()) {
            Toast.makeText(this, "Invalid video URL", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize ExoPlayer
        exoPlayer = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(exoPlayer);

        // Configure HLS Media Source
        DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();

        HlsMediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(videoUrl));

        // Prepare and play
        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);

        // Add error listener
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                Log.e("ExoPlayerError", "Error: " + error.getMessage());
                Toast.makeText(PlayVideoActivity.this, "Playback Error: " + error.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release the ExoPlayer when the activity is destroyed
        if (exoPlayer != null) {
            exoPlayer.release();
        }
    }


    @OptIn(markerClass = UnstableApi.class)
    private MediaSource createMediaSource(String videoUrl, String mimeType) {
        // Create a CacheDataSource.Factory
        CacheDataSource.Factory cacheDataSourceFactory = new CacheDataSource.Factory()
                .setUpstreamDataSourceFactory(new DefaultDataSource.Factory(this))
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);

        MediaSource mediaSource;

        if ("application/x-mpegurl".equals(mimeType) || videoUrl.endsWith(".m3u8")) {
            // Use HlsMediaSource for HLS streams (.m3u8 or application/x-mpegurl)
            mediaSource = new HlsMediaSource.Factory(cacheDataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(videoUrl));
        } else if ("video/mp4".equals(mimeType) || "video/hevc".equals(mimeType)) {
            // Use ProgressiveMediaSource for MP4 and HEVC videos
            mediaSource = new ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(videoUrl));
        } else {
            // Handle unsupported MIME types
            throw new IllegalArgumentException("Unsupported MIME type: " + mimeType);
        }

        return mediaSource;
    }
}