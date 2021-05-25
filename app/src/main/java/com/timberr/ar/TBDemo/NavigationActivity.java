package com.timberr.ar.TBDemo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.ArraySet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.filament.gltfio.Animator;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.timberr.ar.TBDemo.Utils.LocationService;
import com.timberr.ar.TBDemo.Utils.LocationUtils;
import com.timberr.ar.TBDemo.Utils.BearingProvider;
import com.timberr.ar.TBDemo.Utils.PermissionHelper;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;

import static java.util.concurrent.TimeUnit.SECONDS;

public class NavigationActivity extends AppCompatActivity implements BearingProvider.ChangeEventListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG= NavigationActivity.class.getSimpleName();
    private Location target;
    private int CheckpointIterator = 0;
    private float currentDegree = 0f;
    private BearingProvider mBearingProvider;
    private List<TrackPoint> trackpoints;
    private List<TrackPoint> checkpoints;
    private ModelRenderable renderable;
    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;


    private ImageView calib;
    private ImageView nav_compass;
    private TextView distanceTextView;
    private Button back;
    private SceneView sceneView;
    private Button calib_complete;

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

    private static class AnimationInstance {
        Animator animator;
        Long startTime;
        float duration;
        int index;
        AnimationInstance(Animator animator, int index, Long startTime) {
            this.animator = animator;
            this.startTime = startTime;
            this.duration = animator.getAnimationDuration(index);
            this.index = index;
        }
    }
    private final Set<NavigationActivity.AnimationInstance> animators = new ArraySet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        nav_compass=findViewById(R.id.nav_compass);
        distanceTextView =findViewById(R.id.distance);
        calib=(ImageView) findViewById(R.id.calib);
        calib_complete=findViewById(R.id.calib_btn);
        sceneView=findViewById(R.id.nav_anim_view);
        back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        myReceiver = new MyReceiver();
//        GPXParser gpxParser=new GPXParser();
//        Gpx parsedGpx = null;
//        String gpxFile = getIntent().getStringExtra("gpxFile");
//        try {
//            InputStream in = getAssets().open(gpxFile);
//            parsedGpx = gpxParser.parse(in);
//        } catch (IOException | XmlPullParserException e) {
//            // do something with this exception
//            e.printStackTrace();
//        }
//        if (parsedGpx == null) {
//            // error parsing track
//            Log.d(TAG, "onCreate: nope");
//        } else {
//            // do something with the parsed track
//            // see included example app and tests
//            List<Track> tracks = parsedGpx.getTracks();
//            for (int i = 0; i < tracks.size(); i++) {
//                Track track = tracks.get(i);
//                Log.d(TAG, "track " + i + ":");
//                List<TrackSegment> segments = track.getTrackSegments();
//                for (int j = 0; j < segments.size(); j++) {
//                    TrackSegment segment = segments.get(j);
//                    Log.d(TAG, "  segment " + j + ":");
//                    trackpoints =segment.getTrackPoints();
//                    for (TrackPoint trackPoint : segment.getTrackPoints()) {
//                        if (trackPoint.getName()!=null)
//                            checkpoints.add(trackPoint);
//                        Log.d(TAG, "    point: lat " + trackPoint.getLatitude() + ", lon " + trackPoint.getLongitude()+" "+ trackPoint.getName());
//                    }
//                }
//            }
//        }
//        target=new Location("B");
//        target.setLatitude(trackpoints.get(CheckpointIterator).getLatitude());//50.768094  50.785750
//        target.setLongitude(trackpoints.get(CheckpointIterator).getLongitude() );//6.090876  6.053170
        mBearingProvider = new BearingProvider(this);
        mBearingProvider.setChangeEventListener(this);
        calib_complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNavigation();
            }
        });
        WeakReference<NavigationActivity> weakActivity = new WeakReference<>(this);
        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        ModelRenderable.builder()
                .setSource(
                        this,
                        R.raw.animationroute)
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            NavigationActivity activity = weakActivity.get();
                            if (activity != null) {
                                activity.renderable = modelRenderable;
                                activity.placeRenderable();
                            }
                        })
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        if(!PermissionHelper.hasLocationPermission(this)){
            PermissionHelper.requestLocationPermission(this);
        } else {
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    mService.requestLocationUpdates();
                }
            }, 1000);
        }
//        mBearingProvider.start(target);
        bindService(new Intent(this, LocationService.class).putExtra("gpxFile",getIntent().getStringExtra("gpxFile")), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mBearingProvider.resume();
        try {
            sceneView.resume();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause()
    {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
        mBearingProvider.stop();
        try {
            sceneView.pause();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }


    @Override
    public void onBackPressed() {
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        super.onBackPressed();
    }


    /**
     * Receiver for broadcasts sent by {@link LocationService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationService.EXTRA_LOCATION);
            if (location != null) {
//                Toast.makeText(NavigationActivity.this, LocationUtils.getLocationText(location),
//                        Toast.LENGTH_SHORT).show();
                mBearingProvider.onLocationChanged(location);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s.equals(LocationUtils.KEY_REQUESTING_LOCATION_UPDATES)) {

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
//        // create a rotation animation (reverse turn degree degrees)

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

    private  void showNavigation(){
        calib_complete.setVisibility(View.GONE);
        calib.setVisibility(View.GONE);
        distanceTextView.setVisibility(View.VISIBLE);
        nav_compass.setVisibility(View.VISIBLE);
        sceneView.setVisibility(View.VISIBLE);
    }

    @Override
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

    @Override
    public void onDistanceChanged(float distance) {
        String d="Distance : "+ String.valueOf(distance)+ " m";
        Log.d(TAG, "onDistanceChanged: "+d);
        distanceTextView.setVisibility(View.VISIBLE);
        distanceTextView.setText(d);
    }

    private void placeRenderable(){
        if (renderable == null) {
            return;
        }
        Log.d(TAG, "placeRenderable: "+renderable);
        Node node= new Node();
        node.setLocalScale(new Vector3(1.0f,1.0f,1.0f));
        node.setLocalPosition(new Vector3(-0.06f,0.07f,-0.3f));
        node.setLocalRotation(new Quaternion(new Vector3(1,0,0),90));
        node.setRenderable(renderable);
        sceneView.getScene().addChild(node);
        FilamentAsset filamentAsset = node.getRenderableInstance().getFilamentAsset();
        if (filamentAsset.getAnimator().getAnimationCount() > 0) {
            animators.add(new NavigationActivity.AnimationInstance(filamentAsset.getAnimator(), 0, System.nanoTime()));
        }
        sceneView
                .getScene()
                .addOnUpdateListener(
                        frameTime -> {
                            Long time = System.nanoTime();
                            for (NavigationActivity.AnimationInstance animator : animators) {
                                animator.animator.applyAnimation(
                                        animator.index,
                                        (float) ((time - animator.startTime) / (double) SECONDS.toNanos(1))
                                                % animator.duration);
                                animator.animator.updateBoneMatrices();
                            }
                        });
    }
}