package com.timberr.ar.TBDemo.Utils;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/** Helper to ask camera permission. */
public final class PermissionHelper {
    private static final int CAMERA_PERMISSION_CODE = 0;
    private static final int INTERNET_PERMISSION_CODE = 1;
    private static final int LOCATION_PERMISSION_CODE = 2;
    private static final int STORAGE_PERMISSION_CODE = 3;
    private static final int ALL_PERMISSION_CODE = 4;
    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private static final String INTERNET_PERMISSION = Manifest.permission.INTERNET;
    private static final String LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    /** Check to see we have the necessary permissions for this app. */
    public static boolean hasPermission(Activity activity) {
        return (hasCameraPermission(activity) & hasLocationPermission(activity)& hasInternetPermission(activity) & hasStoragePermission(activity));
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
                activity, new String[] {CAMERA_PERMISSION,INTERNET_PERMISSION,LOCATION_PERMISSION, STORAGE_PERMISSION}, ALL_PERMISSION_CODE);
    }
    /** Launch Application Setting to grant permission. */
    public static void launchPermissionSettings(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }
}
