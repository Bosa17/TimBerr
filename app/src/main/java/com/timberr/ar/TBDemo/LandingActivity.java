package com.timberr.ar.TBDemo;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.timberr.ar.TBDemo.Utils.AudioHelper;
import com.timberr.ar.TBDemo.Utils.PermissionHelper;

import java.io.IOException;

public class LandingActivity extends AppCompatActivity {

    private AudioHelper audio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        Button startButton=findViewById(R.id.start_button);
        audio =new AudioHelper(this);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startArtDisplayIntent=new Intent(LandingActivity.this,RouteSelectActivity.class);
                startActivity(startArtDisplayIntent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audio.stopPlaying();
    }
    @Override
    protected void onResume() {
        super.onResume();
        audio.playMusic();
        if (!PermissionHelper.hasPermission(this)) {
            PermissionHelper.requestPermissions(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        audio.stopPlaying();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (!PermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Accept all permissions to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!PermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                PermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }



    public void onInfoClicked(View view) {
        Intent startArtDisplayIntent=new Intent(LandingActivity.this,InfoActivity.class);
        startActivity(startArtDisplayIntent);
    }
}