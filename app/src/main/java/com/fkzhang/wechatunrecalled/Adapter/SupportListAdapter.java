package com.fkzhang.wechatunrecalled.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fkzhang.wechatunrecalled.R;

import java.util.ArrayList;

/**
 * Created by fkzhang on 2/8/2016.
 */
public class SupportListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Activity mActivity;
    private ArrayList<Drawable> mDrawables;
    private ArrayList<String> mTitles;

    public SupportListAdapter(Activity activity, ArrayList<Drawable> drawables, ArrayList<String> titles) {
        mActivity = activity;
        mDrawables = drawables;
        mTitles = titles;
    }

    @Override
    public int getCount() {
        return mDrawables.size();
    }

    @Override
    public Object getItem(int position) {
        return mDrawables.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mInflater == null) {
            mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_support, null);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
        imageView.setImageDrawable(mDrawables.get(position));

        TextView textView = (TextView) convertView.findViewById(R.id.textView);
        textView.setText(mTitles.get(position));

        return convertView;
    }
}
