package com.timberr.ar.TBDemo.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.SceneView;

public class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

    private ArSceneView sceneView;
    private int width;
    private int height;
    private String TAG=CustomSurfaceView.class.getSimpleName();
    public CustomSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    public CustomSurfaceView(Context context, ArSceneView sceneView,int width, int height) {
        super(context);
        getHolder().addCallback(this);
        this.sceneView=sceneView;
        this.height=height;
        this.width=width;
    }

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceCreated: ");
        surfaceHolder.setFixedSize(width,height);
        sceneView.startMirroringToSurface(surfaceHolder.getSurface(),0,0,width,height);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d(TAG, "surfaceChanged: "+surfaceHolder.getSurface());

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceDestroyed: ");
        sceneView.startMirroringToSurface(surfaceHolder.getSurface(),0,0,width,height);
    }
}
