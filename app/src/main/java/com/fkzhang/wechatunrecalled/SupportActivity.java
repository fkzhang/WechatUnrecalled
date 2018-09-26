package com.fkzhang.wechatunrecalled;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.fkzhang.wechatunrecalled.Adapter.SupportListAdapter;

import java.util.ArrayList;

public class SupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        ListView listView = (ListView) findViewById(R.id.qr_list);
        final int[] drawableIds = {R.drawable.wechat_qr, R.drawable.alipay_qr};
        final ArrayList<Drawable> drawables = new ArrayList<>();
        for (int drawableId : drawableIds) {
            drawables.add(ContextCompat.getDrawable(this, drawableId));
        }

        ArrayList<String> titles = new ArrayList<>();
        titles.add(getString(R.string.wechat));
        titles.add(getString(R.string.alipay));

        listView.setAdapter(new SupportListAdapter(this, drawables, titles));


    }


}
