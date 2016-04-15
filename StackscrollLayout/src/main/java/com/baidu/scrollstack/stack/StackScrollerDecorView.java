package com.baidu.scrollstack.stack;

import com.baidu.scrollstack.BuildConfig;
import com.baidu.scrollstack.uitl.Define;
import com.baidu.scrollstack.uitl.LocalPathInterpolator;
import com.baidu.scrollstack.view.ExpandableView;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

/**
 * A common base class for all views in the notification stack scroller which don't have a
 * background.
 */
public abstract class StackScrollerDecorView extends ExpandableView {
    public final Interpolator ALPHA_IN = getPathInterpolator(0.4f, 0f, 1f, 1f);
    public final Interpolator ALPHA_OUT = getPathInterpolator(0f, 0f, 0.8f, 1f);
    protected View mContent;
    private boolean mIsVisible;
    private boolean mAnimating;
    private boolean mWillBeGone;

    public StackScrollerDecorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(21)
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContent = findContentView();
        setInvisible();
        if (Define.SDK_INT >= 21 && getBackground() == null) {
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    Rect clipBounds = new Rect();
                    clipBounds.set(getPaddingLeft(), 0, getWidth() - getPaddingRight(), getHeight());
                    outline.setRect(clipBounds);
                }
            });
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    public void performVisibilityAnimation(boolean nowVisible) {
        animateText(nowVisible, null /* onFinishedRunnable */);
    }

    public void performVisibilityAnimation(boolean nowVisible, Runnable onFinishedRunnable) {
        animateText(nowVisible, onFinishedRunnable);
    }

    public boolean isVisible() {
        return mIsVisible || mAnimating;
    }

    /**
     * Animate the text to a new visibility.
     *
     * @param nowVisible should it now be visible
     * @param onFinishedRunnable A runnable which should be run when the animation is
     *        finished.
     */
    private void animateText(boolean nowVisible, final Runnable onFinishedRunnable) {
        if (nowVisible != mIsVisible) {
            // Animate text
            float endValue = nowVisible ? 1.0f : 0.0f;
            Interpolator interpolator;
            if (nowVisible) {
                interpolator = ALPHA_IN;
            } else {
                interpolator = ALPHA_OUT;
            }
            mAnimating = true;
            mContent.animate()
                    .alpha(endValue)
                    .setInterpolator(interpolator)
                    .setDuration(260)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mAnimating = false;
                            if (onFinishedRunnable != null) {
                                onFinishedRunnable.run();
                            }
                        }
                    });

            mIsVisible = nowVisible;
        } else {
            if (onFinishedRunnable != null) {
                onFinishedRunnable.run();
            }
        }
    }

    public void setInvisible() {
        mContent.setAlpha(0.0f);
        mIsVisible = false;
    }

    @Override
    public void performRemoveAnimation(long duration, float translationDirection,
            Runnable onFinishedRunnable) {
        // TODO: Use duration
        performVisibilityAnimation(false);
    }

    @Override
    public void performAddAnimation(long delay, long duration) {
        // TODO: use delay and duration
        performVisibilityAnimation(true);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    public void cancelAnimation() {
        mContent.animate().cancel();
    }

    public boolean willBeGone() {
        return mWillBeGone;
    }

    public void setWillBeGone(boolean willBeGone) {
        mWillBeGone = willBeGone;
    }

    protected abstract View findContentView();

    @TargetApi(21)
    private Interpolator getPathInterpolator(float controlX1, float controlY1, float controlX2, float controlY2){
       return Define.SDK_INT >= 21 ? new PathInterpolator(controlX1, controlY1, controlX2, controlY2)
                : new LocalPathInterpolator(controlX1, controlY1, controlX2, controlY2);
    }
}
