package com.baidu.scrollstack.view;

import java.util.ArrayList;

import com.baidu.scrollstack.R;
import com.baidu.scrollstack.stack.StackScrollLayout;
import com.baidu.scrollstack.uitl.Define;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * An abstract view for expandable views.
 */
public abstract class ExpandableView extends FrameLayout implements View.OnClickListener {

    private final int mMaxNotificationHeight;
    protected int mActualHeight;
    protected int mClipTopAmount;
    protected int currentAlpha;
    protected int shadowRadius;
    protected int shadowStartColor;
    protected int shadowEndColor;
    private OnHeightChangedListener mOnHeightChangedListener;
    private boolean mActualHeightInitialized;
    private ArrayList<View> mMatchParentViews = new ArrayList<View>();
    private PerformClick performClick;
    private OnClickListener onClickListener;

    public ExpandableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMaxNotificationHeight = getResources().getDimensionPixelSize(
                R.dimen.notification_max_height);
        shadowRadius = getResources().getDimensionPixelSize(
                R.dimen.shadow_radius_size);
        shadowStartColor = getResources().getColor(R.color.shadow_start_color);
        shadowEndColor = getResources().getColor(R.color.shadow_end_color);
        performClick = new PerformClick();
        super.setOnClickListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int ownMaxHeight = mMaxNotificationHeight;
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        boolean hasFixedHeight = heightMode == MeasureSpec.EXACTLY;
        boolean isHeightLimited = heightMode == MeasureSpec.AT_MOST;
        if (hasFixedHeight || isHeightLimited) {
            int size = MeasureSpec.getSize(heightMeasureSpec);
            ownMaxHeight = Math.min(ownMaxHeight, size);
        }
        int newHeightSpec = MeasureSpec.makeMeasureSpec(ownMaxHeight, MeasureSpec.AT_MOST);
        int maxChildHeight = 0;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            int childHeightSpec = newHeightSpec;
            ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
            if (layoutParams.height != ViewGroup.LayoutParams.MATCH_PARENT) {
                if (layoutParams.height >= 0) {
                    // An actual height is set
                    childHeightSpec = layoutParams.height > ownMaxHeight
                            ? MeasureSpec.makeMeasureSpec(ownMaxHeight, MeasureSpec.EXACTLY)
                            : MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY);
                }
                child.measure(
                        getChildMeasureSpec(widthMeasureSpec, 0 /* padding */, layoutParams.width),
                        childHeightSpec);
                int childHeight = child.getMeasuredHeight();
                if (Define.SDK_INT < 21) {
                    childHeight += shadowRadius;
                    maxChildHeight += shadowRadius;
                }
                maxChildHeight = Math.max(maxChildHeight, childHeight);
            } else {
                mMatchParentViews.add(child);
            }
        }
        int ownHeight = hasFixedHeight ? ownMaxHeight : maxChildHeight;
        newHeightSpec = MeasureSpec.makeMeasureSpec(ownHeight, MeasureSpec.EXACTLY);
        for (View child : mMatchParentViews) {
            child.measure(getChildMeasureSpec(
                    widthMeasureSpec, 0 /* padding */, child.getLayoutParams().width),
                    newHeightSpec);
        }
        mMatchParentViews.clear();
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, ownHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!mActualHeightInitialized && mActualHeight == 0) {
            int initialHeight = getInitialHeight();
            if (initialHeight != 0) {
                setActualHeight(initialHeight);
            }
        }
    }

    protected int getInitialHeight() {
        return getHeight();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (filterMotionEvent(ev)) {
            return super.dispatchTouchEvent(ev);
        }
        return false;
    }

    private boolean filterMotionEvent(MotionEvent event) {
        return event.getActionMasked() != MotionEvent.ACTION_DOWN
                || event.getY() > mClipTopAmount && event.getY() < mActualHeight;
    }

    /**
     * Sets the actual height of this notification. This is different than the laid out
     * {@link View#getHeight()}, as we want to avoid layouting during scrolling and expanding.
     *
     * @param actualHeight    The height of this notification.
     * @param notifyListeners Whether the listener should be informed about the change.
     */
    public void setActualHeight(int actualHeight, boolean notifyListeners) {
        mActualHeightInitialized = true;
        mActualHeight = actualHeight;
        if (notifyListeners) {
            notifyHeightChanged();
        }
    }

    /**
     * See {@link #setActualHeight}.
     *
     * @return The current actual height of this notification.
     */
    public int getActualHeight() {
        return mActualHeight;
    }

    public void setActualHeight(int actualHeight) {
        setActualHeight(actualHeight, true);
    }

    /**
     * @return The maximum height of this notification.
     */
    public int getMaxHeight() {
        return getHeight();
    }

    /**
     * @return The minimum height of this notification.
     */
    public int getMinHeight() {
        return getHeight();
    }

    /**
     * Sets the notification as dimmed. The default implementation does nothing.
     *
     * @param dimmed Whether the notification should be dimmed.
     * @param fade   Whether an animation should be played to change the state.
     */
    public void setDimmed(boolean dimmed, boolean fade) {
    }

    /**
     * Sets the notification as dark. The default implementation does nothing.
     *
     * @param dark Whether the notification should be dark.
     * @param fade Whether an animation should be played to change the state.
     */
    public void setDark(boolean dark, boolean fade) {
    }

    /**
     * See {@link #setHideSensitive}. This is a variant which notifies this view in advance about
     * the upcoming state of hiding sensitive notifications. It gets called at the very beginning
     * of a stack scroller update such that the updated intrinsic height (which is dependent on
     * whether private or public layout is showing) gets taken into account into all layout
     * calculations.
     */
    public void setHideSensitiveForIntrinsicHeight(boolean hideSensitive) {
    }

    /**
     * Sets whether the notification should hide its private contents if it is sensitive.
     */
    public void setHideSensitive(boolean hideSensitive, boolean animated, long delay,
                                 long duration) {
    }

    /**
     * @return The desired notification height.
     */
    public int getIntrinsicHeight() {
        return getHeight();
    }

    public int getClipTopAmount() {
        return mClipTopAmount;
    }

    /**
     * Sets the amount this view should be clipped from the top. This is used when an expanded
     * notification is scrolling in the top or bottom stack.
     *
     * @param clipTopAmount The amount of pixels this view should be clipped from top.
     */
    public void setClipTopAmount(int clipTopAmount) {
        mClipTopAmount = clipTopAmount;
    }

    public void setOnHeightChangedListener(OnHeightChangedListener listener) {
        mOnHeightChangedListener = listener;
    }

    /**
     * @return Whether we can expand this views content.
     */
    public boolean isContentExpandable() {
        return false;
    }

    public void notifyHeightChanged() {
        if (mOnHeightChangedListener != null) {
            mOnHeightChangedListener.onHeightChanged(this);
        }
    }

    public boolean isTransparent() {
        return false;
    }

    /**
     * Perform a remove animation on this view.
     *
     * @param duration             The duration of the remove animation.
     * @param translationDirection The direction value from [-1 ... 1] indicating in which the
     *                             animation should be performed. A value of -1 means that The
     *                             remove animation should be performed upwards,
     *                             such that the  child appears to be going away to the top. 1
     *                             Should mean the opposite.
     * @param onFinishedRunnable   A runnable which should be run when the animation is finished.
     */
    public abstract void performRemoveAnimation(long duration, float translationDirection,
                                                Runnable onFinishedRunnable);

    public abstract void performAddAnimation(long delay, long duration);

    public void onHeightReset() {
        if (mOnHeightChangedListener != null) {
            mOnHeightChangedListener.onReset(this);
        }
    }

    /**
     * This method returns the drawing rect for the view which is different from the regular
     * drawing rect, since we layout all children in the {@link StackScrollLayout} at
     * position 0 and usually the translation is neglected. Since we are manually clipping this
     * view,we also need to subtract the clipTopAmount from the top. This is needed in order to
     * ensure that accessibility and focusing work correctly.
     *
     * @param outRect The (scrolled) drawing bounds of the view.
     */
    @Override
    public void getDrawingRect(Rect outRect) {
        super.getDrawingRect(outRect);
        outRect.left += getTranslationX();
        outRect.right += getTranslationX();
        outRect.bottom = (int) (outRect.top + getTranslationY() + getActualHeight());
        outRect.top += getTranslationY() + getClipTopAmount();
    }

    @Override
    public String toString() {
        return "ExpandableView{" +
                "this:" + getId() +
                ", mMaxNotificationHeight=" + mMaxNotificationHeight +
                ", mOnHeightChangedListener=" + mOnHeightChangedListener +
                ", mActualHeight=" + mActualHeight +
                ", mClipTopAmount=" + mClipTopAmount +
                ", mActualHeightInitialized=" + mActualHeightInitialized +
                ", mMatchParentViews=" + mMatchParentViews +
                '}';
    }

    @Override
    public float getTranslationZ() {
        if (Define.SDK_INT >= 21) {
            return super.getTranslationZ();
        } else {
            return 0;
        }
    }

    @Override
    public void setTranslationZ(float translationZ) {
        if (Define.SDK_INT >= 21) {
            super.setTranslationZ(translationZ);
        }
    }

    @Override
    public void setTranslationY(float translationY) {
        super.setTranslationY(translationY);
    }

    @Override
    public void setClipBounds(Rect clipBounds) {
        if (Define.SDK_INT >= 18) {
            super.setClipBounds(clipBounds);
        }
    }

    @Override
    public void setAlpha(float alpha) {
        if (Define.SDK_INT >= 21) {
            super.setAlpha(alpha);
        }
    }

    public void setShadowAlpha(float distance, int height) {
        if (Define.SDK_INT >= 21) {
            return;
        }
        int maxAlpha = 200;
        int newAlpha = (int) ((height - distance + shadowRadius) / shadowRadius * maxAlpha);
        newAlpha = Math.max(newAlpha, 0);
        newAlpha = Math.min(newAlpha, maxAlpha);
        if (currentAlpha != newAlpha) {
            currentAlpha = newAlpha;
            invalidate();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (Define.SDK_INT < 21) {
            super.dispatchDraw(canvas);

            canvas.save();
            Paint paint = new Paint();
            paint.setColor(Color.TRANSPARENT);
            paint.setAlpha(currentAlpha);
            paint.setStrokeWidth(0);

            Shader shader = new LinearGradient(0, getHeight() - shadowRadius, 0, getHeight(),
                    new int[] {shadowStartColor, shadowEndColor}, null, Shader.TileMode.MIRROR);
            paint.setShader(shader);
            canvas.drawRect(0, getHeight() - shadowRadius, getWidth(), getHeight(), paint);

            canvas.restore();
        } else {
            super.dispatchDraw(canvas);
        }
    }

    protected void log(String tag, String log) {
        ViewGroup parent = (ViewGroup) getParent();
        int index = parent.indexOfChild(this);
        Log.i(tag + " : " + index, log);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                if (performClick != null) {
                    performClick.performClick(this);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        if (performClick != null) {
            performClick.performClick(this);
        }
        if (onClickListener != null) {
            onClickListener.onClick(this);
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        onClickListener = l;
    }

    /**
     * A listener notifying when {@link #getActualHeight} changes.
     */
    public interface OnHeightChangedListener {

        /**
         * @param view the view for which the height changed, or {@code null} if just the top
         *             padding or the padding between the elements changed
         */
        void onHeightChanged(ExpandableView view);

        /**
         * Called when the view is reset and therefore the height will change abruptly
         *
         * @param view The view which was reset.
         */
        void onReset(ExpandableView view);
    }

    private class PerformClick {

        public void performClick(View v) {
            ViewGroup parent = (ViewGroup) getParent();
            if (parent != null && parent instanceof StackScrollLayout) {
                StackScrollLayout StackScrollLayout = (StackScrollLayout) parent;
                StackScrollLayout.performClick(v);
            }
        }
    }
}
