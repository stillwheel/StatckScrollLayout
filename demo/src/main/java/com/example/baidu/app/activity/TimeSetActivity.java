package com.example.baidu.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.baidu.scrollstack.stack.StackScrollLayout;
import com.baidu.scrollstack.view.StackScrollPanelView;
import com.example.baidu.app.R;
import com.example.baidu.app.adapter.StackViewAdapter;
import com.example.baidu.app.entity.TimeItemInfo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by helingjian on 16/3/14.
 */
public class TimeSetActivity extends Activity {

    private static final int TAG_YEAR = 0;
    private static final int TAG_MOINTH = 1;
    private static final int TAG_DAY = 2;
    private List<TimeItemInfo> yearInfos = new ArrayList<TimeItemInfo>();
    private List<TimeItemInfo> monthInfos = new ArrayList<TimeItemInfo>();
    private List<TimeItemInfo> dayInfos = new ArrayList<TimeItemInfo>();
    private View lastYearItemView;
    private StackScrollLayout mStackScrollerDays;
    private StackScrollLayout mStackScrollerMonth;
    private StackScrollLayout mStackScrollerYear;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TAG_YEAR: {
                    View itemView = (View) msg.obj;
                    int ownScrollY = mStackScrollerYear.getOwnScrollY();
                    int toY;
                    int translationY = (int) itemView.getTranslationY();
                    int speed = 10;//msg.arg2;//Math.max(Math.abs(msg.arg1 - translationY) / 10, 5);
                    if (translationY > msg.arg1) {
                        toY = ownScrollY + speed;
                        toY = Math.min(toY, ownScrollY + (translationY - msg.arg1));
                    } else if (translationY < msg.arg1) {
                        toY = ownScrollY - speed;
                        toY = Math.max(ownScrollY - (msg.arg1 - translationY), toY);
                    } else {
                        break;
                    }
                    mStackScrollerYear.setOwnScrollY(toY);
                    mStackScrollerYear.updateChildren();
                    if (itemView.getTranslationY() != msg.arg1) {
                        smoothToCenter(itemView, TAG_YEAR, msg.arg2);
                    }
                }
                break;

                case TAG_MOINTH: {
                    View itemView = (View) msg.obj;
                    int ownScrollY = mStackScrollerMonth.getOwnScrollY();
                    int toY;
                    int translationY = (int) itemView.getTranslationY();
                    int speed = 10;//msg.arg2;//Math.max(Math.abs(msg.arg1 - translationY) / 10, 5);
                    if (translationY > msg.arg1) {
                        toY = ownScrollY + speed;
                        toY = Math.min(toY, ownScrollY + (translationY - msg.arg1));
                    } else if (translationY < msg.arg1) {
                        toY = ownScrollY - speed;
                        toY = Math.max(ownScrollY - (msg.arg1 - translationY), toY);
                    } else {
                        break;
                    }
                    mStackScrollerMonth.setOwnScrollY(toY);
                    mStackScrollerMonth.updateChildren();
                    if (itemView.getTranslationY() != msg.arg1) {
                        smoothToCenter(itemView, TAG_MOINTH, msg.arg2);
                    }
                }
                break;

                case TAG_DAY: {
                    View itemView = (View) msg.obj;
                    int ownScrollY = mStackScrollerDays.getOwnScrollY();
                    int toY;
                    int translationY = (int) itemView.getTranslationY();
                    int speed = 10;//msg.arg2;//Math.max(Math.abs(msg.arg1 - translationY) / 10, 5);
                    if (translationY > msg.arg1) {
                        toY = ownScrollY + speed;
                        toY = Math.min(toY, ownScrollY + (translationY - msg.arg1));
                    } else if (translationY < msg.arg1) {
                        toY = ownScrollY - speed;
                        toY = Math.max(ownScrollY - (msg.arg1 - translationY), toY);
                    } else {
                        break;
                    }
                    mStackScrollerDays.setOwnScrollY(toY);
                    mStackScrollerDays.updateChildren();
                    if (itemView.getTranslationY() != msg.arg1) {
                        smoothToCenter(itemView, TAG_DAY, msg.arg2);
                    }
                }
                default:
                    break;
            }
        }
    };
    StackScrollLayout.OnItemClickListener onYearItemClickListener = new StackScrollLayout.OnItemClickListener() {
        @Override
        public void onItemClick(StackScrollLayout parent, View itemView, int position, long id) {
            if (position == 0) {
                return;
            }
            TimeItemInfo info = (TimeItemInfo) itemView.getTag();
            info.setSelect(!info.isSelect());
            changBackground(itemView, info.isSelect());

            if (lastYearItemView != null && lastYearItemView != itemView) {
                TimeItemInfo lastInfo = (TimeItemInfo) lastYearItemView.getTag();
                lastInfo.setSelect(false);
                changBackground(lastYearItemView, lastInfo.isSelect());
            }
            lastYearItemView = itemView;
            smoothToCenter(itemView, TAG_YEAR, 0);
        }
    };
    private View lastMonthItemView;
    StackScrollLayout.OnItemClickListener onMonthItemClickListener = new StackScrollLayout.OnItemClickListener() {
        @Override
        public void onItemClick(StackScrollLayout parent, View itemView, int position, long id) {
            if (position == 0) {
                return;
            }
            TimeItemInfo info = (TimeItemInfo) itemView.getTag();
            info.setSelect(!info.isSelect());
            changBackground(itemView, info.isSelect());

            if (lastMonthItemView != null && lastMonthItemView != itemView) {
                TimeItemInfo lastInfo = (TimeItemInfo) lastMonthItemView.getTag();
                lastInfo.setSelect(false);
                changBackground(lastMonthItemView, lastInfo.isSelect());
            }
            lastMonthItemView = itemView;
            smoothToCenter(itemView, TAG_MOINTH, 0);
        }
    };
    private View lastDayItemView;
    StackScrollLayout.OnItemClickListener onDayItemClickListener = new StackScrollLayout.OnItemClickListener() {
        @Override
        public void onItemClick(StackScrollLayout parent, View itemView, int position, long id) {
            if (position == 0) {
                return;
            }

            TimeItemInfo info = (TimeItemInfo) itemView.getTag();
            info.setSelect(!info.isSelect());
            changBackground(itemView, info.isSelect());

            if (lastDayItemView != null && lastDayItemView != itemView) {
                TimeItemInfo lastInfo = (TimeItemInfo) lastDayItemView.getTag();
                lastInfo.setSelect(false);
                changBackground(lastDayItemView, lastInfo.isSelect());
            }
            lastDayItemView = itemView;
            smoothToCenter(itemView, TAG_DAY, 0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_set_layout);
        initValues();
        insertTitles();
        initScroller();
    }

    private void insertTitles() {
        yearInfos.add(0, new TimeItemInfo("年份", false));
        monthInfos.add(0, new TimeItemInfo("月份", false));
        dayInfos.add(0, new TimeItemInfo("日期", false));
    }

    private void initValues() {
        for (int i = 1970; i <= 2016; i++) {
            yearInfos.add(new TimeItemInfo(String.valueOf(i), false));
        }
        for (int i = 0; i <= 12; i++) {
            monthInfos.add(new TimeItemInfo(String.valueOf(i), false));
        }
        for (int i = 0; i <= 30; i++) {
            dayInfos.add(new TimeItemInfo(String.valueOf(i), false));
        }
    }

    private void initScroller() {
        initYearStackView();
        initMonthStackView();
        initDayStackView();
    }

    private void initYearStackView() {
        View YearStackView = findViewById(R.id.stack_view_year);
        mStackScrollerYear = (StackScrollLayout) YearStackView.findViewById(R.id.stack_scroller);
        StackViewAdapter stackViewAdapterYear = new StackViewAdapter(LayoutInflater.from(this));
        stackViewAdapterYear.setData(yearInfos);
        mStackScrollerYear.setAdapter(stackViewAdapterYear);
        mStackScrollerYear.setOnItemClickListener(onYearItemClickListener);
        StackScrollPanelView stackScrollYearPanelView =
                (StackScrollPanelView) YearStackView.findViewById(R.id.stack_scroll_panel);
        stackScrollYearPanelView.setTouchDisabled(true);
        stackScrollYearPanelView.expand(true);
    }

    private void initMonthStackView() {
        View MonthStackView = findViewById(R.id.stack_view_month);
        mStackScrollerMonth = (StackScrollLayout) MonthStackView.findViewById(R.id.stack_scroller);
        StackViewAdapter stackViewAdapterMonth = new StackViewAdapter(LayoutInflater.from(this));
        stackViewAdapterMonth.setData(monthInfos);
        mStackScrollerMonth.setAdapter(stackViewAdapterMonth);
        mStackScrollerMonth.setOnItemClickListener(onMonthItemClickListener);
        StackScrollPanelView stackScrollMonthPanelView =
                (StackScrollPanelView) MonthStackView.findViewById(R.id.stack_scroll_panel);
        stackScrollMonthPanelView.setTouchDisabled(true);
        stackScrollMonthPanelView.expand(true);
    }

    private void initDayStackView() {
        View daysStackView = findViewById(R.id.stack_view_days);
        mStackScrollerDays = (StackScrollLayout) daysStackView.findViewById(R.id.stack_scroller);
        StackViewAdapter stackViewAdapterDays = new StackViewAdapter(LayoutInflater.from(this));
        stackViewAdapterDays.setData(dayInfos);
        mStackScrollerDays.setAdapter(stackViewAdapterDays);
        mStackScrollerDays.setOnItemClickListener(onDayItemClickListener);
        StackScrollPanelView stackScrollDaysPanelView =
                (StackScrollPanelView) daysStackView.findViewById(R.id.stack_scroll_panel);
        stackScrollDaysPanelView.setTouchDisabled(true);
        stackScrollDaysPanelView.expand(true);
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

    private void smoothToCenter(View itemView, int type, int speed) {
        final int center = 104 + 20 + 104 + 20 + 20;
        Message message = Message.obtain();
        message.what = type;
        message.obj = itemView;
        message.arg1 = center;
        if (speed == 0) {
            message.arg2 = (int) (Math.abs(itemView.getTranslationY() - center) / 10);
        }
        handler.removeMessages(type);
        handler.sendMessageDelayed(message, 0);
    }
}
