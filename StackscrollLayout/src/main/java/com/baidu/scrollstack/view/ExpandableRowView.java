package com.baidu.scrollstack.view;

import com.baidu.scrollstack.stack.StackScrollerDecorView;
import com.baidu.scrollstack.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Like {@link ExpandableView}, but setting an outline for the height and clipping.
 */
public class ExpandableRowView extends StackScrollerDecorView {

    public ExpandableRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View findContentView() {
        return findViewById(R.id.content);
    }
}
