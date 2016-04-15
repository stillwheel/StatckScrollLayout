package com.baidu.scrollstack.view;

import android.view.MotionEvent;

/**
 * An interface for a velocity tracker to delegate. To be implemented by different velocity tracking
 * algorithms.
 */
public interface VelocityTrackerInterface {
    void addMovement(MotionEvent event);

    void computeCurrentVelocity(int units);

    float getXVelocity();

    float getYVelocity();

    void recycle();
}
