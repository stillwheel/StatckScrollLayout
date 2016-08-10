package com.example.baidu.app.entity;

/**
 * Created by LingJian·HE on 16/8/10.
 */
public class TimeItemInfo {

    String time;
    boolean isSelect;

    public TimeItemInfo(String time, boolean isSelect) {
        this.time = time;
        this.isSelect = isSelect;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}
