package com.example.baidu.app.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.baidu.scrollstack.stack.StackScrollLayout;
import com.baidu.scrollstack.view.StackScrollPanelView;
import com.example.baidu.app.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by helingjian on 16/3/14.
 */
public class StackScrollDemoActivity extends Activity {

    private static final int TYPE_STACK_ONLY = 0;
    private static final int TYPE_STACK_AND_SCROLL = 1;
    private static final int TYPE_STACK_WITH_SPRING = 2;
    private int demoType = 2;
    private StackViewAdapter stackViewAdapter;
    private StackScrollPanelView stackScrollPanelView;
    private StackScrollLayout mStackScroller;
    private TextView stackHeader;

    StackScrollLayout.OnItemClickListener onStackItemClickListener = new StackScrollLayout.OnItemClickListener() {
        @Override
        public void onItemClick(StackScrollLayout parent, View itemView, int position, long id) {
            stackHeader.setText("onItemClick position = " + position);
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
        initScroller();
    }

    private void initScroller() {
        mStackScroller = (StackScrollLayout) findViewById(R.id.stack_scroller);
        stackHeader = (TextView) findViewById(R.id.stack_scroll_header);
        stackScrollPanelView = (StackScrollPanelView) findViewById(R.id.stack_scroll_panel);
        stackViewAdapter = new StackViewAdapter(LayoutInflater.from(this));
        stackViewAdapter.setData(getData());
        mStackScroller.setAdapter(stackViewAdapter);
        mStackScroller.setOnItemClickListener(onStackItemClickListener);
        if (demoType == TYPE_STACK_AND_SCROLL) {
            stackScrollPanelView.setEnableOverScroll(false);
        }
    }

    private List<String> getData() {
        List<String> list = new ArrayList<String>();
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 26; i++) {
            list.add(i + " :" + calendar.getTime());
        }
        return list;
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        finish();
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (stackScrollPanelView.isFullyExpanded()) {
            stackScrollPanelView.collapse(false);
        } else if (stackScrollPanelView.isFullyCollapsed()) {
            stackScrollPanelView.expand(false);
        }
        stackViewAdapter.setData(getData());
        stackViewAdapter.notifyDataSetChanged();
    }
}
