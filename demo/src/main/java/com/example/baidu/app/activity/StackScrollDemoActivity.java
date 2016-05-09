package com.example.baidu.app.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.baidu.scrollstack.stack.StackScrollLayout;
import com.baidu.scrollstack.view.StackScrollPanelView;
import com.example.baidu.app.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by helingjian on 16/3/14.
 */
public class StackScrollDemoActivity extends Activity {

    private StackViewAdapter stackViewAdapter;
    private StackScrollPanelView stackScrollPanelView;
    private StackScrollLayout mStackScroller;
    private TextView stackHeader;

    StackScrollLayout.OnItemClickListener onStackItemClickListener = new StackScrollLayout.OnItemClickListener() {
        @Override
        public void onItemClick(StackScrollLayout parent, View itemView, int position, long id) {
            Log.i("onItemClick", "onItemClick");
            stackHeader.setText("onItemClick position = " + position);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stack_scrolled_layout);
        mStackScroller = (StackScrollLayout) findViewById(R.id.stack_scroller);
        stackHeader = (TextView) findViewById(R.id.stack_scroll_header);
        stackScrollPanelView = (StackScrollPanelView) findViewById(R.id.stack_scroll_panel);
        stackScrollPanelView.setEnableOverScroll(true);

        List<String> list = new ArrayList<String>();
        for (int i = 0; i < 26; i++) {
            list.add("下拉测试第" + i + "个view");
        }
        stackViewAdapter = new StackViewAdapter(LayoutInflater.from(this));
        stackViewAdapter.setData(list);
        mStackScroller.setAdapter(stackViewAdapter);
        mStackScroller.setOnItemClickListener(onStackItemClickListener);
        mStackScroller.setScrollingEnabled(true);
        stackHeader.setText("Header");
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
