package com.liu.mytimer.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.liu.mytimer.App;
import com.liu.mytimer.R;
import com.liu.mytimer.module.DaoSession;
import com.liu.mytimer.module.GroupWorkTime;
import com.liu.mytimer.module.GroupWorkTimeDao;

import org.greenrobot.greendao.query.Query;

import java.util.List;

/**
 * Created by kunming.liu on 2017/9/19.
 */

public class WorkTimeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<GroupWorkTime> workTimeList;
    private Context context;
    private LayoutInflater layoutInflater;
    private GroupWorkTime workTime = null;
    private DaoSession daoSession = null;
    private GroupWorkTimeDao groupWorkTimeDao = null;
    private int version ;
    public WorkTimeAdapter(Context context, List<GroupWorkTime> workTimeList ) {
        this.workTimeList = workTimeList;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        workTime = new GroupWorkTime();
        daoSession = ((App)((Activity)context).getApplication()).getDaoSession();
        groupWorkTimeDao = daoSession.getGroupWorkTimeDao();

        version = android.os.Build.VERSION.SDK_INT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        RecyclerView.ViewHolder viewHolder = null;
        if(viewType == 0){
            itemView = layoutInflater.inflate(R.layout.group_layout,parent,false);
            viewHolder = new GroupViewHolder(itemView);
        }else{
            itemView = layoutInflater.inflate(R.layout.row_layout,parent,false);
            viewHolder = new ChildViewHolder(itemView);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final RecyclerView.ViewHolder viewHolder = holder;
        if(getItemViewType(position) == 0){
            ((GroupViewHolder)holder).textDate.setText(workTimeList.get(position).getDate());
            ((GroupViewHolder)holder).textCount.setText("("+String.valueOf(workTimeList.get(position).getChildCount())+")");
            if(workTimeList.get(position).isExpand()){
//                ((GroupViewHolder)viewHolder).imgExpand.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.arrow_down));
                changeBackground(((GroupViewHolder)viewHolder).imgExpand,context, R.drawable.arrow_down);
            }
            else{
//            ((GroupViewHolder)viewHolder).imgExpand.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.arrow_right));
                changeBackground(((GroupViewHolder)viewHolder).imgExpand,context, R.drawable.arrow_right);

            }
            ((GroupViewHolder)holder).imgExpand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    workTime = workTimeList.get(viewHolder.getAdapterPosition());
                    if(workTime.isExpand()){
                        //((GroupViewHolder)viewHolder).imgExpand.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.arrow_right));
                        changeBackground(((GroupViewHolder)viewHolder).imgExpand,context, R.drawable.arrow_right);
                        int childCount = workTime.getChildCount();
                        for(int i = childCount ; i >= 1 ; i--){
                            workTimeList.remove(viewHolder.getAdapterPosition()+i);
                            notifyItemRemoved(viewHolder.getAdapterPosition()+i);
                        }
                        workTimeList.get(viewHolder.getAdapterPosition()).setExpand(false);
                        notifyItemRangeChanged(viewHolder.getAdapterPosition()+childCount,getItemCount()-(viewHolder.getAdapterPosition()+childCount));
                    }else{
//                        ((GroupViewHolder)viewHolder).imgExpand.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.arrow_down));
                        changeBackground(((GroupViewHolder)viewHolder).imgExpand,context, R.drawable.arrow_down);
                        workTime = workTimeList.get(viewHolder.getAdapterPosition());
                        Query<GroupWorkTime> workTimeQuery = groupWorkTimeDao.queryBuilder()
                                .where(GroupWorkTimeDao.Properties.Date.eq(workTime.getDate()), GroupWorkTimeDao.Properties.Type.eq(1))
                                .orderDesc(GroupWorkTimeDao.Properties.Id).build();
                        List<GroupWorkTime> queryList = workTimeQuery.list();
                        for(int i = queryList.size() ; i >= 1 ; i--){
                            workTimeList.add(1+viewHolder.getAdapterPosition(),queryList.get(i-1));
                        }
                        workTimeList.get(viewHolder.getAdapterPosition()).setExpand(true);
                        //insert的話，似乎不用要一個一個呼叫notifyItemInserted
                        notifyItemRangeInserted(viewHolder.getAdapterPosition()+1,queryList.size());
                    }
                }
            });
        }else{
            setTitleAndText(((ChildViewHolder)holder).content,R.string.work_content,workTimeList.get(position).getWorkContent());
            setTitleAndText(((ChildViewHolder)holder).totalTime,R.string.total_time,workTimeList.get(position).getTotalTime());
            setTimeText(((ChildViewHolder)holder).start_end,workTimeList.get(position).getStartTime(),workTimeList.get(position).getEndTime());

        }
    }

    @Override
    public int getItemViewType(int position) {
        return workTimeList.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return workTimeList.size();
    }
    private void setTitleAndText(TextView textView , int resourceId, String content){
        String title = context.getString(resourceId);
        textView.setText(title+content);
    }
    private void setTimeText(TextView textView ,String startTime, String endTime){
        textView.setText(startTime+"~"+endTime);
    }
    private void changeBackground(ImageView imageView ,Context context,  int resId){
        if (version < Build.VERSION_CODES.JELLY_BEAN)
            imageView.setBackgroundDrawable(ContextCompat.getDrawable(context,resId));
        else if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1)
            imageView.setBackground(ContextCompat.getDrawable(context,resId));
        else
            imageView.setBackground(ContextCompat.getDrawable(context, resId));
    }
}
