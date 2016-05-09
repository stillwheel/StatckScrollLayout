package com.example.baidu.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.baidu.scrollstack.uitl.StackViewBaseAdapter;
import com.example.baidu.app.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by helingjian on 16/3/29.
 */
public class StackViewAdapter extends StackViewBaseAdapter{

    private List<String> list = new ArrayList<String>();
    private LayoutInflater layoutInflater;

    public StackViewAdapter(LayoutInflater inflater){
        layoutInflater = inflater;
    }

    public void setData(List<String> list){
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.activity_stack_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.textView.setText(list.get(position));
        return convertView;
    }

    class ViewHolder {
        TextView textView;
    }
}
