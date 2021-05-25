package com.timberr.ar.TBDemo;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RouteSelectActivity extends AppCompatActivity {
    private Button back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_select);
        back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    public void onPocketGuideClicked(View view) {
        Intent intent=new Intent(RouteSelectActivity.this,GuideCameraActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.hold);
    }

    public void route1Selected(View view) {
        Intent intent=new Intent(RouteSelectActivity.this,NavigationActivity.class);
        intent.putExtra("gpxFile","complete.gpx");
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.hold);
    }

    public void route2Selected(View view) {
        Intent intent=new Intent(RouteSelectActivity.this,NavigationActivity.class);
        intent.putExtra("gpxFile","east.gpx");
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.hold);
    }

    public void route3Selected(View view) {
        Intent intent=new Intent(RouteSelectActivity.this,NavigationActivity.class);
        intent.putExtra("gpxFile","west.gpx");
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.hold);
    }
}