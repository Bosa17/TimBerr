package com.timberr.ar.TBDemo.Utils;

import com.timberr.ar.TBDemo.R;


public class FrameSelector {
    public static final String CHOSEN_FRAME="chosen_frame";
    public static final Integer[] frames={
            R.drawable.frame_1,
            R.drawable.frame_2,
            R.drawable.frame_3,
    };
    public static int chooseFrame(){
        return frames[(int)(System.currentTimeMillis() % frames.length)];
    }
}
