package com.baidu.scrollstack.view;

import com.baidu.scrollstack.R;
import com.baidu.scrollstack.stack.StackScrollLayout;
import com.baidu.scrollstack.stack.StackStateAnimator;
import com.baidu.scrollstack.uitl.Define;
import com.baidu.scrollstack.uitl.FlingAnimationUtils;
import com.baidu.scrollstack.uitl.LocalPathInterpolator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class StackScrollPanelView extends PanelView implements
        ExpandableView.OnHeightChangedListener, ObservableScrollView.Listener,
        View.OnClickListener, StackScrollLayout.OnOverscrollTopChangedListener {

    private static final String TAG = "StackScrollPanelView";

    private static final float HEADER_RUBBERBAND_FACTOR = 2.05f;

    private StackHeaderView mHeader;
    private final Runnable mUpdateHeader = new Runnable() {
        @Override
        public void run() {
            mHeader.updateEverything();
        }
    };
    private View mScrollLayout;
    private ObservableScrollView mScrollView;
    private View mSCrollLayoutNavbarScrim;
    private StackScrollLayout mStackScroller;
    private int mNotificationTopPadding;
    private boolean mAnimateNextTopPaddingChange;
    private int mTrackingPointer;
    private VelocityTracker mVelocityTracker;
    private boolean mScrollLayoutTracking;
    private int mBackgroundColor;
    private int mBackgroundAlpha;
    /**
     * If set, the ongoing touch gesture might both trigger the expansion in {@link PanelView} and
     * the expansion for quick settings.
     */
    private boolean mConflictingScrollLayoutExpansionGesture;
    /**
     * Whether we are currently handling a motion gesture in #onInterceptTouchEvent, but haven't
     * intercepted yet.
     */
    private boolean mIntercepting;
    private boolean mScrollLayoutExpanded;
    private boolean mScrollLayoutExpandedWhenExpandingStarted;
    private boolean mScrollLayoutFullyExpanded;
    private float mInitialHeightOnTouch;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private float mLastTouchX;
    private float mLastTouchY;
    private float mScrollLayoutExpansionHeight;
    private int mScrollLayoutMinExpansionHeight;
    private int mScrollLayoutMaxExpansionHeight;
    private int mScrollLayoutPeekHeight;
    private boolean mStackScrollerOverscrolling;
    private boolean mScrollLayoutExpansionFromOverscroll;
    private float mLastOverscroll;
    private boolean mScrollLayoutExpansionEnabled = true;
    private ValueAnimator mScrollLayoutExpansionAnimator;
    private FlingAnimationUtils mFlingAnimationUtils;
    private int mStatusBarMinHeight;
    private Interpolator mFastOutSlowInInterpolator;
    private int mTopPaddingAdjustment;
    private boolean mIsExpanding;
    private boolean mBlockTouches;
    private boolean mTwoFingerScrollLayoutExpand;
    private boolean mTwoFingerScrollLayoutExpandPossible;
    private boolean mEnableOverScroll;
    /**
     * If we are in a panel collapsing motion, we reset scrollY of our scroll view but still
     * need to take this into account in our panel height calculation.
     */
    private int mScrollYOverride = -1;
    private boolean mSCrollLayoutAnimatorExpand;
    private boolean mOnlyAffordanceInThisMotion;
    private boolean mHeaderAnimatingIn;
    private ObjectAnimator mScrollContainerAnimator;
    private final View.OnLayoutChangeListener mScrollContainerAnimatorUpdater
            = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                                   int oldTop, int oldRight, int oldBottom) {
            int oldHeight = oldBottom - oldTop;
            int height = bottom - top;
            if (height != oldHeight && mScrollContainerAnimator != null) {
                PropertyValuesHolder[] values = mScrollContainerAnimator.getValues();
                float newEndValue = mHeader.getCollapsedHeight() + mScrollLayoutPeekHeight - height - top;
                float newStartValue = -height - top;
                values[0].setFloatValues(newStartValue, newEndValue);
                mScrollContainerAnimator.setCurrentPlayTime(mScrollContainerAnimator.getCurrentPlayTime());
            }
        }
    };
    private final Animator.AnimatorListener mAnimateHeaderSlidingInListener
            = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            mHeaderAnimatingIn = false;
            mScrollContainerAnimator = null;
            mScrollLayout.removeOnLayoutChangeListener(mScrollContainerAnimatorUpdater);
        }
    };
    private final ViewTreeObserver.OnPreDrawListener mStartHeaderSlidingIn
            = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            getViewTreeObserver().removeOnPreDrawListener(this);
            mHeader.setTranslationY(-mHeader.getCollapsedHeight() - mScrollLayoutPeekHeight);
            mHeader.animate()
                    .translationY(0f)
                    .setStartDelay(300)
                    .setDuration(StackStateAnimator.ANIMATION_DURATION_GO_TO_FULL_SHADE)
                    .setInterpolator(mFastOutSlowInInterpolator)
                    .start();
            mScrollLayout.setY(-mScrollLayout.getHeight());
            mScrollContainerAnimator = ObjectAnimator.ofFloat(mScrollLayout, View.TRANSLATION_Y,
                    mScrollLayout.getTranslationY(),
                    mHeader.getCollapsedHeight() + mScrollLayoutPeekHeight - mScrollLayout.getHeight()
                            - mScrollLayout.getTop());
            mScrollContainerAnimator.setStartDelay(300);
            mScrollContainerAnimator.setDuration(StackStateAnimator.ANIMATION_DURATION_GO_TO_FULL_SHADE);
            mScrollContainerAnimator.setInterpolator(mFastOutSlowInInterpolator);
            mScrollContainerAnimator.addListener(mAnimateHeaderSlidingInListener);
            mScrollContainerAnimator.start();
            mScrollLayout.addOnLayoutChangeListener(mScrollContainerAnimatorUpdater);
            return true;
        }
    };
    private boolean mScrollScrimEnabled = true;

    public StackScrollPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeader = (StackHeaderView) findViewById(R.id.header);
        if (mHeader == null) {
            mHeader = new StackHeaderView(getContext());
        } else {
            mHeader.setOnClickListener(this);
        }
        mScrollLayout = findViewById(R.id.stack_spring_layout);
        mScrollView = (ObservableScrollView) findViewById(R.id.scroll_view);
        if (mScrollLayout == null) {
            mScrollLayout = new RelativeLayout(getContext());
        }
        if (mScrollView == null) {
            mScrollView = new ObservableScrollView(getContext());
        } else {
            mScrollView.setListener(this);
            mScrollView.setFocusable(false);
        }
        mStackScroller = (StackScrollLayout)
                findViewById(R.id.stack_scroller);
        mStackScroller.setOnHeightChangedListener(this);
        mStackScroller.setOverscrollTopChangedListener(this);
        mStackScroller.setScrollView(mScrollView);
        if (Define.SDK_INT >= 21) {
            mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(getContext(),
                    android.R.interpolator.fast_out_slow_in);
        } else {
            mFastOutSlowInInterpolator = new LocalPathInterpolator(0.4f, 0, 0.2f, 1);
        }

        mSCrollLayoutNavbarScrim = findViewById(R.id.scroll_navbar_scrim);

        // recompute internal state when scroll panel height changes
        mScrollLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right,
                                       int bottom, int oldLeft, int oldTop, int oldRight,
                                       int oldBottom) {
                final int height = bottom - top;
                final int oldHeight = oldBottom - oldTop;
                if (height != oldHeight) {
                    onScrollChanged();
                }
            }
        });

        Drawable bgDrawable = getBackground();
        if (bgDrawable != null && bgDrawable instanceof ColorDrawable) {
            ColorDrawable drawable = (ColorDrawable) getBackground();
            mBackgroundColor = drawable.getColor();
            mBackgroundAlpha = Color.alpha(mBackgroundColor);
        }
        setBackgroundColor(0);
        updateResources();
        updateScrollLayoutState();
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    protected void loadDimens() {
        super.loadDimens();
        mNotificationTopPadding = getResources().getDimensionPixelSize(
                R.dimen.stackitem_top_padding);
        mFlingAnimationUtils = new FlingAnimationUtils(getContext(), 0.4f);
        mStatusBarMinHeight = getStatusBarHeight();
        mScrollLayoutPeekHeight = getResources().getDimensionPixelSize(R.dimen.scroll_peek_height);
    }

    public void updateResources() {
        int panelWidth = getResources().getDimensionPixelSize(R.dimen.stack_scroll_panel_width);
        int panelGravity = getResources().getInteger(R.integer.stack_scroll_layout_gravity);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mHeader.getLayoutParams();
        if (lp == null) {
            mHeader.post(mUpdateHeader);
        } else if (lp.width != panelWidth) {
            lp.width = panelWidth;
            lp.gravity = panelGravity;
            mHeader.setLayoutParams(lp);
            mHeader.post(mUpdateHeader);
        }

        lp = (FrameLayout.LayoutParams) mStackScroller.getLayoutParams();
        if (lp.width != panelWidth) {
            lp.width = panelWidth;
            lp.gravity = panelGravity;
            mStackScroller.setLayoutParams(lp);
        }

        lp = (FrameLayout.LayoutParams) mScrollView.getLayoutParams();
        if (lp == null) {
            lp = new FrameLayout.LayoutParams(panelWidth, 1, panelGravity);
            mScrollView.setLayoutParams(lp);
        } else if (lp.width != panelWidth) {
            lp.width = panelWidth;
            lp.gravity = panelGravity;
            mScrollView.setLayoutParams(lp);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // Calculate quick setting heights.
        mScrollLayoutMinExpansionHeight = mHeader.getCollapsedHeight() + mScrollLayoutPeekHeight;
        mScrollLayoutMaxExpansionHeight = mHeader.getExpandedHeight() + mScrollLayout.getHeight();
        positionClockAndNotifications();
        if (mScrollLayoutExpanded) {
            if (mScrollLayoutFullyExpanded) {
                mScrollLayoutExpansionHeight = mScrollLayoutMaxExpansionHeight;
                requestScrollerTopPaddingUpdate(false /* animate */);
            }
        } else {
            setScrollLayoutExpansion(mScrollLayoutMinExpansionHeight + mLastOverscroll);
            mStackScroller.setStackHeight(getExpandedHeight());
            updateHeader();
        }
        mStackScroller.updateIsSmallScreen(
                mHeader.getCollapsedHeight() + mScrollLayoutPeekHeight);
    }

    /**
     * Positions the clock and notifications dynamically depending on how many notifications are
     * showing.
     */
    private void positionClockAndNotifications() {
        boolean animate = mStackScroller.isAddOrRemoveAnimationPending();
        int stackScrollerPadding = 0;
        int bottom = mHeader.getCollapsedHeight();
        stackScrollerPadding = bottom + mScrollLayoutPeekHeight + mNotificationTopPadding;
        mTopPaddingAdjustment = 0;
        mStackScroller.setIntrinsicPadding(stackScrollerPadding);
        requestScrollerTopPaddingUpdate(animate);
    }

    public void animateToFullShade(long delay) {
        mAnimateNextTopPaddingChange = true;
        mStackScroller.goToFullShade(delay);
        requestLayout();
    }

    public void setScrollLayoutExpansionEnabled(boolean scrollLayoutExpansionEnabled) {
        mScrollLayoutExpansionEnabled = scrollLayoutExpansionEnabled;
        mHeader.setClickable(scrollLayoutExpansionEnabled);
    }

    @Override
    public void resetViews() {
        mBlockTouches = false;
        closeScrollLayout();
        mStackScroller.setOverScrollAmount(0f, true /* onTop */, false /* animate */,
                true /* cancelAnimators */);
    }

    public void closeScrollLayout() {
        cancelAnimation();
        setScrollLayoutExpansion(mScrollLayoutMinExpansionHeight);
    }

    public void animateCloseScrollLayout() {
        if (mScrollLayoutExpansionAnimator != null) {
            if (!mSCrollLayoutAnimatorExpand) {
                return;
            }
            float height = mScrollLayoutExpansionHeight;
            mScrollLayoutExpansionAnimator.cancel();
            setScrollLayoutExpansion(height);
        }
        flingSettings(0 /* vel */, false);
    }

    public void openScrollLayout() {
        cancelAnimation();
        if (mScrollLayoutExpansionEnabled) {
            setScrollLayoutExpansion(mScrollLayoutMaxExpansionHeight);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mBlockTouches) {
            return false;
        }
        resetDownStates(event);
        int pointerIndex = event.findPointerIndex(mTrackingPointer);
        if (pointerIndex < 0) {
            pointerIndex = 0;
            mTrackingPointer = event.getPointerId(pointerIndex);
        }
        final float x = event.getX(pointerIndex);
        final float y = event.getY(pointerIndex);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mIntercepting = true;
                mInitialTouchY = y;
                mInitialTouchX = x;
                initVelocityTracker();
                trackMovement(event);
                if (shouldQuickSettingsIntercept(mInitialTouchX, mInitialTouchY, 0)) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (mScrollLayoutExpansionAnimator != null) {
                    onScrollLayoutExpansionStarted();
                    mInitialHeightOnTouch = mScrollLayoutExpansionHeight;
                    mScrollLayoutTracking = true;
                    mIntercepting = false;
                    mStackScroller.removeLongPressCallback();
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                final int upPointer = event.getPointerId(event.getActionIndex());
                if (mTrackingPointer == upPointer) {
                    // gesture is ongoing, find a new pointer to track
                    final int newIndex = event.getPointerId(0) != upPointer ? 0 : 1;
                    mTrackingPointer = event.getPointerId(newIndex);
                    mInitialTouchX = event.getX(newIndex);
                    mInitialTouchY = event.getY(newIndex);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                final float h = y - mInitialTouchY;
                trackMovement(event);
                if (mScrollLayoutTracking) {
                    // Already tracking because onOverscrolled was called. We need to update here
                    // so we don't stop for a frame until the next touch event gets handled in
                    // onTouchEvent.
                    setScrollLayoutExpansion(h + mInitialHeightOnTouch);
                    trackMovement(event);
                    mIntercepting = false;
                    return true;
                }
                if (Math.abs(h) > mTouchSlop && Math.abs(h) > Math.abs(x - mInitialTouchX)
                        && shouldQuickSettingsIntercept(mInitialTouchX, mInitialTouchY, h)) {
                    onScrollLayoutExpansionStarted();
                    mInitialHeightOnTouch = mScrollLayoutExpansionHeight;
                    mInitialTouchY = y;
                    mInitialTouchX = x;
                    mScrollLayoutTracking = true;
                    mIntercepting = false;
                    mStackScroller.removeLongPressCallback();
                    return true;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                trackMovement(event);
                if (mScrollLayoutTracking) {
                    flingScrollLayoutWithCurrentVelocity();
                    mScrollLayoutTracking = false;
                }
                mIntercepting = false;
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    private void resetDownStates(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mOnlyAffordanceInThisMotion = false;
        }
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        // Block request when interacting with the scroll view so we can still intercept the
        // scrolling when ScrollLayout is expanded.
        if (mScrollView.isHandlingTouchEvent()) {
            return;
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    private void flingScrollLayoutWithCurrentVelocity() {
        float vel = getCurrentVelocity();
        flingSettings(vel, flingExpandsScrollLayout(vel));
    }

    private boolean flingExpandsScrollLayout(float vel) {
        if (Math.abs(vel) < mFlingAnimationUtils.getMinVelocityPxPerSecond()) {
            return getScrollLayoutExpansionFraction() > 0.5f;
        } else {
            return vel > 0;
        }
    }

    private float getScrollLayoutExpansionFraction() {
        return Math.min(1f, (mScrollLayoutExpansionHeight - mScrollLayoutMinExpansionHeight)
                / (getTempScrollLayoutMaxExpansion() - mScrollLayoutMinExpansionHeight));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mBlockTouches) {
            return false;
        }
        resetDownStates(event);
        if (mOnlyAffordanceInThisMotion) {
            return true;
        }
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN && getExpandedFraction() == 1f
                && !mScrollLayoutExpanded && mScrollLayoutExpansionEnabled) {

            // Down in the empty area while fully expanded - go to ScrollLayout.
            mScrollLayoutTracking = true;
            mConflictingScrollLayoutExpansionGesture = true;
            onScrollLayoutExpansionStarted();
            mInitialHeightOnTouch = mScrollLayoutExpansionHeight;
            mInitialTouchY = event.getX();
            mInitialTouchX = event.getY();
        }
        if (mExpandedHeight != 0) {
            handleScrollLayoutDown(event);
        }
        if (!mTwoFingerScrollLayoutExpand && mScrollLayoutTracking) {
            onScrollLayoutTouch(event);
            if (!mConflictingScrollLayoutExpansionGesture) {
                return true;
            }
        }
        if (event.getActionMasked() == MotionEvent.ACTION_CANCEL
                || event.getActionMasked() == MotionEvent.ACTION_UP) {
            mConflictingScrollLayoutExpansionGesture = false;
        }
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN && mExpandedHeight == 0
                && mScrollLayoutExpansionEnabled) {
            mTwoFingerScrollLayoutExpandPossible = true;
        }
        if (mTwoFingerScrollLayoutExpandPossible && event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN
                && event.getPointerCount() == 2
                && event.getY(event.getActionIndex()) < mStatusBarMinHeight) {
            mTwoFingerScrollLayoutExpand = true;
            requestPanelHeightUpdate();

            // Normally, we start listening when the panel is expanded, but here we need to start
            // earlier so the state is already up to date when dragging down.
            setListening(true);
        }
        super.onTouchEvent(event);
        return true;
    }

    private boolean isInScrollLayoutArea(float x, float y) {
        return (x >= mScrollView.getLeft() && x <= mScrollView.getRight()) &&
                (y <= mStackScroller.getBottomMostNotificationBottom()
                         || y <= mScrollLayout.getY() + mScrollLayout.getHeight());
    }

    private void handleScrollLayoutDown(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                && shouldQuickSettingsIntercept(event.getX(), event.getY(), -1)) {
            mScrollLayoutTracking = true;
            onScrollLayoutExpansionStarted();
            mInitialHeightOnTouch = mScrollLayoutExpansionHeight;
            mInitialTouchY = event.getX();
            mInitialTouchX = event.getY();
            // If we interrupt an expansion gesture here, make sure to update the state correctly.
            if (mIsExpanding) {
                onExpandingFinished();
            }
        }
    }

    @Override
    protected boolean flingExpands(float vel, float vectorVel) {
        boolean expands = super.flingExpands(vel, vectorVel);

        // If we are already running a ScrollLayout expansion, make sure that we keep the panel open.
        if (mScrollLayoutExpansionAnimator != null) {
            expands = true;
        }
        return expands;
    }

    @Override
    protected boolean hasConflictingGestures() {
        return false;
    }

    private void onScrollLayoutTouch(MotionEvent event) {
        int pointerIndex = event.findPointerIndex(mTrackingPointer);
        if (pointerIndex < 0) {
            pointerIndex = 0;
            mTrackingPointer = event.getPointerId(pointerIndex);
        }
        final float y = event.getY(pointerIndex);
        final float x = event.getX(pointerIndex);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mScrollLayoutTracking = true;
                mInitialTouchY = y;
                mInitialTouchX = x;
                onScrollLayoutExpansionStarted();
                mInitialHeightOnTouch = mScrollLayoutExpansionHeight;
                initVelocityTracker();
                trackMovement(event);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                final int upPointer = event.getPointerId(event.getActionIndex());
                if (mTrackingPointer == upPointer) {
                    // gesture is ongoing, find a new pointer to track
                    final int newIndex = event.getPointerId(0) != upPointer ? 0 : 1;
                    final float newY = event.getY(newIndex);
                    final float newX = event.getX(newIndex);
                    mTrackingPointer = event.getPointerId(newIndex);
                    mInitialHeightOnTouch = mScrollLayoutExpansionHeight;
                    mInitialTouchY = newY;
                    mInitialTouchX = newX;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                final float h = y - mInitialTouchY;
                setScrollLayoutExpansion(h + mInitialHeightOnTouch);
                trackMovement(event);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mScrollLayoutTracking = false;
                mTrackingPointer = -1;
                trackMovement(event);
                float fraction = getScrollLayoutExpansionFraction();
                if (mEnableOverScroll) {
                    flingSettings(getCurrentVelocity(), false);
                } else if ((fraction != 0f || y >= mInitialTouchY)
                        && (fraction != 1f || y <= mInitialTouchY)) {
                    flingScrollLayoutWithCurrentVelocity();
                } else {
                    mScrollYOverride = -1;
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }
    }

    @Override
    public void onOverscrolled(float lastTouchX, float lastTouchY, int amount) {
        if (mIntercepting && shouldQuickSettingsIntercept(lastTouchX, lastTouchY,
                -1 /* yDiff: Not relevant here */)) {
            onScrollLayoutExpansionStarted(amount);
            mInitialHeightOnTouch = mScrollLayoutExpansionHeight;
            mInitialTouchY = mLastTouchY;
            mInitialTouchX = mLastTouchX;
            mScrollLayoutTracking = true;
        }
    }

    @Override
    public void onOverscrollTopChanged(float amount, boolean isRubberbanded) {
        cancelAnimation();
        if (!mScrollLayoutExpansionEnabled) {
            amount = 0f;
        }
        float rounded = amount >= 1f ? amount : 0f;
        mStackScrollerOverscrolling = rounded != 0f && isRubberbanded;
        mScrollLayoutExpansionFromOverscroll = rounded != 0f;
        mLastOverscroll = rounded;
        updateScrollLayoutState();
        setScrollLayoutExpansion(mScrollLayoutMinExpansionHeight + rounded);
    }

    @Override
    public void flingTopOverscroll(float velocity, boolean open) {
        mLastOverscroll = 0f;
        setScrollLayoutExpansion(mScrollLayoutExpansionHeight);
        flingSettings(!mScrollLayoutExpansionEnabled && open ? 0f : velocity, open && mScrollLayoutExpansionEnabled,
                new Runnable() {
                    @Override
                    public void run() {
                        mStackScrollerOverscrolling = false;
                        mScrollLayoutExpansionFromOverscroll = false;
                        updateScrollLayoutState();
                    }
                });
    }

    private void onScrollLayoutExpansionStarted() {
        onScrollLayoutExpansionStarted(0);
    }

    private void onScrollLayoutExpansionStarted(int overscrollAmount) {
        cancelAnimation();

        // Reset scroll position and apply that position to the expanded height.
        float height = mScrollLayoutExpansionHeight - mScrollView.getScrollY() - overscrollAmount;
        if (mScrollView.getScrollY() != 0) {
            mScrollYOverride = mScrollView.getScrollY();
        }
        mScrollView.scrollTo(0, 0);
        setScrollLayoutExpansion(height);
    }

    public void setBarState(boolean goingToFullShade) {
        updateScrollLayoutState();
        if (goingToFullShade) {
            animateHeaderSlidingIn();
        }
    }

    private void animateHeaderSlidingIn() {
        mHeaderAnimatingIn = true;
        getViewTreeObserver().addOnPreDrawListener(mStartHeaderSlidingIn);

    }

    public void updateScrollLayoutState() {
        mHeader.setVisibility(View.VISIBLE);
        mHeader.setExpanded((mScrollLayoutExpanded && !mStackScrollerOverscrolling));
        mStackScroller.setScrollingEnabled(!mScrollLayoutExpanded || mScrollLayoutExpansionFromOverscroll);
        mScrollView.setTouchEnabled(mScrollLayoutExpanded);
        if (mSCrollLayoutNavbarScrim != null) {
            mSCrollLayoutNavbarScrim
                    .setVisibility(mScrollLayoutExpanded && !mStackScrollerOverscrolling && mScrollScrimEnabled
                    ? View.VISIBLE
                    : View.INVISIBLE);
        }
    }

    private void setScrollLayoutExpansion(float height) {
        height = Math.min(Math.max(height, mScrollLayoutMinExpansionHeight), mScrollLayoutMaxExpansionHeight);
        mScrollLayoutFullyExpanded = height == mScrollLayoutMaxExpansionHeight;
        if (height > mScrollLayoutMinExpansionHeight && !mScrollLayoutExpanded && !mStackScrollerOverscrolling) {
            setScrollLayoutExpanded(true);
        } else if (height <= mScrollLayoutMinExpansionHeight && mScrollLayoutExpanded) {
            setScrollLayoutExpanded(false);
        }
        mScrollLayoutExpansionHeight = height;
        mHeader.setExpansion(getHeaderExpansionFraction());
        setScrollLayoutTranslation(height);
        requestScrollerTopPaddingUpdate(false /* animate */);
        if (mSCrollLayoutNavbarScrim != null && mScrollLayoutExpanded && !mStackScrollerOverscrolling
                && mScrollScrimEnabled) {
            mSCrollLayoutNavbarScrim.setAlpha(getScrollLayoutExpansionFraction());
        }
    }

    private float getHeaderExpansionFraction() {
        return getScrollLayoutExpansionFraction();
    }

    private void setScrollLayoutTranslation(float height) {
        if (!mHeaderAnimatingIn) {
            mScrollLayout.setY(height - mScrollLayout.getHeight() + getHeaderTranslation());
        }
    }

    private float calculateScrollLayoutTopPadding() {
        return mScrollLayoutExpansionHeight;
    }

    private void requestScrollerTopPaddingUpdate(boolean animate) {
        mStackScroller.updateTopPadding(calculateScrollLayoutTopPadding(),
                mScrollView.getScrollY(),
                mAnimateNextTopPaddingChange || animate);
        mAnimateNextTopPaddingChange = false;
    }

    private void trackMovement(MotionEvent event) {
        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(event);
        }
        mLastTouchX = event.getX();
        mLastTouchY = event.getY();
    }

    private void initVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
        }
        mVelocityTracker = VelocityTracker.obtain();
    }

    private float getCurrentVelocity() {
        if (mVelocityTracker == null) {
            return 0;
        }
        mVelocityTracker.computeCurrentVelocity(1000);
        return mVelocityTracker.getYVelocity();
    }

    private void cancelAnimation() {
        if (mScrollLayoutExpansionAnimator != null) {
            mScrollLayoutExpansionAnimator.cancel();
        }
    }

    private void flingSettings(float vel, boolean expand) {
        flingSettings(vel, expand, null);
    }

    private void flingSettings(float vel, boolean expand, final Runnable onFinishRunnable) {
        float target = expand ? mScrollLayoutMaxExpansionHeight : mScrollLayoutMinExpansionHeight;
        if (target == mScrollLayoutExpansionHeight) {
            mScrollYOverride = -1;
            if (onFinishRunnable != null) {
                onFinishRunnable.run();
            }
            return;
        }
        mScrollView.setBlockFlinging(true);
        ValueAnimator animator = ValueAnimator.ofFloat(mScrollLayoutExpansionHeight, target);
        mFlingAnimationUtils.apply(animator, mScrollLayoutExpansionHeight, target, vel);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setScrollLayoutExpansion((Float) animation.getAnimatedValue());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mScrollView.setBlockFlinging(false);
                mScrollYOverride = -1;
                mScrollLayoutExpansionAnimator = null;
                if (onFinishRunnable != null) {
                    onFinishRunnable.run();
                }
            }
        });
        animator.start();
        mScrollLayoutExpansionAnimator = animator;
        mSCrollLayoutAnimatorExpand = expand;
    }

    /**
     * @return Whether we should intercept a gesture to open Quick Settings.
     */
    private boolean shouldQuickSettingsIntercept(float x, float y, float yDiff) {
        if (!mScrollLayoutExpansionEnabled) {
            return false;
        }
        View header = mHeader;
        boolean onHeader = x >= header.getLeft() && x <= header.getRight()
                && y >= header.getTop() && y <= header.getBottom();
        if (mScrollLayoutExpanded) {
            return onHeader || (mScrollView.isScrolledToBottom() && yDiff < 0) && isInScrollLayoutArea(x, y);
        } else {
            return onHeader;
        }
    }

    @Override
    protected boolean isScrolledToBottom() {
        if (!isInSettings()) {
            return mStackScroller.isScrolledToBottom();
        } else {
            return mScrollView.isScrolledToBottom();
        }
    }

    @Override
    protected int getMaxPanelHeight() {
        int min = mStatusBarMinHeight;
        if (mStackScroller.getNotGoneChildCount() == 0) {
            int minHeight = (int) ((mScrollLayoutMinExpansionHeight + getOverExpansionAmount())
                                           * HEADER_RUBBERBAND_FACTOR);
            min = Math.max(min, minHeight);
        }
        int maxHeight;
        if (mTwoFingerScrollLayoutExpand || mScrollLayoutExpanded
                || mIsExpanding && mScrollLayoutExpandedWhenExpandingStarted) {
            maxHeight = Math.max(calculatePanelHeightScrollLayoutExpanded(), calculatePanelHeightShade());
        } else {
            maxHeight = calculatePanelHeightShade();
        }
        maxHeight = Math.max(maxHeight, min);
        return maxHeight;
    }

    private boolean isInSettings() {
        return mScrollLayoutExpanded;
    }

    @Override
    protected void onHeightUpdated(float expandedHeight) {
        if (!mScrollLayoutExpanded) {
            positionClockAndNotifications();
        }
        if (mTwoFingerScrollLayoutExpand || mScrollLayoutExpanded && !mScrollLayoutTracking
                && mScrollLayoutExpansionAnimator == null
                && !mScrollLayoutExpansionFromOverscroll) {
            float panelHeightScrollLayoutCollapsed = mStackScroller.getIntrinsicPadding()
                    + mStackScroller.getMinStackHeight()
                    + mStackScroller.getNotificationTopPadding();
            float panelHeightScrollLayoutExpanded = calculatePanelHeightScrollLayoutExpanded();
            float t = (expandedHeight - panelHeightScrollLayoutCollapsed)
                    / (panelHeightScrollLayoutExpanded - panelHeightScrollLayoutCollapsed);

            setScrollLayoutExpansion(mScrollLayoutMinExpansionHeight
                    + t * (getTempScrollLayoutMaxExpansion() - mScrollLayoutMinExpansionHeight));
        }
        mStackScroller.setStackHeight(expandedHeight);
        updateHeader();
        updateNotificationTranslucency();
        int panelHeight = calculatePanelHeightShade();
        int alpha = (int) ((Math.min(expandedHeight, panelHeight) * 1.0f / panelHeight) * mBackgroundAlpha);
        updateBackgroundColor(this, alpha, mBackgroundColor);
    }

    /**
     * @return a temporary override of {@link #mScrollLayoutMaxExpansionHeight}, which is needed when
     * collapsing ScrollLayout / the panel when ScrollLayout was scrolled
     */
    private int getTempScrollLayoutMaxExpansion() {
        int scrollLayoutTempMaxExpansion = mScrollLayoutMaxExpansionHeight;
        if (mScrollYOverride != -1) {
            scrollLayoutTempMaxExpansion -= mScrollYOverride;
        }
        return scrollLayoutTempMaxExpansion;
    }

    private int calculatePanelHeightShade() {
        int emptyBottomMargin = mStackScroller.getEmptyBottomMargin();
        int maxHeight = mStackScroller.getHeight() - emptyBottomMargin
                - mTopPaddingAdjustment;
        maxHeight += mStackScroller.getTopPaddingOverflow();
        return maxHeight;
    }

    private int calculatePanelHeightScrollLayoutExpanded() {
        float notificationHeight = mStackScroller.getHeight()
                - mStackScroller.getEmptyBottomMargin()
                - mStackScroller.getTopPadding();
        float totalHeight = mScrollLayoutMaxExpansionHeight + notificationHeight
                + mStackScroller.getNotificationTopPadding();
        if (totalHeight > mStackScroller.getHeight()) {
            float fullyCollapsedHeight = mScrollLayoutMaxExpansionHeight
                    + mStackScroller.getMinStackHeight()
                    + mStackScroller.getNotificationTopPadding()
                    - getScrollViewScrollY();
            totalHeight = Math.max(fullyCollapsedHeight, mStackScroller.getHeight());
        }
        return (int) totalHeight;
    }

    private int getScrollViewScrollY() {
        if (mScrollYOverride != -1) {
            return mScrollYOverride;
        } else {
            return mScrollView.getScrollY();
        }
    }

    private void updateNotificationTranslucency() {
        float alpha = (getNotificationsTopY() + mStackScroller.getItemHeight())
                / (mScrollLayoutMinExpansionHeight + mStackScroller.getBottomStackPeekSize()
                           - mStackScroller.getCollapseSecondCardPadding());
        alpha = Math.max(0, Math.min(alpha, 1));
        alpha = (float) Math.pow(alpha, 0.75);
        if (alpha != 1f && mStackScroller.getLayerType() != LAYER_TYPE_HARDWARE) {
            mStackScroller.setLayerType(LAYER_TYPE_HARDWARE, null);
        } else if (alpha == 1f
                && mStackScroller.getLayerType() == LAYER_TYPE_HARDWARE) {
            mStackScroller.setLayerType(LAYER_TYPE_NONE, null);
        }
        mStackScroller.setAlpha(alpha);
    }

    private void updateBackgroundColor(View target, int targetAlpha, int rgb) {
        if (mBackgroundColor == 0) {
            return;
        }
        final int r = Color.red(rgb);
        final int g = Color.green(rgb);
        final int b = Color.blue(rgb);
        target.setBackgroundColor(Color.argb(targetAlpha, r, g, b));
    }

    @Override
    protected float getOverExpansionAmount() {
        return mStackScroller.getCurrentOverScrollAmount(true /* top */);
    }

    @Override
    protected float getOverExpansionPixels() {
        return mStackScroller.getCurrentOverScrolledPixels(true /* top */);
    }

    /**
     * Hides the header when notifications are colliding with it.
     */
    private void updateHeader() {
        updateHeaderShade();
    }

    private void updateHeaderShade() {
        if (!mHeaderAnimatingIn) {
            mHeader.setTranslationY(getHeaderTranslation());
        }
        setScrollLayoutTranslation(mScrollLayoutExpansionHeight);
    }

    private float getHeaderTranslation() {
        if (mStackScroller.getNotGoneChildCount() == 0) {
            if (mExpandedHeight / HEADER_RUBBERBAND_FACTOR >= mScrollLayoutMinExpansionHeight) {
                return 0;
            } else {
                return mExpandedHeight / HEADER_RUBBERBAND_FACTOR - mScrollLayoutMinExpansionHeight;
            }
        }
        return Math.min(0, mStackScroller.getTranslationY()) / HEADER_RUBBERBAND_FACTOR;
    }

    private float getNotificationsTopY() {
        if (mStackScroller.getNotGoneChildCount() == 0) {
            return getExpandedHeight();
        }
        return mStackScroller.getNotificationsTopY();
    }

    @Override
    protected void onExpandingStarted() {
        super.onExpandingStarted();
        mStackScroller.onExpansionStarted();
        mIsExpanding = true;
        mScrollLayoutExpandedWhenExpandingStarted = mScrollLayoutExpanded;
        if (mScrollLayoutExpanded) {
            onScrollLayoutExpansionStarted();
        }
    }

    @Override
    protected void onExpandingFinished() {
        super.onExpandingFinished();
        mStackScroller.onExpansionStopped();
        mIsExpanding = false;
        mScrollYOverride = -1;
        if (mExpandedHeight == 0f) {
            setListening(false);
        } else {
            setListening(true);
        }
        mTwoFingerScrollLayoutExpand = false;
        mTwoFingerScrollLayoutExpandPossible = false;
    }

    private void setListening(boolean listening) {
        mHeader.setListening(listening);
    }

    @Override
    public void instantExpand() {
        super.instantExpand();
        setListening(true);
    }

    @Override
    protected void setOverExpansion(float overExpansion, boolean isPixels) {
        if (mConflictingScrollLayoutExpansionGesture || mTwoFingerScrollLayoutExpand) {
            return;
        }
        mStackScroller.setOnHeightChangedListener(null);
        if (isPixels) {
            mStackScroller.setOverScrolledPixels(
                    overExpansion, true /* onTop */, false /* animate */);
        } else {
            mStackScroller.setOverScrollAmount(
                    overExpansion, true /* onTop */, false /* animate */);
        }
        mStackScroller.setOnHeightChangedListener(this);
    }

    @Override
    protected void onTrackingStarted() {
        super.onTrackingStarted();
        if (mScrollLayoutExpanded) {
            mTwoFingerScrollLayoutExpand = true;
        }
    }

    @Override
    protected void onTrackingStopped(boolean expand) {
        super.onTrackingStopped(expand);
        if (expand) {
            mStackScroller.setOverScrolledPixels(
                    0.0f, true /* onTop */, true /* animate */);
        }
    }

    @Override
    public void onHeightChanged(ExpandableView view) {

        // Block update if we are in quick settings and just the top padding changed
        // (i.e. view == null).
        if (view == null && mScrollLayoutExpanded) {
            return;
        }
        requestPanelHeightUpdate();
    }

    @Override
    public void onReset(ExpandableView view) {
    }

    @Override
    public void onScrollChanged() {
        if (mScrollLayoutExpanded) {
            requestScrollerTopPaddingUpdate(false /* animate */);
            requestPanelHeightUpdate();
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View v) {
        if (v == mHeader) {
            onScrollLayoutExpansionStarted();
            if (mScrollLayoutExpanded) {
                flingSettings(0 /* vel */, false /* expand */);
            } else if (mScrollLayoutExpansionEnabled) {
                flingSettings(0 /* vel */, true /* expand */);
            }
        }
    }

    @Override
    protected void onEdgeClicked(boolean right) {

    }

    @Override
    protected void startUnlockHintAnimation() {
        super.startUnlockHintAnimation();
    }

    @Override
    protected float getPeekHeight() {
        if (mStackScroller.getNotGoneChildCount() > 0) {
            return mStackScroller.getPeekHeight();
        } else {
            return mScrollLayoutMinExpansionHeight * HEADER_RUBBERBAND_FACTOR;
        }
    }

    @Override
    protected float getCannedFlingDurationFactor() {
        if (mScrollLayoutExpanded) {
            return 0.7f;
        } else {
            return 0.6f;
        }
    }

    @Override
    protected boolean isTrackingBlocked() {
        return mConflictingScrollLayoutExpansionGesture && mScrollLayoutExpanded;
    }

    public boolean isScrollLayoutExpanded() {
        return mScrollLayoutExpanded;
    }

    private void setScrollLayoutExpanded(boolean expanded) {
        boolean changed = mScrollLayoutExpanded != expanded;
        if (changed) {
            mScrollLayoutExpanded = expanded;
            updateScrollLayoutState();
            requestPanelHeightUpdate();
            mStackScroller.setInterceptDelegateEnabled(expanded);
        }
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public void setScrollLayoutScrimEnabled(boolean scrollLayoutScrimEnabled) {
        boolean changed = mScrollScrimEnabled != scrollLayoutScrimEnabled;
        mScrollScrimEnabled = scrollLayoutScrimEnabled;
        if (changed) {
            updateScrollLayoutState();
        }
    }

    public void setEnableOverScroll(boolean enableOverScroll) {
        this.mEnableOverScroll = enableOverScroll;
        mStackScroller.setEnableOverScroll(mEnableOverScroll);
    }

    public boolean isExpanding() {
        return mIsExpanding;
    }
}
