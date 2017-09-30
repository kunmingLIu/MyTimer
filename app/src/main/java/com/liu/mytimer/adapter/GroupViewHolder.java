package com.liu.mytimer.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.liu.mytimer.R;
import com.liu.mytimer.view.TimeTableView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by kunming.liu on 2017/9/22.
 */

public class GroupViewHolder  extends RecyclerView.ViewHolder {
    @BindView(R.id.textDate)
    public TextView textDate;
    @BindView(R.id.textCount)
    public TextView textCount;
    @BindView(R.id.timeTableView)
    public TimeTableView timeTableView;
    @BindView(R.id.imgExpand)
    public ImageView imgExpand;
    public GroupViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}