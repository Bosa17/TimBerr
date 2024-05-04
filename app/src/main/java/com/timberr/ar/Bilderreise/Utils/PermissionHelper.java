package com.timberr.ar.Bilderreise.Utils;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/** Helper to ask permissions. */
public final class PermissionHelper {
    public static final int CAMERA_PERMISSION_CODE = 0;
    public static final int INTERNET_PERMISSION_CODE = 1;
    public static final int LOCATION_PERMISSION_CODE = 2;
    public static final int STORAGE_PERMISSION_CODE = 3;
    public static final int ALL_PERMISSION_CODE = 4;
    public static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    public static final String INTERNET_PERMISSION = Manifest.permission.INTERNET;
    public static final String LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    /** Check to see we have the necessary permissions for this app. */
    public static boolean hasPermission(Activity activity) {
        return (hasCameraPermission(activity) & hasLocationPermission(activity)& hasInternetPermission(activity));
    }

    public static boolean hasCameraPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasInternetPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, INTERNET_PERMISSION)
                == PackageManager.PERMISSION_GRANTED;
    }


    /** Check to see we have the necessary permissions for this app. */
    public static boolean hasLocationPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, LOCATION_PERMISSION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Check to see we have the necessary permissions for this app. */
    public static boolean hasStoragePermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, STORAGE_PERMISSION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Check to see we have the necessary permissions for this app, and ask for them if we don't. */
    public static void requestCameraPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity, new String[] {CAMERA_PERMISSION}, CAMERA_PERMISSION_CODE);
    }

    /** Check to see if we need to show the rationale for this permission. */
    public static boolean shouldShowRequestPermissionRationale(Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, CAMERA_PERMISSION);
    }

    /** Check to see we have the necessary permissions for this app, and ask for them if we don't. */
    public static void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity, new String[] {LOCATION_PERMISSION}, LOCATION_PERMISSION_CODE);
    }
    /** Check to see we have the necessary permissions for this app, and ask for them if we don't. */
    public static void requestInternetPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity, new String[] {INTERNET_PERMISSION}, INTERNET_PERMISSION_CODE);
    }
    /** Check to see we have the necessary permissions for this app, and ask for them if we don't. */
    public static void requestStoragePermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity, new String[] {STORAGE_PERMISSION}, STORAGE_PERMISSION_CODE);
    }
    public static void requestPermissions(Activity activity) {
        ActivityCompat.requestPermissions(
                activity, new String[] {CAMERA_PERMISSION,LOCATION_PERMISSION}, ALL_PERMISSION_CODE);
    }
    /** Launch Application Setting to grant permission. */
    public static void launchPermissionSettings(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }
}
