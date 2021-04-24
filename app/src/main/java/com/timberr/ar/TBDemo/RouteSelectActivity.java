package com.timberr.ar.TBDemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class RouteSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_select);
    }


    public void onPocketGuideClicked(View view) {
        Intent startArtDisplayIntent=new Intent(RouteSelectActivity.this,GuideCameraActivity.class);
        startActivity(startArtDisplayIntent);
    }
}