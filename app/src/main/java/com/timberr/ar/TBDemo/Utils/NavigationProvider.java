package com.timberr.ar.TBDemo.Utils;

import android.content.Context;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;

public class NavigationProvider {

    private BearingProvider bearingProvider;

    public NavigationProvider(Context context) {
        bearingProvider = new BearingProvider(context);
        GPXParser gpxParser=new GPXParser();
        Gpx parsedGpx = null;
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
//                        Log.d(TAG, "    point: lat " + trackPoint.getLatitude() + ", lon " + trackPoint.getLongitude());
//                    }
//                }
//            }
//        }
    }

    public void onResume(){

    }

    public void onPause(){

    }
}
