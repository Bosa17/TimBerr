package com.timberr.ar.TBDemo;

import android.content.Intent;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.VideoView;

public class VideoPreviewActivity extends AppCompatActivity {
    private VideoView mediaViewer;
    private  String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);
        Toast.makeText(this, getText(R.string.artworkdisplay_vdo), Toast.LENGTH_LONG).show();
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