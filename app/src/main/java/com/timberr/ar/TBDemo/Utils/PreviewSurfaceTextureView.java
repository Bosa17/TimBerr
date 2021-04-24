package com.timberr.ar.TBDemo.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.glcanvas.RawTexture;
import com.chillingvan.canvasgl.glview.texture.GLSurfaceTextureProducerView;
import com.chillingvan.canvasgl.glview.texture.GLTexture;
import com.chillingvan.canvasgl.glview.texture.gles.GLThread;
import com.chillingvan.canvasgl.textureFilter.BasicTextureFilter;
import com.chillingvan.canvasgl.textureFilter.RGBFilter;
import com.chillingvan.canvasgl.textureFilter.TextureFilter;
import com.timberr.ar.TBDemo.R;

import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

public class PreviewSurfaceTextureView extends GLSurfaceTextureProducerView {

    private int width;
    private int height;
    private Bitmap bmp2;
    private TextureFilter textureFilter = new BasicTextureFilter();

    public PreviewSurfaceTextureView(Context context) {
        super(context);
    }

    public PreviewSurfaceTextureView(Context context,int width, int height) {
        super(context);
        this.width=width;
        this.height=height;
        bmp2= BitmapFactory.decodeResource(getResources(),
                R.drawable.photo_frame);
        bmp2= getResizedBitmap(bmp2,width,height);
    }

    public PreviewSurfaceTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreviewSurfaceTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();
    }


    @Override
    protected void onGLDraw(ICanvasGL canvas, GLTexture producedGLTexture, @Nullable GLTexture outsideGLTexture) {
//        super.onGLDraw(canvas, producedGLTexture, outsideGLTexture);
        Log.d("lol", "onGLDraw: ");
        RawTexture producedRawTexture = producedGLTexture.getRawTexture();
        SurfaceTexture producedSurfaceTexture = producedGLTexture.getSurfaceTexture();
        producedRawTexture.setIsFlippedVertically(true);
        canvas.drawSurfaceTexture(producedRawTexture, producedSurfaceTexture, 0, 0, width, height);
        canvas.drawBitmap(bmp2, 0,0);
    }

    private static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

}
