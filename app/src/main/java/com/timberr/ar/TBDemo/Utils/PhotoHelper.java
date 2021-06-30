package com.timberr.ar.TBDemo.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.PixelCopy;
import android.widget.Toast;

import com.google.ar.sceneform.ArSceneView;
import com.timberr.ar.TBDemo.R;

import java.io.File;

public class PhotoHelper {


    //take picture from the AR Scene View
    public static void takePhoto(Context context,ArSceneView view) {

        // Create a bitmap the size of the scene view.
        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);
        // Create a handler thread to offload the processing of the image.
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        // Make the request to copy.
        PixelCopy.request(view, bitmap, (copyResult) -> {
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    new AppearGallery(context, FileUtils.storeImage(bitmap));

                } catch (Exception e) {
                    Toast toast = Toast.makeText(context,e.toString(),
                            Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                Toast.makeText(context, "Photo Saved", Toast.LENGTH_LONG).show();

            } else {
                Toast toast = Toast.makeText(context,
                        "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
                toast.show();
            }
            handlerThread.quitSafely();
        }, new Handler(handlerThread.getLooper()));
    }
}
