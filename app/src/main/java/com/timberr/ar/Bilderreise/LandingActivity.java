package com.timberr.ar.Bilderreise;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.timberr.ar.Bilderreise.Utils.AudioHelper;
import com.timberr.ar.Bilderreise.Utils.PermissionHelper;
import com.timberr.ar.Bilderreise.Utils.ScreenUtil;

public class LandingActivity extends AppCompatActivity {
    private AnimationDrawable rabbitIdleAnimation;
    private AnimationDrawable rabbitCtaAnimation;
    private ImageView rabbit;
    private ImageView rotate_bg;
    private AudioHelper audio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        Button startButton=findViewById(R.id.start_button);
        startButton.setAnimation( AnimationUtils.loadAnimation(this, R.anim.bobbing));
        rabbit=findViewById(R.id.rabbit);
        rotate_bg=findViewById(R.id.rotate_bg);
        rotate_bg.getLayoutParams().width=(int)(ScreenUtil.getScreenHeight(this)*2);
        rotate_bg.getLayoutParams().height=(int)(ScreenUtil.getScreenHeight(this)*2);
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
        if (rabbitCtaAnimation!=null)
            rabbitCtaAnimation.stop();
        if (rabbitIdleAnimation!=null)
            rabbitIdleAnimation.stop();
        super.onResume();
        RotateAnimation rotate = new RotateAnimation(
                0, 359,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setInterpolator(new LinearInterpolator());
        rotate.setDuration(17000);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate_bg.startAnimation(rotate);
        audio.playMusic();
        if (!PermissionHelper.hasPermission(this)) {
            PermissionHelper.requestPermissions(this);
        }
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
        if (rabbitCtaAnimation!=null)
            rabbitCtaAnimation.stop();
        if (rabbitIdleAnimation!=null)
            rabbitIdleAnimation.stop();
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