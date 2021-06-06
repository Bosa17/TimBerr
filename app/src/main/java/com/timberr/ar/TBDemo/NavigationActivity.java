package com.timberr.ar.TBDemo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timberr.ar.TBDemo.Utils.DataHelper;
import com.timberr.ar.TBDemo.Utils.LocationService;
import com.timberr.ar.TBDemo.Utils.LocationUtils;
import com.timberr.ar.TBDemo.Utils.BearingProvider;
import com.timberr.ar.TBDemo.Utils.PermissionHelper;

import static com.timberr.ar.TBDemo.Utils.LocationService.EXTRA_GPSFILE;
import static com.timberr.ar.TBDemo.Utils.LocationService.EXTRA_REACHED;


public class NavigationActivity extends AppCompatActivity implements BearingProvider.ChangeEventListener, OnMapReadyCallback {
    private static final String TAG = NavigationActivity.class.getSimpleName();
    //1-complete 2-west 3-east
    public static final String ROUTE_MODE ="mode";
    private float currentDegree = 0f;
    private BearingProvider mBearingProvider;
    private DataHelper dataHelper;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;
    //int to track the selected route
    private int mode=1;
    //Location of starting point
    private  LatLng START_LOC = new LatLng(51.099528, 6.160169);
    //Location of west map point
    private  LatLng MAP_LOC_NE ;
    //Location of west map point
    private  LatLng MAP_LOC_SW ;
    //padding for bounds
    private  LatLng NEAR_NE ;
    private  LatLng NEAR_SW ;
    //drawable for selected route
    private int route_overlay;
    private GroundOverlay groundOverlay;

    private ImageView nav_compass;
    private TextView distanceTextView;
    private Button back;
    private SupportMapFragment mapFragment;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        dataHelper=new DataHelper(this);
        mode=dataHelper.getRouteMode();
        if (mode==1){
            MAP_LOC_NE = new LatLng(51.1430101, 6.3018778);
            MAP_LOC_SW = new LatLng(51.0056267, 5.9585881);
            route_overlay=R.drawable.overlay_complete;
        }
        else if (mode == 2){
            MAP_LOC_NE = new LatLng(51.1390929, 6.1775385);
            MAP_LOC_SW = new LatLng(51.0040943, 5.9606951);
            route_overlay=R.drawable.overlay_west;
        }
        else{
            MAP_LOC_NE = new LatLng(51.1305468, 6.2937138);
            MAP_LOC_SW = new LatLng(51.0394039, 6.1280402);
            route_overlay=R.drawable.overlay_east;
        }
        NEAR_NE =
                new LatLng(MAP_LOC_NE.latitude + 0.111, MAP_LOC_NE.longitude + 0.111);
        NEAR_SW =
                new LatLng(MAP_LOC_SW.latitude - 0.111, MAP_LOC_SW.longitude - 0.111);
        nav_compass = findViewById(R.id.nav_compass);
        distanceTextView = findViewById(R.id.distance);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.nav_view);
        mapFragment.getMapAsync(this);
        mapFragment.getView().setBackgroundColor(Color.parseColor("#ab6680"));
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        myReceiver = new MyReceiver();
        mBearingProvider = new BearingProvider(this);
        mBearingProvider.setChangeEventListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!PermissionHelper.hasLocationPermission(this)) {
            PermissionHelper.requestLocationPermission(this);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mService.requestLocationUpdates();
                }
            }, 3000);
        }

        bindService(new Intent(this, LocationService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBearingProvider.resume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
        mBearingProvider.stop();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        final LatLngBounds bounds= new LatLngBounds(MAP_LOC_SW,MAP_LOC_NE);
        map.setMapType(GoogleMap.MAP_TYPE_NONE);
        map.setMaxZoomPreference(14);
        map.setMinZoomPreference(13);
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.getUiSettings().setTiltGesturesEnabled(false);
        map.getUiSettings().setCompassEnabled(false);
        map.setLatLngBoundsForCameraTarget(bounds);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(START_LOC, 14));
        try {
            map.setMyLocationEnabled(true);
        }
        catch (SecurityException e){}

        // Add a large overlay for covering paddings.
        groundOverlay = map.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.map_bg))
                .anchor(0, 1)
                .positionFromBounds(new LatLngBounds(NEAR_SW,NEAR_NE)));

        // Add the ground overlay
        groundOverlay = map.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(route_overlay))
                .anchor(0, 1)
                .positionFromBounds(bounds));

        // Override the default content description on the view, for accessibility mode.
        // Ideally this string would be localised.
        map.setContentDescription("Google Map with SNA MAP");
    }


    /**
     * Receiver for broadcasts sent by {@link LocationService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location destination = intent.getParcelableExtra(LocationService.EXTRA_DESTINATION);
            Location location = intent.getParcelableExtra(LocationService.EXTRA_LOCATION);
            float distance = intent.getFloatExtra(LocationService.EXTRA_DISTANCE,0.0f);
            boolean reached = intent.getBooleanExtra(EXTRA_REACHED,false);
            if (location != null) {
                mBearingProvider.realign(destination);
                mBearingProvider.onLocationChanged(location);
            }
            onDistanceChanged(distance);
            if (reached)
                onLocationReached();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionHelper.LOCATION_PERMISSION_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService.requestLocationUpdates();
            } else {
                Toast.makeText(this, "Accept all permissions to run this application", Toast.LENGTH_LONG)
                        .show();
                if (!PermissionHelper.shouldShowRequestPermissionRationale(this)) {
                    // Permission denied with checking "Do not ask again".
                    PermissionHelper.launchPermissionSettings(this);
                }
                finish();
            }
        }
    }


    @Override
    public void onBearingChanged(double bearing) {
        // create a rotation animation (reverse turn degree degrees)
        rotateArrow((float) bearing);
    }

    private void rotateArrow(float angle){

        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -angle,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(700);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        nav_compass.startAnimation(ra);
        currentDegree = -angle;
    }

    public void onLocationReached() {
        Toast.makeText(this, "You have reached!", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Activity. */
                Intent intent = new Intent(NavigationActivity.this,ArtWorkDisplayActivity.class);
                NavigationActivity.this.startActivity(intent);
                overridePendingTransition(R.anim.fadein, R.anim.hold);
            }
        }, 1000);
    }


    public void onDistanceChanged(float distance) {
        String d="Next Checkpoint : "+ String.valueOf(distance)+ " m";
        Log.d(TAG, "onDistanceChanged: "+d);
        distanceTextView.setText(d);
    }
}