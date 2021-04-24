package com.timberr.ar.TBDemo;

import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.timberr.ar.TBDemo.Utils.BearingProvider;
import com.timberr.ar.TBDemo.Utils.PermissionHelper;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;

public class NavigationActivity extends AppCompatActivity implements BearingProvider.ChangeEventListener {
    private static final String TAG= NavigationActivity.class.getSimpleName();
    private Location target;
    private float currentDegree = 0f;
    private BearingProvider mBearingProvider;
    private List<TrackPoint> trackpoints;

    private ImageView calib;
    private ImageView nav_compass;
    private TextView distanceTextView;
    private Button calib_complete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        nav_compass=findViewById(R.id.nav_compass);
        distanceTextView =findViewById(R.id.distance);
        calib=(ImageView) findViewById(R.id.calib);
        calib_complete=findViewById(R.id.calib_btn);
        GPXParser gpxParser=new GPXParser();
        Gpx parsedGpx = null;
        try {
            InputStream in = getAssets().open("test.gpx");
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
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mBearingProvider.stop();
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
    }

    @Override
    public void onLocationReached() {
        Toast.makeText(this, "You have reached!", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(NavigationActivity.this,ArtWorkDisplayActivity.class);
                NavigationActivity.this.startActivity(mainIntent);
//                NavigationActivity.this.finish();
            }
        }, 1000);
    }

    @Override
    public void onDistanceChanged(float distance) {
        distance/=1000;
        String d="Disance : "+ String.valueOf(distance).substring(0,4)+ " km";
        Log.d(TAG, "onDistanceChanged: "+d);
        distanceTextView.setVisibility(View.VISIBLE);
        distanceTextView.setText(d);
    }
}