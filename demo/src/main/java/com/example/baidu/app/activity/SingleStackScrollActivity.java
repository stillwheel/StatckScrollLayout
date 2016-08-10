package com.example.baidu.app.activity;

import com.baidu.scrollstack.stack.StackScrollLayout;
import com.baidu.scrollstack.view.StackScrollPanelView;
import com.example.baidu.app.R;
import com.example.baidu.app.adapter.DoubleStackViewAdapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by helingjian on 16/3/14.
 */
public class SingleStackScrollActivity extends Activity {

    private static final int TYPE_STACK_ONLY = 0;
    private static final int TYPE_STACK_AND_SCROLL = 1;
    private static final int TYPE_STACK_WITH_SPRING = 2;
    private int demoType = 2;
    private DoubleStackViewAdapter stackViewAdapter;
    private StackScrollPanelView stackScrollPanelView;
    private StackScrollLayout mStackScroller;
    private TextView stackHeader;
    private int[] drawableIds;
    private String[] titles;

    StackScrollLayout.OnItemClickListener onStackItemClickListener = new StackScrollLayout.OnItemClickListener() {
        @Override
        public void onItemClick(StackScrollLayout parent, View itemView, int position, long id) {
            stackHeader.setText(titles[position]);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switch (demoType) {
            case TYPE_STACK_ONLY:
                setContentView(R.layout.activity_stack_layout);
                break;
            case TYPE_STACK_AND_SCROLL:
                setContentView(R.layout.activity_stack_scroll_layout);
                break;
            case TYPE_STACK_WITH_SPRING:
                setContentView(R.layout.activity_stack_spring_layout);
                break;
            default:
                break;
        }
        initValues();
        initScroller();
    }

    private void initScroller() {
        mStackScroller = (StackScrollLayout) findViewById(R.id.stack_scroller);
        stackHeader = (TextView) findViewById(R.id.stack_scroll_header);
        stackScrollPanelView = (StackScrollPanelView) findViewById(R.id.stack_scroll_panel);
        stackViewAdapter = new DoubleStackViewAdapter(LayoutInflater.from(this));
        stackViewAdapter.setData(drawableIds, titles);
        mStackScroller.setAdapter(stackViewAdapter);
        mStackScroller.setOnItemClickListener(onStackItemClickListener);
        if (demoType == TYPE_STACK_AND_SCROLL) {
            stackScrollPanelView.setEnableOverScroll(false);
        }
    }

    //    @Override
    //    public void onBackPressed() {
    //        if (stackScrollPanelView.isFullyExpanded()) {
    //            stackScrollPanelView.collapse(false);
    //        } else if (stackScrollPanelView.isFullyCollapsed()) {
    //            stackScrollPanelView.expand(false);
    //        }
    //        stackViewAdapter.notifyDataSetChanged();
    //    }

    private void initValues() {
        titles = new String[] {
                getString(R.string.item_name_1), getString(R.string.item_name_2),
                getString(R.string.item_name_3), getString(R.string.item_name_4),
                getString(R.string.item_name_5), getString(R.string.item_name_6),
                getString(R.string.item_name_7), getString(R.string.item_name_8),
                getString(R.string.item_name_9), getString(R.string.item_name_10),
                getString(R.string.item_name_11), getString(R.string.item_name_12),
                getString(R.string.item_name_13), getString(R.string.item_name_4),
                getString(R.string.item_name_15), getString(R.string.item_name_16),
                getString(R.string.item_name_17), getString(R.string.item_name_8),
                getString(R.string.item_name_19)
        };
        drawableIds = new int[] {
                R.drawable.icon1, R.drawable.icon2, R.drawable.icon3, R.drawable.icon4,
                R.drawable.icon5, R.drawable.icon6, R.drawable.icon7, R.drawable.icon8,
                R.drawable.icon9, R.drawable.icon10, R.drawable.icon11, R.drawable.icon12,
                R.drawable.icon13, R.drawable.icon4, R.drawable.icon15, R.drawable.icon16,
                R.drawable.icon17, R.drawable.icon8, R.drawable.icon19
        };
    }
}
