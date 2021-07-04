package com.timberr.ar.TBDemo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.timberr.ar.TBDemo.Utils.DataHelper;
import com.timberr.ar.TBDemo.Utils.LocationService;
import com.timberr.ar.TBDemo.Utils.PermissionHelper;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;

import static com.timberr.ar.TBDemo.Utils.LocationService.EXTRA_REACHED;


public class NavigationActivity extends AppCompatActivity implements  OnMapReadyCallback {
    private static final String TAG = NavigationActivity.class.getSimpleName();
    //1-complete 2-west 3-east
    public static final String ROUTE_MODE ="mode";
    private float currentDegree = 0f;
    private boolean reached=false;
    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;
    private DataHelper dataHelper;
    // A reference to the service used to get location updates.
    private LocationService mService = null;

    private GoogleMap map;
    //int to track the selected route
    private Location loc;
    private int mode=1;
    // Tracks the bound state of the service.
    private boolean mBound = false;
    //Location of west map point
    private  LatLng MAP_LOC_NE ;
    //Location of west map point
    private  LatLng MAP_LOC_SW ;
    //Location of starting point
    private  LatLng START_LOC = new LatLng(51.099546, 6.160174);
    //padding for bounds
    private  LatLng NEAR_NE ;
    private  LatLng NEAR_SW ;
    String gpxFile="";
    private int color=Color.RED;

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
            gpxFile="complete.gpx";
            color= Color.RED;
        }
        else if (mode == 2){
            MAP_LOC_NE = new LatLng(51.1390929, 6.1775385);
            MAP_LOC_SW = new LatLng(51.0040943, 5.9606951);
            gpxFile="west.gpx";
            color= Color.YELLOW;
        }
        else{
            MAP_LOC_NE = new LatLng(51.1305468, 6.2937138);
            MAP_LOC_SW = new LatLng(51.0394039, 6.1280402);
            gpxFile="east.gpx";
            color= Color.GREEN;
        }
        NEAR_NE =
                new LatLng(MAP_LOC_NE.latitude + 0.111, MAP_LOC_NE.longitude + 0.111);
        NEAR_SW =
                new LatLng(MAP_LOC_SW.latitude - 0.111, MAP_LOC_SW.longitude - 0.111);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.nav_view);
        mapFragment.getMapAsync(this);
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        myReceiver = new MyReceiver();

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
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
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
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map=googleMap;
        final LatLngBounds bounds= new LatLngBounds(MAP_LOC_SW,MAP_LOC_NE);
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.style));
        map.setMaxZoomPreference(30);
        map.setMinZoomPreference(12);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setTiltGesturesEnabled(false);
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        ArrayList<LatLng> points = null;
        PolylineOptions polyLineOptions = null;

        // traversing through routes
        for (int i = 0; i < 3; i++) {
            points = new ArrayList<LatLng>();
            polyLineOptions = new PolylineOptions();
            GPXParser gpxParser = new GPXParser();
            Gpx parsedGpx = null;
            polyLineOptions.width(17);
            polyLineOptions.color(color);
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
                for (int k = 0; k < tracks.size(); k++) {
                    Track track = tracks.get(k);
                    List<TrackSegment> segments = track.getTrackSegments();
                    for (int j = 0; j < segments.size(); j++) {
                        TrackSegment segment = segments.get(j);
                        for (TrackPoint trackPoint : segment.getTrackPoints()) {
                            Log.d(TAG, "    point: lat " + trackPoint.getLatitude() + ", lon " + trackPoint.getLongitude() + " " + trackPoint.getName());
                            double lat = trackPoint.getLatitude() ;
                            double lng = trackPoint.getLongitude() ;
                            LatLng position = new LatLng(lat, lng);

                            points.add(position);
                        }
                    }
                }
            }

            polyLineOptions.addAll(points);

            //add the polyline in google map
            map.addPolyline(polyLineOptions);
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(START_LOC, 15));
        GPXParser gpxParser = new GPXParser();
        Gpx parsedGpx = null;
        try {
            InputStream in = getAssets().open("artwork.gpx");
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
                List<TrackSegment> segments = track.getTrackSegments();
                for (int j = 0; j < segments.size(); j++) {
                    TrackSegment segment = segments.get(j);
                    for (TrackPoint trackPoint :segment.getTrackPoints()) {
                        String title ="";
                        String desc="";
                        int drawable=R.drawable.marker1;
                        switch (trackPoint.getName()){
                            case "1":title="Alt Wassenberg I";
                                    desc="Rückblick auf den Collé-Berg ";
                                    drawable=R.drawable.marker1;
                                    break;
                            case "2":title="Alt Wassenberg II";
                                desc="Erinnerungsstück für den Judenbruch ";
                                drawable=R.drawable.marker2;
                                break;
                            case "3":title="Effeld";
                                desc="Der Brot-Riecher";
                                drawable=R.drawable.marker3;
                                break;
                            case "4":title="Brüggelchen";
                                desc="Witterung der Mondknolle ";
                                drawable=R.drawable.marker4;
                                break;
                            case "5":title="Breberen";
                                desc="Die Ahnung der falschen Glocke ";
                                drawable=R.drawable.marker5;
                                break;
                            case "6":title="Schierwaldenrath";
                                desc="Transit Souvenir ";
                                drawable=R.drawable.marker6;
                                break;
                            case "7":title="Heinsberg";
                                desc="Ahnung vom Großen Haus";
                                drawable=R.drawable.marker7;
                                break;
                            case "8":title="Ratheim";
                                desc="Das große Einsehen";
                                drawable=R.drawable.marker_8;
                                break;
                            case "9":title="Millich I";
                                desc="Kleines Hopfen-Vorgefühl ";
                                drawable=R.drawable.marker9;
                                break;
                            case "10":title="Millich II";
                                desc="AEI23 Seher";
                                drawable=R.drawable.marker10;
                                break;
                            case "11":title="Hohenbusch I";
                                desc="Die Vermutung ";
                                drawable=R.drawable.marker11;
                                break;
                            case "12":title="Hohenbusch II";
                                desc=" Rückschau auf Josefa Breuer ́s Geschenk";
                                drawable=R.drawable.marker12;
                                break;
                            case "13":title="Tüschenbroich";
                                desc="Überblick mit Wegzehrung  ";
                                drawable=R.drawable.marker13;
                                break;
                            case "14":title="Wassenberg";
                                desc="Auge und Weltbild ";
                                drawable=R.drawable.marker14;
                                break;
                            case "21":title="Peter-Müller-Park";
                                drawable=R.drawable.marker_a;
                                break;
                            case "22":title="der Westlichste Punkt Deutschlands";
                                drawable=R.drawable.marker_b;
                                break;
                            case "23":title="Haus Wildenrath";
                                drawable=R.drawable.marker_c;
                                break;
                        }
                        switch(Integer.parseInt(trackPoint.getDesc())){
                            case 2: if (mode==1 | mode==2){
                                        map.addMarker(new MarkerOptions()
                                        .position(new LatLng(trackPoint.getLatitude(),trackPoint.getLongitude()))
                                        .icon(BitmapDescriptorFactory.fromResource(drawable))
                                        .title(title)
                                        .snippet(desc));
                                        drawCircle(new LatLng(trackPoint.getLatitude(),trackPoint.getLongitude()));
                                    }
                                    break;
                            case 3: if (mode==1 | mode==3){
                                        map.addMarker(new MarkerOptions()
                                        .position(new LatLng(trackPoint.getLatitude(),trackPoint.getLongitude()))
                                        .icon(BitmapDescriptorFactory.fromResource(drawable))
                                        .title(title)
                                        .snippet(desc));
                                        drawCircle(new LatLng(trackPoint.getLatitude(),trackPoint.getLongitude()));
                                    }
                                    break;
                            case 4: map.addMarker(new MarkerOptions()
                                        .position(new LatLng(trackPoint.getLatitude(),trackPoint.getLongitude()))
                                        .icon(BitmapDescriptorFactory.fromResource(drawable))
                                        .title(title)
                                        .snippet(desc));
                                        drawCircle(new LatLng(trackPoint.getLatitude(),trackPoint.getLongitude()));
                                    break;
                            case 5:
                                    break;
                        }

                    }
                }
            }
        }
        try {
            map.setMyLocationEnabled(true);
        }
        catch (SecurityException e){}

        // Override the default content description on the view, for accessibility mode.
        // Ideally this string would be localised.
        map.setContentDescription("Google Map with Artworks");
    }

    private void drawCircle(LatLng point){

        // Instantiating CircleOptions to draw a circle around the marker
        CircleOptions circleOptions = new CircleOptions();
        // Specifying the center of the circle
        circleOptions.center(point);
        // Radius of the circle
        circleOptions.radius(30);
        // Fill color of the circle
        circleOptions.fillColor(0x30ff0000);
        // Adding the circle to the GoogleMap
        map.addCircle(circleOptions);

    }

    /**
     * Receiver for broadcasts sent by {@link LocationService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            loc = intent.getParcelableExtra(LocationService.EXTRA_LOCATION);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(),loc.getLongitude()), 15));
            if (!reached) {
                reached = intent.getBooleanExtra(EXTRA_REACHED, false);
                if (reached)
                    onLocationReached();
            }
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
                Toast.makeText(this, getText(R.string.permission_location), Toast.LENGTH_LONG)
                        .show();
                if (!PermissionHelper.shouldShowRequestPermissionRationale(this)) {
                    // Permission denied with checking "Do not ask again".
                    PermissionHelper.launchPermissionSettings(this);
                }
                finish();
            }
        }
    }

    public void onLocationReached() {
        Toast.makeText(this, getText(R.string.navigation_reached), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                if (reached) {
                    new BottomDialog.Builder(NavigationActivity.this)
                            .setTitle(getText(R.string.navigation_title))
                            .setContent(getText(R.string.navigation_content))
                            .setNegativeText(getText(R.string.navigation_hide))
                            .onNegative(new BottomDialog.ButtonCallback() {
                                @Override
                                public void onClick(BottomDialog dialog) {
                                    reached = false;
                                    dialog.dismiss();
                                }
                            })
                            .setCancelable(false)
                            .autoDismiss(false)
                            .setPositiveText(getText(R.string.navigation_show))
                            .onPositive(new BottomDialog.ButtonCallback() {
                                @Override
                                public void onClick(@NonNull BottomDialog dialog) {
                                    reached = false;
                                    /* Create an Intent that will start the Activity. */
                                    Intent intent = new Intent(NavigationActivity.this, ArtWorkDisplayActivity.class);
                                    NavigationActivity.this.startActivity(intent);
                                    overridePendingTransition(R.anim.fadein, R.anim.hold);
                                }
                            })
                            .show();
                }

            }
        }, 3000);
    }

}