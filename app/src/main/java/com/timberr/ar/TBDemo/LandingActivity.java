package com.timberr.ar.TBDemo;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.timberr.ar.TBDemo.Utils.AudioHelper;
import com.timberr.ar.TBDemo.Utils.PermissionHelper;

public class LandingActivity extends AppCompatActivity {
    private AnimationDrawable rabbitIdleAnimation;
    private AnimationDrawable rabbitCtaAnimation;
    private ImageView rabbit;
    private AudioHelper audio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        Button startButton=findViewById(R.id.start_button);
        rabbit=findViewById(R.id.rabbit);

        audio =new AudioHelper(this);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startArtDisplayIntent=new Intent(LandingActivity.this,TutorialActivity.class);
                startActivity(startArtDisplayIntent);
                overridePendingTransition(R.anim.fadein, R.anim.hold);
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
        if (rabbitCtaAnimation!=null)
            rabbitCtaAnimation.stop();
        if (rabbitIdleAnimation!=null)
            rabbitIdleAnimation.stop();

        /* New Handler to start the rabbit idle animation
         */
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                if (rabbitCtaAnimation!=null)
                    rabbitCtaAnimation.stop();
                rabbit.setBackgroundResource(R.drawable.rabbit_idle);

                rabbitIdleAnimation = (AnimationDrawable) rabbit.getBackground();
                rabbitIdleAnimation.start();
            }
        }, 5*1000);

        /* New Handler to start the CTA animation
         * and close the idle animation after 10 seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                if (rabbitIdleAnimation!=null)
                    rabbitIdleAnimation.stop();
                rabbit.setBackgroundResource(R.drawable.rabbit_cta);

                rabbitCtaAnimation = (AnimationDrawable) rabbit.getBackground();
                rabbitCtaAnimation.start();
            }
        }, 15*1000);
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
        overridePendingTransition(R.anim.fadein, R.anim.hold);
    }
}