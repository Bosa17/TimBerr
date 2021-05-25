package com.timberr.ar.TBDemo.Utils;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class PhotoVideoButton extends androidx.appcompat.widget.AppCompatButton {


    public PhotoVideoButton(Context context) {
        super(context);
    }

    public PhotoVideoButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoVideoButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN :
                performClick();
                long down=System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP :
                long up=System.currentTimeMillis();
                return true;
        }
        return false;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        l=new OnClickListener() {
            @Override
            public void onClick(View view) {
                //photo
            }
        };
        super.setOnClickListener(l);
    }

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
        l=new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //video
                return true;
            }
        };
        super.setOnLongClickListener(l);
    }
}
