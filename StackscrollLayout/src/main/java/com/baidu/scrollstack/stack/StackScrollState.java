package com.baidu.scrollstack.stack;

import java.util.HashMap;
import java.util.Map;

import com.baidu.scrollstack.uitl.Define;
import com.baidu.scrollstack.view.ExpandableRowView;
import com.baidu.scrollstack.view.ExpandableView;
import com.baidu.scrollstack.R;;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class StackScrollState {

    private static final String CHILD_NOT_FOUND_TAG = "StackScrollStateNoSuchChild";

    private final ViewGroup mHostView;
    private Map<ExpandableView, ViewState> mStateMap;
    private final Rect mClipRect = new Rect();
    private final int mClearAllTopPadding;

    public StackScrollState(ViewGroup hostView) {
        mHostView = hostView;
        mStateMap = new HashMap<ExpandableView, ViewState>();
        mClearAllTopPadding = hostView.getContext().getResources().getDimensionPixelSize(
                R.dimen.clear_all_padding_top);
    }

    public ViewGroup getHostView() {
        return mHostView;
    }

    public void resetViewStates() {
        int numChildren = mHostView.getChildCount();
        for (int i = 0; i < numChildren; i++) {
            ExpandableView child = (ExpandableView) mHostView.getChildAt(i);
            ViewState viewState = mStateMap.get(child);
            if (viewState == null) {
                viewState = new ViewState();
                mStateMap.put(child, viewState);
            }
            // initialize with the default values of the view
            viewState.height = child.getIntrinsicHeight();
            viewState.gone = child.getVisibility() == View.GONE;
            viewState.alpha = 1;
            viewState.notGoneIndex = -1;
        }
    }

    public ViewState getViewStateForView(View requestedView) {
        return mStateMap.get(requestedView);
    }

    public void removeViewStateForView(View child) {
        mStateMap.remove(child);
    }

    /**
     * Apply the properties saved in {@link #mStateMap} to the children of the {@link #mHostView}.
     * The properties are only applied if they effectively changed.
     */
    public void apply() {
        int numChildren = mHostView.getChildCount();
        for (int i = 0; i < numChildren; i++) {
            ExpandableView child = (ExpandableView) mHostView.getChildAt(i);
            ViewState state = mStateMap.get(child);
            if (state == null) {
                Log.wtf(CHILD_NOT_FOUND_TAG, "No child state was found when applying this state " +
                        "to the hostView");
                continue;
            }
            if (!state.gone) {
                float alpha = child.getAlpha();
                float yTranslation = child.getTranslationY();
                float xTranslation = child.getTranslationX();
                float zTranslation = child.getTranslationZ();
                float scale = child.getScaleX();
                int height = child.getActualHeight();
                float newAlpha = state.alpha;
                float newYTranslation = state.yTranslation;
                float newZTranslation = state.zTranslation;
                float newScale = state.scale;
                int newHeight = state.height;
                boolean becomesInvisible = newAlpha == 0.0f;
                if (alpha != newAlpha && xTranslation == 0) {
                    // apply layer type
                    boolean becomesFullyVisible = newAlpha == 1.0f;
                    boolean newLayerTypeIsHardware = !becomesInvisible && !becomesFullyVisible;
                    int layerType = child.getLayerType();
                    int newLayerType = newLayerTypeIsHardware
                            ? View.LAYER_TYPE_HARDWARE
                            : View.LAYER_TYPE_NONE;
                    if (layerType != newLayerType) {
                        child.setLayerType(newLayerType, null);
                    }

                    // apply alpha
                    if (!becomesInvisible) {
                        child.setAlpha(newAlpha);
                    }
                }

                // apply visibility
                int oldVisibility = child.getVisibility();
                int newVisibility = becomesInvisible ? View.INVISIBLE : View.VISIBLE;
                if (newVisibility != oldVisibility) {
                    child.setVisibility(newVisibility);
                }

                // apply yTranslation
                if (yTranslation != newYTranslation) {
                    child.setTranslationY(newYTranslation);
                }

                // apply zTranslation
                if (zTranslation != newZTranslation) {
                    child.setTranslationZ(newZTranslation);
                }

                // apply scale
                if (scale != newScale) {
                    child.setScaleX(newScale);
                    child.setScaleY(newScale);
                }

                // apply height
                if (height != newHeight) {
                    child.setActualHeight(newHeight, false /* notifyListeners */);
                }

                // apply dimming
                child.setDimmed(state.dimmed, false /* animate */);

                // apply dark
                child.setDark(state.dark, false /* animate */);

                // apply hiding sensitive
                child.setHideSensitive(
                        state.hideSensitive, false /* animated */, 0 /* delay */, 0 /* duration */);

                // apply clipping
                float oldClipTopAmount = child.getClipTopAmount();
                if (oldClipTopAmount != state.clipTopAmount) {
                    child.setClipTopAmount(state.clipTopAmount);
                }
                updateChildClip(child, newHeight, state.topOverLap);

                if (child instanceof ExpandableRowView) {
                    ExpandableRowView expandableRowView = (ExpandableRowView) child;
                    boolean visible = state.topOverLap < mClearAllTopPadding;
                    expandableRowView.performVisibilityAnimation(visible && !expandableRowView.willBeGone());
                }

            }
        }
    }

    /**
     * Updates the clipping of a view
     *
     * @param child the view to update
     * @param height the currently applied height of the view
     * @param clipInset how much should this view be clipped from the top
     */
    private void updateChildClip(View child, int height, int clipInset) {
        mClipRect.set(0,
                clipInset,
                child.getWidth(),
                height);
        if (Define.SDK_INT >= 18) {
            child.setClipBounds(mClipRect);
        }
    }

    private View getNextChildNotGone(int childIndex) {
        int childCount = mHostView.getChildCount();
        for (int i = childIndex + 1; i < childCount; i++) {
            View child = mHostView.getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                return child;
            }
        }
        return null;
    }

    public static class ViewState {

        // These are flags such that we can create masks for filtering.

        public static final int LOCATION_UNKNOWN = 0x00;
        public static final int LOCATION_FIRST_CARD = 0x01;
        public static final int LOCATION_TOP_STACK_HIDDEN = 0x02;
        public static final int LOCATION_TOP_STACK_PEEKING = 0x04;
        public static final int LOCATION_MAIN_AREA = 0x08;
        public static final int LOCATION_BOTTOM_STACK_PEEKING = 0x10;
        public static final int LOCATION_BOTTOM_STACK_HIDDEN = 0x20;

        float alpha;
        float yTranslation;
        float zTranslation;
        int height;
        boolean gone;
        float scale;
        boolean dimmed;
        boolean dark;
        boolean hideSensitive;

        /**
         * The amount which the view should be clipped from the top. This is calculated to
         * perceive consistent shadows.
         */
        int clipTopAmount;

        /**
         * How much does the child overlap with the previous view on the top? Can be used for
         * a clipping optimization
         */
        int topOverLap;

        /**
         * The index of the view, only accounting for views not equal to GONE
         */
        int notGoneIndex;

        /**
         * The location this view is currently rendered at.
         *
         * <p>See <code>LOCATION_</code> flags.</p>
         */
        int location;

        @Override
        public String toString() {
            return "ViewState{" +
                    "alpha=" + alpha +
                    ", yTranslation=" + yTranslation +
                    ", zTranslation=" + zTranslation +
                    ", height=" + height +
                    ", gone=" + gone +
                    ", scale=" + scale +
                    ", dimmed=" + dimmed +
                    ", dark=" + dark +
                    ", hideSensitive=" + hideSensitive +
                    ", clipTopAmount=" + clipTopAmount +
                    ", topOverLap=" + topOverLap +
                    ", notGoneIndex=" + notGoneIndex +
                    ", location=" + location +
                    '}';
        }
    }
}
