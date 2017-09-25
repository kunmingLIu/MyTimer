package com.liu.mytimer.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.liu.mytimer.R;
import com.liu.mytimer.view.StarView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by kunming.liu on 2017/9/19.
 */

public class ViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.image)
    public StarView image;
    @BindView(R.id.content)
    public TextView content;
    @BindView(R.id.totalTime)
    public TextView totalTime;
    @BindView(R.id.start_end)
    public TextView start_end;
    public ViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
