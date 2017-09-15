package com.gpstracker;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ScrollView;
import android.widget.Scroller;

/**
 * Created by RGarai on 27.10.2016.
 */

public class ScrollPager implements View.OnTouchListener {
    public ScrollPager(ScrollView aScrollView, ViewGroup aContentView) {
        final ScrollView mScrollView = aScrollView;
        ViewGroup mContentView = aContentView;
        final Scroller scroller = new Scroller(mScrollView.getContext(), new OvershootInterpolator());
        Runnable task = new Runnable() {
            public void run() {
                scroller.computeScrollOffset();
                mScrollView.scrollTo(0, scroller.getCurrY());

                if (!scroller.isFinished()) {
                    mScrollView.post(this);
                }
            }
        };
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}