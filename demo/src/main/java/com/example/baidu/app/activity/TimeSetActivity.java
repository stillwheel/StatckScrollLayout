package com.example.baidu.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.baidu.scrollstack.stack.StackScrollLayout;
import com.baidu.scrollstack.view.StackScrollPanelView;
import com.example.baidu.app.R;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by helingjian on 16/3/14.
 */
public class TimeSetActivity extends Activity {

    private List<String> years = new ArrayList<String>();
    private List<String> months = new ArrayList<String>();
    private List<String> days = new ArrayList<String>();
    private View lastHourItemView;
    StackScrollLayout.OnItemClickListener onHourItemClickListener = new StackScrollLayout.OnItemClickListener() {
        @Override
        public void onItemClick(StackScrollLayout parent, View itemView, int position, long id) {
            changBackground(itemView, true);
            changBackground(lastHourItemView, false);
            lastHourItemView = itemView;
        }
    };
    private View lastMinuteItemView;
    StackScrollLayout.OnItemClickListener onMinuteItemClickListener = new StackScrollLayout.OnItemClickListener() {
        @Override
        public void onItemClick(StackScrollLayout parent, View itemView, int position, long id) {
            changBackground(itemView, true);
            changBackground(lastMinuteItemView, false);
            lastMinuteItemView = itemView;
        }
    };
    private View lastSecondItemView;
    StackScrollLayout.OnItemClickListener onSecondItemClickListener = new StackScrollLayout.OnItemClickListener() {
        @Override
        public void onItemClick(StackScrollLayout parent, View itemView, int position, long id) {
            changBackground(itemView, true);
            changBackground(lastSecondItemView, false);
            lastSecondItemView = itemView;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_set_layout);
        initValues();
        initScroller();
    }

    private void initValues() {
        for (int i = 1970; i <= 2016; i++) {
            years.add(String.valueOf(i));
        }
        for (int i = 0; i <= 12; i++) {
            months.add(String.valueOf(i));
        }
        for (int i = 0; i <= 30; i++) {
            days.add(String.valueOf(i));
        }
    }

    private void initScroller() {
        initHourStackView();
        initMinuteStackView();
        initSecondStackView();
    }

    private void initHourStackView() {
        View hourStackView = findViewById(R.id.stack_view_hour);
        StackScrollLayout mStackScrollerHour = (StackScrollLayout) hourStackView.findViewById(R.id.stack_scroller);
        StackViewAdapter stackViewAdapterHour = new StackViewAdapter(LayoutInflater.from(this));
        stackViewAdapterHour.setData(years);
        mStackScrollerHour.setAdapter(stackViewAdapterHour);
        mStackScrollerHour.setOnItemClickListener(onHourItemClickListener);
        StackScrollPanelView stackScrollHourPanelView =
                (StackScrollPanelView) hourStackView.findViewById(R.id.stack_scroll_panel);
        stackScrollHourPanelView.setTouchDisabled(true);
        stackScrollHourPanelView.expand(true);
    }

    private void initMinuteStackView() {
        View minuteStackView = findViewById(R.id.stack_view_minute);
        StackScrollLayout mStackScrollerMinute = (StackScrollLayout) minuteStackView.findViewById(R.id.stack_scroller);
        StackViewAdapter stackViewAdapterMinute = new StackViewAdapter(LayoutInflater.from(this));
        stackViewAdapterMinute.setData(months);
        mStackScrollerMinute.setAdapter(stackViewAdapterMinute);
        mStackScrollerMinute.setOnItemClickListener(onMinuteItemClickListener);
        StackScrollPanelView stackScrollMinutePanelView =
                (StackScrollPanelView) minuteStackView.findViewById(R.id.stack_scroll_panel);
        stackScrollMinutePanelView.setTouchDisabled(true);
        stackScrollMinutePanelView.expand(true);
    }

    private void initSecondStackView() {
        View secondStackView = findViewById(R.id.stack_view_second);
        StackScrollLayout mStackScrollerMSecond = (StackScrollLayout) secondStackView.findViewById(R.id.stack_scroller);
        StackViewAdapter stackViewAdapterSecond = new StackViewAdapter(LayoutInflater.from(this));
        stackViewAdapterSecond.setData(days);
        mStackScrollerMSecond.setAdapter(stackViewAdapterSecond);
        mStackScrollerMSecond.setOnItemClickListener(onSecondItemClickListener);
        StackScrollPanelView stackScrollSecondPanelView =
                (StackScrollPanelView) secondStackView.findViewById(R.id.stack_scroll_panel);
        stackScrollSecondPanelView.setTouchDisabled(true);
        stackScrollSecondPanelView.expand(true);
    }

    private void changBackground(View itemView, boolean focus) {
        if (itemView == null) {
            return;
        }
        if (focus) {
            itemView.findViewById(R.id.content).setBackgroundColor(0xff116cd5);
            ((TextView) itemView.findViewById(R.id.text)).setTextColor(Color.WHITE);
        } else {
            itemView.findViewById(R.id.content).setBackgroundColor(0xff489bf8);
            ((TextView) itemView.findViewById(R.id.text)).setTextColor(Color.WHITE);
        }
    }
}
