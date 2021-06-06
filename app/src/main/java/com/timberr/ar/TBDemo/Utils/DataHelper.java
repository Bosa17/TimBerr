package com.timberr.ar.TBDemo.Utils;

import android.content.Context;

import com.orhanobut.hawk.Hawk;

import static com.timberr.ar.TBDemo.NavigationActivity.ROUTE_MODE;
import static com.timberr.ar.TBDemo.Utils.FrameSelector.CHOSEN_FRAME;
import static com.timberr.ar.TBDemo.Utils.LocationService.EXTRA_REACHED;

public class DataHelper {
    private Context mContext;


    public DataHelper(Context context) {
        mContext = context;
        Hawk.init(mContext).build();
    }

    public void setRouteMode(int mode){
        Hawk.put(ROUTE_MODE, mode);
    }

    public int getRouteMode(){
        return Hawk.get(ROUTE_MODE);
    }

    public void setFrame(int frame){
        Hawk.put(CHOSEN_FRAME, frame);
    }

    public int getFrame(){
        return Hawk.get(CHOSEN_FRAME);
    }

    public void setArtworkReached(int reached){
        Hawk.put(EXTRA_REACHED, reached);
    }

    public int getArtworkReached(){
        return Hawk.get(EXTRA_REACHED);
    }
}