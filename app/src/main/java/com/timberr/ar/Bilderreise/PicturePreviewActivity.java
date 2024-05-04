package com.timberr.ar.Bilderreise;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;


public class PicturePreviewActivity extends AppCompatActivity {
    private ImageView mediaViewer;
    private  String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_preview);
        Toast.makeText(this, "Photo Saved", Toast.LENGTH_LONG).show();
        mediaViewer=findViewById(R.id.media_viewer);
        Intent intent = getIntent();
        path= intent.getExtras().getString("path");
        Log.d("lol", "createContentView: "+path);
        loadBitmap(path);
    }
    private void loadBitmap(String filePath) {
        Bitmap bmp = BitmapFactory.decodeFile(filePath);
        Log.d("lol", "loadBitmap: "+bmp);
        mediaViewer.setImageBitmap(bmp);
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                PicturePreviewActivity.super.onBackPressed();
                overridePendingTransition(R.anim.fadein, R.anim.hold);
            }
        }, 2000);
    }
}