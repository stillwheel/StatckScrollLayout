package com.baidu.scrollstack.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * A View which does not have overlapping renderings commands and therefore does not need a
 * layer when alpha is changed.
 */
public class AlphaOptimizedView extends View
{
    public AlphaOptimizedView(Context context) {
        super(context);
    }

    public AlphaOptimizedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlphaOptimizedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }
}
