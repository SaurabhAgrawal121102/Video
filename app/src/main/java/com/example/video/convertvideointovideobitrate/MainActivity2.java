package com.example.video.convertvideointovideobitrate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.video.R;

public class MainActivity2 extends AppCompatActivity {

    Button uploadVideo,showvideo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main2);


        uploadVideo = (Button)findViewById(R.id.uploadVideo);
        showvideo = (Button)findViewById(R.id.showvideo);

        uploadVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity2.this, Upload_Video.class));
            }
        });
        showvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity2.this, video_play_recyclerview.class));
            }
        });
    }
}