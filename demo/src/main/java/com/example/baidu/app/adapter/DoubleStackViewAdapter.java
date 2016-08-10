package com.example.baidu.app.adapter;

import com.baidu.scrollstack.uitl.StackViewBaseAdapter;
import com.example.baidu.app.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by helingjian on 16/3/29.
 */
public class DoubleStackViewAdapter extends StackViewBaseAdapter {

    private int[] drawableIds = new int[] {};
    private String[] titles = new String[] {};
    private LayoutInflater layoutInflater;

    public DoubleStackViewAdapter(LayoutInflater inflater) {
        layoutInflater = inflater;
    }

    public void setData(int[] drawables, String[] titleIds) {
        this.drawableIds = drawables;
        this.titles = titleIds;
    }

    @Override
    public int getCount() {
        return drawableIds.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
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
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.textView.setText(titles[position]);
        viewHolder.imageView.setImageResource(drawableIds[position]);
        return convertView;
    }

    class ViewHolder {
        TextView textView;
        ImageView imageView;
    }
}
