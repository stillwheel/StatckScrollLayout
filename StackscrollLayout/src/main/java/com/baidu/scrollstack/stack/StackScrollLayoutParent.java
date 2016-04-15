package com.baidu.scrollstack.stack;

import java.lang.reflect.Field;

import com.baidu.scrollstack.R;;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by helingjian on 16/3/14.
 */
public class StackScrollLayoutParent  extends ViewGroup{

    protected int mPaddingLeft;
    protected int mPaddingTop;
    protected int mPaddingRight;
    protected int mPaddingBottom;
    protected int mLeft;
    protected int mTop;
    protected int mRight;
    protected int mBottom;
    protected Context mContext;
    protected Field mScrollXField;
    protected Field mScrollYField;

    OnHierarchyChangeListener mOnHierarchyChangeListener = new OnHierarchyChangeListener() {
        @Override
        public void onChildViewAdded(View parent, View child) {
            onViewAdded(child);
        }

        @Override
        public void onChildViewRemoved(View parent, View child) {
            onViewRemoved(child);
        }
    };
    public StackScrollLayoutParent(Context context) {
        super(context);
        init();
    }

    public StackScrollLayoutParent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StackScrollLayoutParent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    }

    private void init() {
        mPaddingLeft = getPaddingLeft();
        mPaddingTop = getPaddingTop();
        mPaddingRight = getPaddingRight();
        mPaddingBottom = getPaddingBottom();
        mLeft = getLeft();
        mTop = getTop();
        mRight = getRight();
        mBottom = getBottom();
        mContext = getContext();
        setOnHierarchyChangeListener(mOnHierarchyChangeListener);
    }

    public void invalidateParentIfNeeded() {
        if (isHardwareAccelerated() && getParent() instanceof View) {
            ((View) getParent()).invalidate();
        }
    }

    public void setScrollXObject(int value){
        try {
            if(mScrollXField != null) {
                mScrollXField.set(this, value);
                return;
            }
            Class workerClass = Class.forName(View.class.getName());
            Field mScrollXField = workerClass.getDeclaredField("mScrollX");
            mScrollXField.setAccessible(true);
            mScrollXField.set(this, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setScrollYObject(int value){
        try {
            if(mScrollYField != null) {
                mScrollYField.set(this, value);
                return;
            }
            Class workerClass = Class.forName(View.class.getName());
            Field mScrollYField = workerClass.getDeclaredField("mScrollY");
            mScrollYField.setAccessible(true);
            mScrollYField.set(this, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onViewAdded(View child) {

    }

    public void onViewRemoved(View child) {
    }
}
