package com.timberr.ar.TBDemo.Utils;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtils {

    public interface photoSavedListener{
        /**
         * callback method when the photo is saved
         */
        void onPhotoSaved(String file_path);
    }

    private static photoSavedListener mPhotoSaved;
    /**
     * Specifies the bearing event listener to which bearing events must be sent.
     * @param photoSavedListener the bearing event listener
     */
    public static void setPhotoSavedListener(FileUtils.photoSavedListener photoSavedListener)
    {
         mPhotoSaved = photoSavedListener;
    }

    private static String TAG="FileUtils";

    public static File storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ");// e.getMessage());
            return null;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
        mPhotoSaved.onPhotoSaved(pictureFile.getAbsolutePath());
        return pictureFile;
    }

    public  static File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        + "/Bilderreise");
        Log.d(TAG, "getOutputMediaFile: "+mediaStorageDir);

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="TB_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        Log.d(TAG, "getOutputMediaFile: "+mediaFile);
        return mediaFile;
    }

    public static File buildVideoFile() {
        File mediaStorageDir=
                    new File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                    + "/Bilderreise");
        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        String mVideoName="TB_"+ timeStamp +".mp4";
        File videoPath =
                new File(
                        mediaStorageDir,  mVideoName);
        Log.d(TAG, "getOutputMediaFile: "+videoPath);
        return videoPath;
    }


}