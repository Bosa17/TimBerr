package com.timberr.ar.TBDemo;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;

public class VideoPreviewActivity extends AppCompatActivity {
    private VideoView mediaViewer;
    private  String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);
        Toast.makeText(this, "Video Saved", Toast.LENGTH_LONG).show();
        mediaViewer=findViewById(R.id.videoViewer);
        Intent intent = getIntent();
        path= intent.getExtras().getString("path");
        loadVideo(path);
    }
    private void loadVideo(String filePath) {
        try {
            mediaViewer.setVideoPath(filePath);
            mediaViewer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                VideoPreviewActivity.super.onBackPressed();
            }
        }, 2000);
    }
}