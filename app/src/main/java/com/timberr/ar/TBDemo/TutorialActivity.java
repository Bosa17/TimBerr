package com.timberr.ar.TBDemo;

import android.content.Intent;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.timberr.ar.TBDemo.Utils.MediaPlayerHelper;
import com.timberr.ar.TBDemo.Utils.ScreenUtil;

public class TutorialActivity extends AppCompatActivity implements SurfaceHolder.Callback{
    private int width;
    private int height;
    private SurfaceView tutorialSurface;
    private MediaPlayerHelper mediaPlayer = new MediaPlayerHelper("tutorial_vdo.mp4");;
    private Button skip;
    private Button back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        height = ScreenUtil.getScreenHeight(this);
        width = ScreenUtil.getScreenWidth(this);
        setContentView(R.layout.activity_tutorial);
        skip =findViewById(R.id.skip_btn);
        back=findViewById(R.id.back);
        tutorialSurface = findViewById(R.id.tutorial_vdo);
        tutorialSurface.getHolder().addCallback(this);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startArtDisplayIntent=new Intent(TutorialActivity.this,RouteSelectActivity.class);
                startActivity(startArtDisplayIntent);
                overridePendingTransition(R.anim.fadein, R.anim.hold);
            }
        });
        initVideoView();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.release();
        }
    }
    private void initVideoView(){
        ConstraintLayout parentLayout = (ConstraintLayout)findViewById(R.id.parent_layout);
        ConstraintSet set = new ConstraintSet();
        if(width<height)
            height = width;
        else
            width = height;
        set.clone(parentLayout);
        // connect start and end point of views, in this case top of child to top of parent.
        set.connect(tutorialSurface.getId(), ConstraintSet.TOP, parentLayout.getId(), ConstraintSet.TOP, 0);
        set.connect(tutorialSurface.getId(), ConstraintSet.BOTTOM, parentLayout.getId(), ConstraintSet.BOTTOM, 0);
        set.constrainWidth(tutorialSurface.getId(),width);
        set.constrainHeight(tutorialSurface.getId(),height);
//         ... similarly add other constraints
        set.applyTo(parentLayout);
    }
    private void playMedia() {
        mediaPlayer.playMedia(this, tutorialSurface.getHolder().getSurface());
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        playMedia();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}