package com.baidu.scrollstack.view;

import android.view.animation.Interpolator;

/**
 * An implementation of a bouncer interpolator optimized for unlock hinting.
 */
public class BounceInterpolator implements Interpolator {

    private final static float SCALE_FACTOR = 7.5625f;

    @Override
    public float getInterpolation(float t) {
        t *= 11f / 10f;
        if (t < 4f / 11f) {
            return SCALE_FACTOR * t * t;
        } else if (t < 8f / 11f) {
            float t2 = t - 6f / 11f;
            return SCALE_FACTOR * t2 * t2 + 3f / 4f;
        } else if (t < 10f / 11f) {
            float t2 = t - 9f / 11f;
            return SCALE_FACTOR * t2 * t2 + 15f / 16f;
        } else {
            return 1;
        }
    }
}
