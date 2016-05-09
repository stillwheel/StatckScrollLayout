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

    private static final int TAG_KEY_ANIM = R.id.scrim;
    private static final long DOZE_BACKGROUND_ANIM_DURATION = 220;

    private StackHeaderView mHeader;
    private final Runnable mUpdateHeader = new Runnable() {
        @Override
        public void run() {
            mHeader.updateEverything();
        }
    };
    private View mQsContainer;
    private ObservableScrollView mScrollView;
    private View mQsNavbarScrim;
    private StackScrollLayout mStackScroller;
    private int mNotificationTopPadding;
    private boolean mAnimateNextTopPaddingChange;
    private int mTrackingPointer;
    private VelocityTracker mVelocityTracker;
    private boolean mQsTracking;
    private int mBackgroundColor;
    private int mBackgroundAlpha;
    /**
     * If set, the ongoing touch gesture might both trigger the expansion in {@link PanelView} and
     * the expansion for quick settings.
     */
    private boolean mConflictingQsExpansionGesture;
    /**
     * Whether we are currently handling a motion gesture in #onInterceptTouchEvent, but haven't
     * intercepted yet.
     */
    private boolean mIntercepting;
    private boolean mQsExpanded;
    private boolean mQsExpandedWhenExpandingStarted;
    private boolean mQsFullyExpanded;
    private float mInitialHeightOnTouch;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private float mLastTouchX;
    private float mLastTouchY;
    private float mQsExpansionHeight;
    private int mQsMinExpansionHeight;
    private int mQsMaxExpansionHeight;
    private int mQsPeekHeight;
    private boolean mStackScrollerOverscrolling;
    private boolean mQsExpansionFromOverscroll;
    private float mLastOverscroll;
    private boolean mQsExpansionEnabled = true;
    private ValueAnimator mQsExpansionAnimator;
    private FlingAnimationUtils mFlingAnimationUtils;
    private int mStatusBarMinHeight;
    private Interpolator mFastOutSlowInInterpolator;
    private int mTopPaddingAdjustment;
    private boolean mIsExpanding;
    private boolean mBlockTouches;
    private boolean mTwoFingerQsExpand;
    private boolean mTwoFingerQsExpandPossible;
    private boolean mEnableOverScroll;
    /**
     * If we are in a panel collapsing motion, we reset scrollY of our scroll view but still
     * need to take this into account in our panel height calculation.
     */
    private int mScrollYOverride = -1;
    private boolean mQsAnimatorExpand;
    private boolean mOnlyAffordanceInThisMotion;
    private boolean mHeaderAnimatingIn;
    private ObjectAnimator mQsContainerAnimator;
    private final View.OnLayoutChangeListener mQsContainerAnimatorUpdater
            = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                                   int oldTop, int oldRight, int oldBottom) {
            int oldHeight = oldBottom - oldTop;
            int height = bottom - top;
            if (height != oldHeight && mQsContainerAnimator != null) {
                PropertyValuesHolder[] values = mQsContainerAnimator.getValues();
                float newEndValue = mHeader.getCollapsedHeight() + mQsPeekHeight - height - top;
                float newStartValue = -height - top;
                values[0].setFloatValues(newStartValue, newEndValue);
                mQsContainerAnimator.setCurrentPlayTime(mQsContainerAnimator.getCurrentPlayTime());
            }
        }
    };
    private final Animator.AnimatorListener mAnimateHeaderSlidingInListener
            = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            mHeaderAnimatingIn = false;
            mQsContainerAnimator = null;
            mQsContainer.removeOnLayoutChangeListener(mQsContainerAnimatorUpdater);
        }
    };
    private final ViewTreeObserver.OnPreDrawListener mStartHeaderSlidingIn
            = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            getViewTreeObserver().removeOnPreDrawListener(this);
            mHeader.setTranslationY(-mHeader.getCollapsedHeight() - mQsPeekHeight);
            mHeader.animate()
                    .translationY(0f)
                    .setStartDelay(300)
                    .setDuration(StackStateAnimator.ANIMATION_DURATION_GO_TO_FULL_SHADE)
                    .setInterpolator(mFastOutSlowInInterpolator)
                    .start();
            mQsContainer.setY(-mQsContainer.getHeight());
            mQsContainerAnimator = ObjectAnimator.ofFloat(mQsContainer, View.TRANSLATION_Y,
                    mQsContainer.getTranslationY(),
                    mHeader.getCollapsedHeight() + mQsPeekHeight - mQsContainer.getHeight()
                            - mQsContainer.getTop());
            mQsContainerAnimator.setStartDelay(300);
            mQsContainerAnimator.setDuration(StackStateAnimator.ANIMATION_DURATION_GO_TO_FULL_SHADE);
            mQsContainerAnimator.setInterpolator(mFastOutSlowInInterpolator);
            mQsContainerAnimator.addListener(mAnimateHeaderSlidingInListener);
            mQsContainerAnimator.start();
            mQsContainer.addOnLayoutChangeListener(mQsContainerAnimatorUpdater);
            return true;
        }
    };
    private boolean mQsScrimEnabled = true;

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
        mQsContainer = findViewById(R.id.quick_settings_container);
        mScrollView = (ObservableScrollView) findViewById(R.id.scroll_view);
        if (mQsContainer == null) {
            mQsContainer = new RelativeLayout(getContext());
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

        mQsNavbarScrim = findViewById(R.id.qs_navbar_scrim);

        // recompute internal state when qspanel height changes
        mQsContainer.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
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
        ColorDrawable drawable = (ColorDrawable) getBackground();
        mBackgroundColor = drawable.getColor();
        mBackgroundAlpha = Color.alpha(mBackgroundColor);
        setBackgroundColor(0);
        updateResources();
        updateQsState();
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
        mQsPeekHeight = getResources().getDimensionPixelSize(R.dimen.qs_peek_height);
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
        mQsMinExpansionHeight = mHeader.getCollapsedHeight() + mQsPeekHeight;
        mQsMaxExpansionHeight = mHeader.getExpandedHeight() + mQsContainer.getHeight();
        positionClockAndNotifications();
        if (mQsExpanded) {
            if (mQsFullyExpanded) {
                mQsExpansionHeight = mQsMaxExpansionHeight;
                requestScrollerTopPaddingUpdate(false /* animate */);
            }
        } else {
            setQsExpansion(mQsMinExpansionHeight + mLastOverscroll);
            mStackScroller.setStackHeight(getExpandedHeight());
            updateHeader();
        }
        mStackScroller.updateIsSmallScreen(
                mHeader.getCollapsedHeight() + mQsPeekHeight);
    }

    /**
     * Positions the clock and notifications dynamically depending on how many notifications are
     * showing.
     */
    private void positionClockAndNotifications() {
        boolean animate = mStackScroller.isAddOrRemoveAnimationPending();
        int stackScrollerPadding = 0;
        int bottom = mHeader.getCollapsedHeight();
        stackScrollerPadding = bottom + mQsPeekHeight + mNotificationTopPadding;
        mTopPaddingAdjustment = 0;
        mStackScroller.setIntrinsicPadding(stackScrollerPadding);
        requestScrollerTopPaddingUpdate(animate);
    }

    public void animateToFullShade(long delay) {
        mAnimateNextTopPaddingChange = true;
        mStackScroller.goToFullShade(delay);
        requestLayout();
    }

    public void setQsExpansionEnabled(boolean qsExpansionEnabled) {
        mQsExpansionEnabled = qsExpansionEnabled;
        mHeader.setClickable(qsExpansionEnabled);
    }

    @Override
    public void resetViews() {
        mBlockTouches = false;
        closeQs();
        mStackScroller.setOverScrollAmount(0f, true /* onTop */, false /* animate */,
                true /* cancelAnimators */);
    }

    public void closeQs() {
        cancelAnimation();
        setQsExpansion(mQsMinExpansionHeight);
    }

    public void animateCloseQs() {
        if (mQsExpansionAnimator != null) {
            if (!mQsAnimatorExpand) {
                return;
            }
            float height = mQsExpansionHeight;
            mQsExpansionAnimator.cancel();
            setQsExpansion(height);
        }
        flingSettings(0 /* vel */, false);
    }

    public void openQs() {
        cancelAnimation();
        if (mQsExpansionEnabled) {
            setQsExpansion(mQsMaxExpansionHeight);
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
                if (mQsExpansionAnimator != null) {
                    onQsExpansionStarted();
                    mInitialHeightOnTouch = mQsExpansionHeight;
                    mQsTracking = true;
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
                if (mQsTracking) {
                    // Already tracking because onOverscrolled was called. We need to update here
                    // so we don't stop for a frame until the next touch event gets handled in
                    // onTouchEvent.
                    setQsExpansion(h + mInitialHeightOnTouch);
                    trackMovement(event);
                    mIntercepting = false;
                    return true;
                }
                if (Math.abs(h) > mTouchSlop && Math.abs(h) > Math.abs(x - mInitialTouchX)
                        && shouldQuickSettingsIntercept(mInitialTouchX, mInitialTouchY, h)) {
                    onQsExpansionStarted();
                    mInitialHeightOnTouch = mQsExpansionHeight;
                    mInitialTouchY = y;
                    mInitialTouchX = x;
                    mQsTracking = true;
                    mIntercepting = false;
                    mStackScroller.removeLongPressCallback();
                    return true;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                trackMovement(event);
                if (mQsTracking) {
                    flingQsWithCurrentVelocity();
                    mQsTracking = false;
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
        // scrolling when QS is expanded.
        if (mScrollView.isHandlingTouchEvent()) {
            return;
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    private void flingQsWithCurrentVelocity() {
        float vel = getCurrentVelocity();
        flingSettings(vel, flingExpandsQs(vel));
    }

    private boolean flingExpandsQs(float vel) {
        if (Math.abs(vel) < mFlingAnimationUtils.getMinVelocityPxPerSecond()) {
            return getQsExpansionFraction() > 0.5f;
        } else {
            return vel > 0;
        }
    }

    private float getQsExpansionFraction() {
        return Math.min(1f, (mQsExpansionHeight - mQsMinExpansionHeight)
                / (getTempQsMaxExpansion() - mQsMinExpansionHeight));
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
                && !mQsExpanded && mQsExpansionEnabled) {

            // Down in the empty area while fully expanded - go to QS.
            mQsTracking = true;
            mConflictingQsExpansionGesture = true;
            onQsExpansionStarted();
            mInitialHeightOnTouch = mQsExpansionHeight;
            mInitialTouchY = event.getX();
            mInitialTouchX = event.getY();
        }
        if (mExpandedHeight != 0) {
            handleQsDown(event);
        }
        if (!mTwoFingerQsExpand && mQsTracking) {
            onQsTouch(event);
            if (!mConflictingQsExpansionGesture) {
                return true;
            }
        }
        if (event.getActionMasked() == MotionEvent.ACTION_CANCEL
                || event.getActionMasked() == MotionEvent.ACTION_UP) {
            mConflictingQsExpansionGesture = false;
        }
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN && mExpandedHeight == 0
                && mQsExpansionEnabled) {
            mTwoFingerQsExpandPossible = true;
        }
        if (mTwoFingerQsExpandPossible && event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN
                && event.getPointerCount() == 2
                && event.getY(event.getActionIndex()) < mStatusBarMinHeight) {
            mTwoFingerQsExpand = true;
            requestPanelHeightUpdate();

            // Normally, we start listening when the panel is expanded, but here we need to start
            // earlier so the state is already up to date when dragging down.
            setListening(true);
        }
        super.onTouchEvent(event);
        return true;
    }

    private boolean isInQsArea(float x, float y) {
        return (x >= mScrollView.getLeft() && x <= mScrollView.getRight()) &&
                (y <= mStackScroller.getBottomMostNotificationBottom()
                         || y <= mQsContainer.getY() + mQsContainer.getHeight());
    }

    private void handleQsDown(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                && shouldQuickSettingsIntercept(event.getX(), event.getY(), -1)) {
            mQsTracking = true;
            onQsExpansionStarted();
            mInitialHeightOnTouch = mQsExpansionHeight;
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

        // If we are already running a QS expansion, make sure that we keep the panel open.
        if (mQsExpansionAnimator != null) {
            expands = true;
        }
        return expands;
    }

    @Override
    protected boolean hasConflictingGestures() {
        return false;
    }

    private void onQsTouch(MotionEvent event) {
        int pointerIndex = event.findPointerIndex(mTrackingPointer);
        if (pointerIndex < 0) {
            pointerIndex = 0;
            mTrackingPointer = event.getPointerId(pointerIndex);
        }
        final float y = event.getY(pointerIndex);
        final float x = event.getX(pointerIndex);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mQsTracking = true;
                mInitialTouchY = y;
                mInitialTouchX = x;
                onQsExpansionStarted();
                mInitialHeightOnTouch = mQsExpansionHeight;
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
                    mInitialHeightOnTouch = mQsExpansionHeight;
                    mInitialTouchY = newY;
                    mInitialTouchX = newX;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                final float h = y - mInitialTouchY;
                setQsExpansion(h + mInitialHeightOnTouch);
                trackMovement(event);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mQsTracking = false;
                mTrackingPointer = -1;
                trackMovement(event);
                float fraction = getQsExpansionFraction();
                if (mEnableOverScroll) {
                    flingSettings(getCurrentVelocity(), false);
                } else if ((fraction != 0f || y >= mInitialTouchY)
                        && (fraction != 1f || y <= mInitialTouchY)) {
                    flingQsWithCurrentVelocity();
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
            onQsExpansionStarted(amount);
            mInitialHeightOnTouch = mQsExpansionHeight;
            mInitialTouchY = mLastTouchY;
            mInitialTouchX = mLastTouchX;
            mQsTracking = true;
        }
    }

    @Override
    public void onOverscrollTopChanged(float amount, boolean isRubberbanded) {
        cancelAnimation();
        if (!mQsExpansionEnabled) {
            amount = 0f;
        }
        float rounded = amount >= 1f ? amount : 0f;
        mStackScrollerOverscrolling = rounded != 0f && isRubberbanded;
        mQsExpansionFromOverscroll = rounded != 0f;
        mLastOverscroll = rounded;
        updateQsState();
        setQsExpansion(mQsMinExpansionHeight + rounded);
    }

    @Override
    public void flingTopOverscroll(float velocity, boolean open) {
        mLastOverscroll = 0f;
        setQsExpansion(mQsExpansionHeight);
        flingSettings(!mQsExpansionEnabled && open ? 0f : velocity, open && mQsExpansionEnabled,
                new Runnable() {
                    @Override
                    public void run() {
                        mStackScrollerOverscrolling = false;
                        mQsExpansionFromOverscroll = false;
                        updateQsState();
                    }
                });
    }

    private void onQsExpansionStarted() {
        onQsExpansionStarted(0);
    }

    private void onQsExpansionStarted(int overscrollAmount) {
        cancelAnimation();

        // Reset scroll position and apply that position to the expanded height.
        float height = mQsExpansionHeight - mScrollView.getScrollY() - overscrollAmount;
        if (mScrollView.getScrollY() != 0) {
            mScrollYOverride = mScrollView.getScrollY();
        }
        mScrollView.scrollTo(0, 0);
        setQsExpansion(height);
    }

    public void setBarState(boolean goingToFullShade) {
        updateQsState();
        if (goingToFullShade) {
            animateHeaderSlidingIn();
        }
    }

    private void animateHeaderSlidingIn() {
        mHeaderAnimatingIn = true;
        getViewTreeObserver().addOnPreDrawListener(mStartHeaderSlidingIn);

    }

    public void updateQsState() {
        mHeader.setVisibility(View.VISIBLE);
        mHeader.setExpanded((mQsExpanded && !mStackScrollerOverscrolling));
        mStackScroller.setScrollingEnabled(!mQsExpanded || mQsExpansionFromOverscroll);
        mScrollView.setTouchEnabled(mQsExpanded);
        if (mQsNavbarScrim != null) {
            mQsNavbarScrim.setVisibility(mQsExpanded && !mStackScrollerOverscrolling && mQsScrimEnabled
                    ? View.VISIBLE
                    : View.INVISIBLE);
        }
    }

    private void setQsExpansion(float height) {
        height = Math.min(Math.max(height, mQsMinExpansionHeight), mQsMaxExpansionHeight);
        mQsFullyExpanded = height == mQsMaxExpansionHeight;
        if (height > mQsMinExpansionHeight && !mQsExpanded && !mStackScrollerOverscrolling) {
            setQsExpanded(true);
        } else if (height <= mQsMinExpansionHeight && mQsExpanded) {
            setQsExpanded(false);
        }
        mQsExpansionHeight = height;
        mHeader.setExpansion(getHeaderExpansionFraction());
        setQsTranslation(height);
        requestScrollerTopPaddingUpdate(false /* animate */);
        if (mQsNavbarScrim != null && mQsExpanded && !mStackScrollerOverscrolling && mQsScrimEnabled) {
            mQsNavbarScrim.setAlpha(getQsExpansionFraction());
        }
    }

    private float getHeaderExpansionFraction() {
        return getQsExpansionFraction();
    }

    private void setQsTranslation(float height) {
        if (!mHeaderAnimatingIn) {
            mQsContainer.setY(height - mQsContainer.getHeight() + getHeaderTranslation());
        }
    }

    private float calculateQsTopPadding() {
        return mQsExpansionHeight;
    }

    private void requestScrollerTopPaddingUpdate(boolean animate) {
        mStackScroller.updateTopPadding(calculateQsTopPadding(),
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
        if (mQsExpansionAnimator != null) {
            mQsExpansionAnimator.cancel();
        }
    }

    private void flingSettings(float vel, boolean expand) {
        flingSettings(vel, expand, null);
    }

    private void flingSettings(float vel, boolean expand, final Runnable onFinishRunnable) {
        float target = expand ? mQsMaxExpansionHeight : mQsMinExpansionHeight;
        if (target == mQsExpansionHeight) {
            mScrollYOverride = -1;
            if (onFinishRunnable != null) {
                onFinishRunnable.run();
            }
            return;
        }
        mScrollView.setBlockFlinging(true);
        ValueAnimator animator = ValueAnimator.ofFloat(mQsExpansionHeight, target);
        mFlingAnimationUtils.apply(animator, mQsExpansionHeight, target, vel);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setQsExpansion((Float) animation.getAnimatedValue());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mScrollView.setBlockFlinging(false);
                mScrollYOverride = -1;
                mQsExpansionAnimator = null;
                if (onFinishRunnable != null) {
                    onFinishRunnable.run();
                }
            }
        });
        animator.start();
        mQsExpansionAnimator = animator;
        mQsAnimatorExpand = expand;
    }

    /**
     * @return Whether we should intercept a gesture to open Quick Settings.
     */
    private boolean shouldQuickSettingsIntercept(float x, float y, float yDiff) {
        if (!mQsExpansionEnabled) {
            return false;
        }
        View header = mHeader;
        boolean onHeader = x >= header.getLeft() && x <= header.getRight()
                && y >= header.getTop() && y <= header.getBottom();
        if (mQsExpanded) {
            return onHeader || (mScrollView.isScrolledToBottom() && yDiff < 0) && isInQsArea(x, y);
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
            int minHeight = (int) ((mQsMinExpansionHeight + getOverExpansionAmount())
                                           * HEADER_RUBBERBAND_FACTOR);
            min = Math.max(min, minHeight);
        }
        int maxHeight;
        if (mTwoFingerQsExpand || mQsExpanded || mIsExpanding && mQsExpandedWhenExpandingStarted) {
            maxHeight = Math.max(calculatePanelHeightQsExpanded(), calculatePanelHeightShade());
        } else {
            maxHeight = calculatePanelHeightShade();
        }
        maxHeight = Math.max(maxHeight, min);
        return maxHeight;
    }

    private boolean isInSettings() {
        return mQsExpanded;
    }

    @Override
    protected void onHeightUpdated(float expandedHeight) {
        if (!mQsExpanded) {
            positionClockAndNotifications();
        }
        if (mTwoFingerQsExpand || mQsExpanded && !mQsTracking && mQsExpansionAnimator == null
                && !mQsExpansionFromOverscroll) {
            float panelHeightQsCollapsed = mStackScroller.getIntrinsicPadding()
                    + mStackScroller.getMinStackHeight()
                    + mStackScroller.getNotificationTopPadding();
            float panelHeightQsExpanded = calculatePanelHeightQsExpanded();
            float t = (expandedHeight - panelHeightQsCollapsed)
                    / (panelHeightQsExpanded - panelHeightQsCollapsed);

            setQsExpansion(mQsMinExpansionHeight
                    + t * (getTempQsMaxExpansion() - mQsMinExpansionHeight));
        }
        mStackScroller.setStackHeight(expandedHeight);
        updateHeader();
        updateNotificationTranslucency();
        int panelHeight = calculatePanelHeightShade();
        int alpha = (int) ((Math.min(expandedHeight, panelHeight) * 1.0f / panelHeight) * mBackgroundAlpha);
        updateBackgroundColor(this, alpha, mBackgroundColor);
    }

    /**
     * @return a temporary override of {@link #mQsMaxExpansionHeight}, which is needed when
     * collapsing QS / the panel when QS was scrolled
     */
    private int getTempQsMaxExpansion() {
        int qsTempMaxExpansion = mQsMaxExpansionHeight;
        if (mScrollYOverride != -1) {
            qsTempMaxExpansion -= mScrollYOverride;
        }
        return qsTempMaxExpansion;
    }

    private int calculatePanelHeightShade() {
        int emptyBottomMargin = mStackScroller.getEmptyBottomMargin();
        int maxHeight = mStackScroller.getHeight() - emptyBottomMargin
                - mTopPaddingAdjustment;
        maxHeight += mStackScroller.getTopPaddingOverflow();
        return maxHeight;
    }

    private int calculatePanelHeightQsExpanded() {
        float notificationHeight = mStackScroller.getHeight()
                - mStackScroller.getEmptyBottomMargin()
                - mStackScroller.getTopPadding();
        float totalHeight = mQsMaxExpansionHeight + notificationHeight
                + mStackScroller.getNotificationTopPadding();
        if (totalHeight > mStackScroller.getHeight()) {
            float fullyCollapsedHeight = mQsMaxExpansionHeight
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
                / (mQsMinExpansionHeight + mStackScroller.getBottomStackPeekSize()
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
        setQsTranslation(mQsExpansionHeight);
    }

    private float getHeaderTranslation() {
        if (mStackScroller.getNotGoneChildCount() == 0) {
            if (mExpandedHeight / HEADER_RUBBERBAND_FACTOR >= mQsMinExpansionHeight) {
                return 0;
            } else {
                return mExpandedHeight / HEADER_RUBBERBAND_FACTOR - mQsMinExpansionHeight;
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
        mQsExpandedWhenExpandingStarted = mQsExpanded;
        if (mQsExpanded) {
            onQsExpansionStarted();
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
        mTwoFingerQsExpand = false;
        mTwoFingerQsExpandPossible = false;
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
        if (mConflictingQsExpansionGesture || mTwoFingerQsExpand) {
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
        if (mQsExpanded) {
            mTwoFingerQsExpand = true;
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
        if (view == null && mQsExpanded) {
            return;
        }
        requestPanelHeightUpdate();
    }

    @Override
    public void onReset(ExpandableView view) {
    }

    @Override
    public void onScrollChanged() {
        if (mQsExpanded) {
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
            onQsExpansionStarted();
            if (mQsExpanded) {
                flingSettings(0 /* vel */, false /* expand */);
            } else if (mQsExpansionEnabled) {
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
            return mQsMinExpansionHeight * HEADER_RUBBERBAND_FACTOR;
        }
    }

    @Override
    protected float getCannedFlingDurationFactor() {
        if (mQsExpanded) {
            return 0.7f;
        } else {
            return 0.6f;
        }
    }

    @Override
    protected boolean isTrackingBlocked() {
        return mConflictingQsExpansionGesture && mQsExpanded;
    }

    public boolean isQsExpanded() {
        return mQsExpanded;
    }

    private void setQsExpanded(boolean expanded) {
        boolean changed = mQsExpanded != expanded;
        if (changed) {
            mQsExpanded = expanded;
            updateQsState();
            requestPanelHeightUpdate();
            mStackScroller.setInterceptDelegateEnabled(expanded);
        }
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public void setQsScrimEnabled(boolean qsScrimEnabled) {
        boolean changed = mQsScrimEnabled != qsScrimEnabled;
        mQsScrimEnabled = qsScrimEnabled;
        if (changed) {
            updateQsState();
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
