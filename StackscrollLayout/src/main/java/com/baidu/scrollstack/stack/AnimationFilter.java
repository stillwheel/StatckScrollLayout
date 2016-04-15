package com.baidu.scrollstack.stack;

import java.util.ArrayList;

/**
 * Filters the animations for only a certain type of properties.
 */
public class AnimationFilter {
    boolean animateAlpha;
    boolean animateY;
    boolean animateZ;
    boolean animateScale;
    boolean animateHeight;
    boolean animateTopInset;
    boolean animateDimmed;
    boolean animateDark;
    boolean animateHideSensitive;
    boolean hasDelays;
    boolean hasGoToFullShadeEvent;

    public AnimationFilter animateAlpha() {
        animateAlpha = true;
        return this;
    }

    public AnimationFilter animateY() {
        animateY = true;
        return this;
    }

    public AnimationFilter hasDelays() {
        hasDelays = true;
        return this;
    }

    public AnimationFilter animateZ() {
        animateZ = true;
        return this;
    }

    public AnimationFilter animateScale() {
        animateScale = true;
        return this;
    }

    public AnimationFilter animateHeight() {
        animateHeight = true;
        return this;
    }

    public AnimationFilter animateTopInset() {
        animateTopInset = true;
        return this;
    }

    public AnimationFilter animateDimmed() {
        animateDimmed = true;
        return this;
    }

    public AnimationFilter animateDark() {
        animateDark = true;
        return this;
    }

    public AnimationFilter animateHideSensitive() {
        animateHideSensitive = true;
        return this;
    }

    /**
     * Combines multiple filters into {@code this} filter, using or as the operand .
     *
     * @param events The animation events from the filters to combine.
     */
    public void applyCombination(ArrayList<StackScrollLayout.AnimationEvent> events) {
        reset();
        int size = events.size();
        for (int i = 0; i < size; i++) {
            combineFilter(events.get(i).filter);
            if (events.get(i).animationType ==
                    StackScrollLayout.AnimationEvent.ANIMATION_TYPE_GO_TO_FULL_SHADE) {
                hasGoToFullShadeEvent = true;
            }
        }
    }

    private void combineFilter(AnimationFilter filter) {
        animateAlpha |= filter.animateAlpha;
        animateY |= filter.animateY;
        animateZ |= filter.animateZ;
        animateScale |= filter.animateScale;
        animateHeight |= filter.animateHeight;
        animateTopInset |= filter.animateTopInset;
        animateDimmed |= filter.animateDimmed;
        animateDark |= filter.animateDark;
        animateHideSensitive |= filter.animateHideSensitive;
        hasDelays |= filter.hasDelays;
    }

    private void reset() {
        animateAlpha = false;
        animateY = false;
        animateZ = false;
        animateScale = false;
        animateHeight = false;
        animateTopInset = false;
        animateDimmed = false;
        animateDark = false;
        animateHideSensitive = false;
        hasDelays = false;
        hasGoToFullShadeEvent = false;
    }
}
