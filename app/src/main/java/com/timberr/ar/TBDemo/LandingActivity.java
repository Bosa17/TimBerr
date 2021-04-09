package com.timberr.ar.TBDemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.timberr.ar.TBDemo.Utils.PermissionHelper;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        Button startButton=findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startArtDisplayIntent=new Intent(LandingActivity.this,ArtWorkDisplayActivity.class);
                startActivity(startArtDisplayIntent);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!PermissionHelper.hasPermission(this)) {
            PermissionHelper.requestPermissions(this);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
//        if (!PermissionHelper.hasCameraPermission(this)) {
//            Toast.makeText(this, "Accept all permissions to run this application", Toast.LENGTH_LONG)
//                    .show();
//            if (!PermissionHelper.shouldShowRequestPermissionRationale(this)) {
//                // Permission denied with checking "Do not ask again".
//                PermissionHelper.launchPermissionSettings(this);
//            }
//            finish();
//        }
    }
}