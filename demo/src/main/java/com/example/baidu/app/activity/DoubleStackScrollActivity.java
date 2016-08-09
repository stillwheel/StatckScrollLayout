package com.example.baidu.app.activity;

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
public class DoubleStackScrollActivity extends Activity {

    private int[] drawableLeftIds = new int[] {
            R.drawable.icon1, R.drawable.icon2, R.drawable.icon3, R.drawable.icon4,
            R.drawable.icon5, R.drawable.icon6, R.drawable.icon7, R.drawable.icon8,
            R.drawable.icon9, R.drawable.icon10, R.drawable.icon11, R.drawable.icon12,
            R.drawable.icon13, R.drawable.icon4, R.drawable.icon15, R.drawable.icon16,
            R.drawable.icon17, R.drawable.icon8, R.drawable.icon19
    };
    private int[] titleLeftIds = new int[] {
            R.string.item_name_19, R.string.item_name_18, R.string.item_name_17, R.string.item_name_16,
            R.string.item_name_15, R.string.item_name_14, R.string.item_name_13, R.string.item_name_12,
            R.string.item_name_11, R.string.item_name_10, R.string.item_name_9, R.string.item_name_8,
            R.string.item_name_7, R.string.item_name_6, R.string.item_name_5, R.string.item_name_4,
            R.string.item_name_3, R.string.item_name_2, R.string.item_name_1
    };
    private int[] drawableRightIds = new int[] {
            R.drawable.icon19, R.drawable.icon18, R.drawable.icon17, R.drawable.icon16,
            R.drawable.icon15, R.drawable.icon14, R.drawable.icon13, R.drawable.icon12,
            R.drawable.icon11, R.drawable.icon10, R.drawable.icon9, R.drawable.icon8,
            R.drawable.icon7, R.drawable.icon6, R.drawable.icon5, R.drawable.icon4,
            R.drawable.icon3, R.drawable.icon2, R.drawable.icon1
    };
    private int[] titleRightIds = new int[] {
            R.string.item_name_1, R.string.item_name_2, R.string.item_name_3, R.string.item_name_4,
            R.string.item_name_5, R.string.item_name_6, R.string.item_name_7, R.string.item_name_8,
            R.string.item_name_9, R.string.item_name_10, R.string.item_name_11, R.string.item_name_12,
            R.string.item_name_13, R.string.item_name_4, R.string.item_name_15, R.string.item_name_16,
            R.string.item_name_17, R.string.item_name_8, R.string.item_name_19
    };
    private DoubleStackViewAdapter stackViewAdapterLeft;
    private StackScrollPanelView stackScrollPanelViewLeft;
    private StackScrollLayout mStackScrollerLeft;
    private TextView stackHeaderLeft;

    StackScrollLayout.OnItemClickListener onStackItemClickListener = new StackScrollLayout.OnItemClickListener() {
        @Override
        public void onItemClick(StackScrollLayout parent, View itemView, int position, long id) {
            stackHeaderLeft.setText("onItemClick position = " + position);
        }
    };
    private StackScrollLayout mStackScrollerRight;
    private TextView stackHeaderRight;
    private StackScrollPanelView stackScrollPanelViewRight;
    private DoubleStackViewAdapter stackViewAdapterRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_double_stack_layout);
        initScroller();
    }

    private void initScroller() {

        View leftStackView = findViewById(R.id.stack_view_left);
        mStackScrollerLeft = (StackScrollLayout) leftStackView.findViewById(R.id.stack_scroller);
        stackHeaderLeft = (TextView) leftStackView.findViewById(R.id.stack_scroll_header);
        stackScrollPanelViewLeft = (StackScrollPanelView) leftStackView.findViewById(R.id.stack_scroll_panel);
        stackViewAdapterLeft = new DoubleStackViewAdapter(LayoutInflater.from(this));
        stackViewAdapterLeft.setData(drawableLeftIds, titleLeftIds);
        mStackScrollerLeft.setAdapter(stackViewAdapterLeft);
        mStackScrollerLeft.setOnItemClickListener(onStackItemClickListener);

        View rightStackView = findViewById(R.id.stack_view_right);
        mStackScrollerRight = (StackScrollLayout) rightStackView.findViewById(R.id.stack_scroller);
        stackHeaderRight = (TextView) rightStackView.findViewById(R.id.stack_scroll_header);
        stackScrollPanelViewRight = (StackScrollPanelView) rightStackView.findViewById(R.id.stack_scroll_panel);
        stackViewAdapterRight = new DoubleStackViewAdapter(LayoutInflater.from(this));
        stackViewAdapterLeft.setData(drawableRightIds, titleRightIds);
        mStackScrollerRight.setAdapter(stackViewAdapterLeft);
        mStackScrollerRight.setOnItemClickListener(onStackItemClickListener);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        finish();
        return super.onKeyLongPress(keyCode, event);
    }
}
