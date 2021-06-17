package com.timberr.ar.TBDemo;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.timberr.ar.TBDemo.Utils.DataHelper;


public class RouteSelectActivity extends AppCompatActivity {
    private Button back;
    private DataHelper dataHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_select);
        dataHelper=new DataHelper(this);
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
        dataHelper.setRouteMode(1);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.hold);
    }

    public void route2Selected(View view) {
        Intent intent=new Intent(RouteSelectActivity.this,NavigationActivity.class);
        dataHelper.setRouteMode(2);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.hold);
    }

    public void route3Selected(View view) {
        Intent intent=new Intent(RouteSelectActivity.this,NavigationActivity.class);
        dataHelper.setRouteMode(3);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.hold);
    }
}