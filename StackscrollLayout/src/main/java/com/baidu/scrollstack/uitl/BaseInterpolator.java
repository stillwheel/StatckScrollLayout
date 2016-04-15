package com.baidu.scrollstack.uitl;

import android.view.animation.Interpolator;

/**
 * Created by baidu on 16/4/1.
 */
abstract public class BaseInterpolator implements Interpolator {
    private int mChangingConfiguration;
    /**
     * @hide
     */
    public int getChangingConfiguration() {
        return mChangingConfiguration;
    }

    /**
     * @hide
     */
    void setChangingConfiguration(int changingConfiguration) {
        mChangingConfiguration = changingConfiguration;
    }
}