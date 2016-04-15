package com.example.baidu.app.activity;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.baidu.scrollstack.R;
import com.baidu.scrollstack.stack.StackScrollLayout;
import com.baidu.scrollstack.view.StackScrollPanelView;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

/**
 * Created by helingjian on 16/3/14.
 */
public class StackScrollDemoActivity extends Activity {

    private final String TAG = "StackScrollDemoActivity";
    private StackViewAdapter stackViewAdapter;
    private StackScrollPanelView stackScrollPanelView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stack_scrolled_layout);
        final StackScrollLayout mStackScroller = (StackScrollLayout) findViewById(R.id
                .stack_scroller);
        stackScrollPanelView = (StackScrollPanelView) findViewById(R.id.stack_scroll_panel);
        stackScrollPanelView.setEnableOverScroll(true);

        List<String> list = new ArrayList<String>();
        for (int i = 0; i < 26; i++) {
            list.add("下拉测试第下拉测试" + i + "个view");
        }
        stackViewAdapter = new StackViewAdapter(LayoutInflater.from(this));
        stackViewAdapter.setData(list);
        mStackScroller.setAdapter(stackViewAdapter);
        mStackScroller.setOnItemClickListener(onStackItemClickListener);
        mStackScroller.setScrollingEnabled(true);
    }

    StackScrollLayout.OnItemClickListener onStackItemClickListener = new StackScrollLayout.OnItemClickListener() {
        @Override
        public void onItemClick(StackScrollLayout parent, View itemView, int position, long id) {
            Log.i(TAG, "onItemClick position = " + position);
        }
    };

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        finish();
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (stackScrollPanelView.isFullyExpanded()) {
            stackScrollPanelView.collapse(false);
        } else  if (stackScrollPanelView.isFullyCollapsed()){
            stackScrollPanelView.expand(false);
        }
        List<String> list = new ArrayList<String>();
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 26; i++) {
            list.add("下拉测试i " + i + " : " + calendar.get(Calendar.HOUR)
                    + " : " + calendar.get(Calendar.MINUTE) + " : " + calendar.get(Calendar.SECOND));
        }
        stackViewAdapter.setData(list);
        stackViewAdapter.notifyDataSetChanged();
    }
}
