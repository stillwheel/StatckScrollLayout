package com.baidu.scrollstack.view;

import com.baidu.scrollstack.R;
import com.baidu.scrollstack.uitl.Define;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * The view to manage the header area in the expanded status bar.
 */
public class StackHeaderView extends RelativeLayout {

    private boolean mExpanded;
    private boolean mListening;

    private int mCollapsedHeight;
    private int mExpandedHeight;

    private final Rect mClipBounds = new Rect();

    private boolean mCaptureValues;
    private final LayoutValues mCollapsedValues = new LayoutValues();
    private final LayoutValues mExpandedValues = new LayoutValues();
    private final LayoutValues mCurrentValues = new LayoutValues();

    private float mCurrentT;

    public StackHeaderView(Context context) {
        this(context, null);
    }

    public StackHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StackHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Resources resources = getResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StackHeaderView, defStyleAttr, 0);
        mCollapsedHeight = a.getLayoutDimension(R.styleable.StackHeaderView_collapsed_height,
                resources.getDimensionPixelSize(R.dimen.status_bar_header_height));
        mExpandedHeight = a.getLayoutDimension(R.styleable.StackHeaderView_expanded_height,
                resources.getDimensionPixelSize(R.dimen.status_bar_header_height_expanded));
    }

    @TargetApi(21)
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right,
                                       int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if ((right - left) != (oldRight - oldLeft)) {
                    // width changed, update clipping
                    setClipping(getHeight());
                }
            }
        });
        if (Define.SDK_INT >= 21) {
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRect(mClipBounds);
                }
            });
        }
        requestCaptureValues();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l + getPaddingLeft(), t + getPaddingTop(), r - getPaddingRight(),
                b - getPaddingBottom());
        if (mCaptureValues) {
            if (mExpanded) {
                captureLayoutValues(mExpandedValues);
            } else {
                captureLayoutValues(mCollapsedValues);
            }
            mCaptureValues = false;
            updateLayoutValues(mCurrentT);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void requestCaptureValues() {
        mCaptureValues = true;
        requestLayout();
    }

    public int getCollapsedHeight() {
        return mCollapsedHeight;
    }

    public int getExpandedHeight() {
        return mExpandedHeight;
    }

    public void setListening(boolean listening) {
        if (listening == mListening) {
            return;
        }
        mListening = listening;
    }

    public void setExpanded(boolean expanded) {
        boolean changed = expanded != mExpanded;
        mExpanded = expanded;
        if (changed) {
            updateEverything();
        }
    }

    public void updateEverything() {
        updateHeights();
        requestCaptureValues();
    }

    private void updateHeights() {
        int height = mExpanded ? mExpandedHeight : mCollapsedHeight;
        ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp.height != height) {
            lp.height = height;
            setLayoutParams(lp);
        }
    }

    public void setExpansion(float t) {
        if (!mExpanded) {
            t = 0f;
        }
        mCurrentT = t;
        float height = mCollapsedHeight + t * (mExpandedHeight - mCollapsedHeight);
        if (height < mCollapsedHeight) {
            height = mCollapsedHeight;
        }
        if (height > mExpandedHeight) {
            height = mExpandedHeight;
        }
        setClipping(height);
        updateLayoutValues(t);
    }

    private void updateLayoutValues(float t) {
        if (mCaptureValues) {
            return;
        }
        mCurrentValues.interpoloate(mCollapsedValues, mExpandedValues, t);
    }


    @TargetApi(21)
    private void setClipping(float height) {
        if (Define.SDK_INT < 18) {
            ViewGroup.LayoutParams lp = getLayoutParams();
            if (lp.height != height) {
                lp.height = (int) height;
                setLayoutParams(lp);
            }
        } else {
            mClipBounds.set(getPaddingLeft(), 0, getWidth() - getPaddingRight(), (int) height);
            if (Define.SDK_INT >= 18) {
                setClipBounds(mClipBounds);
            }
            if (height != getHeight()) {
                if (Define.SDK_INT >= 21) {
                    invalidateOutline();
                } else {
                    requestLayout();
                }
            }
        }
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    private void captureLayoutValues(LayoutValues target) {
        target.signalClusterAlpha = 1f;
        target.settingsRotation = !mExpanded ? 90f : 0f;
    }

/**
 * Captures all layout values (position, visibility) for a certain state. This is used for
 * animations.
 */
private static final class LayoutValues {

    float dateExpandedAlpha;
    float dateCollapsedAlpha;
    float emergencyCallsOnlyAlpha;
    float alarmStatusAlpha;
    float timeScale = 1f;
    float dateY;
    float avatarScale;
    float avatarX;
    float avatarY;
    float batteryX;
    float batteryY;
    float batteryLevelAlpha;
    float settingsAlpha;
    float settingsTranslation;
    float signalClusterAlpha;
    float settingsRotation;

    public void interpoloate(LayoutValues v1, LayoutValues v2, float t) {
        timeScale = v1.timeScale * (1 - t) + v2.timeScale * t;
        dateY = v1.dateY * (1 - t) + v2.dateY * t;
        avatarScale = v1.avatarScale * (1 - t) + v2.avatarScale * t;
        avatarX = v1.avatarX * (1 - t) + v2.avatarX * t;
        avatarY = v1.avatarY * (1 - t) + v2.avatarY * t;
        batteryX = v1.batteryX * (1 - t) + v2.batteryX * t;
        batteryY = v1.batteryY * (1 - t) + v2.batteryY * t;
        settingsTranslation = v1.settingsTranslation * (1 - t) + v2.settingsTranslation * t;

        float t1 = Math.max(0, t - 0.5f) * 2;
        settingsRotation = v1.settingsRotation * (1 - t1) + v2.settingsRotation * t1;
        emergencyCallsOnlyAlpha =
                v1.emergencyCallsOnlyAlpha * (1 - t1) + v2.emergencyCallsOnlyAlpha * t1;

        float t2 = Math.min(1, 2 * t);
        signalClusterAlpha = v1.signalClusterAlpha * (1 - t2) + v2.signalClusterAlpha * t2;

        float t3 = Math.max(0, t - 0.7f) / 0.3f;
        batteryLevelAlpha = v1.batteryLevelAlpha * (1 - t3) + v2.batteryLevelAlpha * t3;
        settingsAlpha = v1.settingsAlpha * (1 - t3) + v2.settingsAlpha * t3;
        dateExpandedAlpha = v1.dateExpandedAlpha * (1 - t3) + v2.dateExpandedAlpha * t3;
        dateCollapsedAlpha = v1.dateCollapsedAlpha * (1 - t3) + v2.dateCollapsedAlpha * t3;
        alarmStatusAlpha = v1.alarmStatusAlpha * (1 - t3) + v2.alarmStatusAlpha * t3;
    }
}

}
