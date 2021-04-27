package com.timberr.ar.TBDemo;

import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
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

public class NavigationActivity extends AppCompatActivity implements BearingProvider.ChangeEventListener {
    private static final String TAG= NavigationActivity.class.getSimpleName();
    private Location target;
    private float currentDegree = 0f;
    private BearingProvider mBearingProvider;
    private List<TrackPoint> trackpoints;
    private ModelRenderable renderable;

    private ImageView calib;
    private ImageView nav_compass;
    private TextView distanceTextView;
    private Button back;
    private SceneView sceneView;
    private Button calib_complete;

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
//        new Scene(sceneView);
        back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        GPXParser gpxParser=new GPXParser();
        Gpx parsedGpx = null;
        String gpxFile = getIntent().getStringExtra("gpxFile");
        try {
            InputStream in = getAssets().open(gpxFile);
            parsedGpx = gpxParser.parse(in);
        } catch (IOException | XmlPullParserException e) {
            // do something with this exception
            e.printStackTrace();
        }
        if (parsedGpx == null) {
            // error parsing track
            Log.d(TAG, "onCreate: nope");
        } else {
            // do something with the parsed track
            // see included example app and tests
            List<Track> tracks = parsedGpx.getTracks();
            for (int i = 0; i < tracks.size(); i++) {
                Track track = tracks.get(i);
                Log.d(TAG, "track " + i + ":");
                List<TrackSegment> segments = track.getTrackSegments();
                for (int j = 0; j < segments.size(); j++) {
                    TrackSegment segment = segments.get(j);
                    Log.d(TAG, "  segment " + j + ":");
                    trackpoints =segment.getTrackPoints();
                    for (TrackPoint trackPoint : segment.getTrackPoints()) {
                        Log.d(TAG, "    point: lat " + trackPoint.getLatitude() + ", lon " + trackPoint.getLongitude());
                    }
                }
            }
        }
        target=new Location("B");
        target.setLatitude(trackpoints.get(0).getLatitude());//50.768094  50.785750
        target.setLongitude(trackpoints.get(0).getLongitude());//6.090876  6.053170
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
                        R.raw.nav_anim)
                .setIsFilamentGltf(false)
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
    protected void onResume()
    {
        super.onResume();
        if(!PermissionHelper.hasLocationPermission(this)){
            PermissionHelper.requestLocationPermission(this);
        }else {
            mBearingProvider.start(target);
        }
        try {
            sceneView.resume();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mBearingProvider.stop();
        try {
            sceneView.pause();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
//        if (!PermissionHelper.hasCameraPermission(this)) {
//            Toast.makeText(this, "Accept all permissions to run this application", Toast.LENGTH_LONG)
//                    .show();
//            if (!PermissionHelper.shouldShowRequestPermissionRationale(this)) {
//                // Permission denied with checking "Do not ask again".
//                PermissionHelper.launchPermissionSettings(this);
//            }
//            finish();
//        }
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
        ra.setDuration(500);

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
                /* Create an Intent that will start the Menu-Activity. */
                Intent intent = new Intent(NavigationActivity.this,ArtWorkDisplayActivity.class);
                NavigationActivity.this.startActivity(intent);
                overridePendingTransition(R.anim.fadein, R.anim.hold);
            }
        }, 1000);
    }

    @Override
    public void onDistanceChanged(float distance) {
        String d="Disance : "+ String.valueOf(distance)+ " m";
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
        node.setRenderable(renderable);
        node.setLocalPosition(new Vector3(0f,0,-3f));
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