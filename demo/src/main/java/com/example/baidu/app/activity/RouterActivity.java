package com.example.baidu.app.activity;

import com.example.baidu.app.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by LingJian·HE on 16/8/9.
 */
public class RouterActivity extends Activity {

    View.OnClickListener onDemo1ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showDemo(SingleStackScrollActivity.class);
        }
    };

    View.OnClickListener onDemo2ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showDemo(DoubleStackScrollActivity.class);
        }
    };

    View.OnClickListener onDemo3ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showDemo(TimeSetActivity.class);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router);
        initView();
    }

    private void initView() {
        findViewById(R.id.demo_single_stack).setOnClickListener(onDemo1ClickListener);
        findViewById(R.id.demo_double_stack).setOnClickListener(onDemo2ClickListener);
        findViewById(R.id.demo_time_set).setOnClickListener(onDemo3ClickListener);
    }

    private void showDemo(Class clsName) {
        Intent intent = new Intent();
        intent.setClass(this, clsName);
        startActivity(intent);
    }
}
