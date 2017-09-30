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
import com.liu.mytimer.dialog.EditWorkRecordDialog;
import com.liu.mytimer.module.DaoSession;
import com.liu.mytimer.module.WorkRecord;
import com.liu.mytimer.module.WorkRecordDao;
import com.liu.mytimer.utils.Util;

import org.greenrobot.greendao.query.Query;

import java.util.List;

/**
 * Created by kunming.liu on 2017/9/19.
 */

public class WorkRecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<WorkRecord> workRecordList;
    private Context context;
    private LayoutInflater layoutInflater;
    private WorkRecord workRecord = null;

    private int version ;
    private OnLongClickListener listener;
    public WorkRecordAdapter(Context context, List<WorkRecord> workRecordList ) {
        this.workRecordList = workRecordList;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        workRecord = new WorkRecord();
//        daoSession = ((App)((Activity)context).getApplication()).getDaoSession();
//        workRecordDao = daoSession.getWorkRecordDao();

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
            ((GroupViewHolder)holder).textDate.setText(workRecordList.get(position).getDate());
            ((GroupViewHolder)holder).textCount.setText("("+String.valueOf(workRecordList.get(position).getChildCount())+")");
            ((GroupViewHolder)holder).timeTableView.setGroupWorkRecord(workRecordList.get(position));
//            ((GroupViewHolder)holder).timeTableView.forceLayout();
            if(workRecordList.get(position).isExpand()){
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
                    workRecord = workRecordList.get(viewHolder.getAdapterPosition());
                    if(workRecord.isExpand()){
                        //((GroupViewHolder)viewHolder).imgExpand.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.arrow_right));
                        changeBackground(((GroupViewHolder)viewHolder).imgExpand,context, R.drawable.arrow_right);
                        int childCount = workRecord.getChildCount();
                        for(int i = childCount ; i >= 1 ; i--){
                            workRecordList.remove(viewHolder.getAdapterPosition()+i);
                            notifyItemRemoved(viewHolder.getAdapterPosition()+i);
                        }
                        workRecordList.get(viewHolder.getAdapterPosition()).setExpand(false);
                        notifyItemRangeChanged(viewHolder.getAdapterPosition()+childCount,getItemCount()-(viewHolder.getAdapterPosition()+childCount));
                    }else{
//                        ((GroupViewHolder)viewHolder).imgExpand.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.arrow_down));
                        changeBackground(((GroupViewHolder)viewHolder).imgExpand,context, R.drawable.arrow_down);
                        workRecord = workRecordList.get(viewHolder.getAdapterPosition());

//                        Query<WorkRecord> workRecordQuery = workRecordDao.queryBuilder()
//                                .where(WorkRecordDao.Properties.Date.eq(workRecord.getDate()), WorkRecordDao.Properties.Type.eq(1))
//                                .orderDesc(WorkRecordDao.Properties.Id).build();
//                        List<WorkRecord> queryList = workRecordQuery.list();
                        List<WorkRecord> childList = workRecord.getChildList();
                        for(int i = childList.size() ; i >= 1 ; i--){
                            workRecordList.add(1+viewHolder.getAdapterPosition(),childList.get(i-1));
                        }
                        workRecordList.get(viewHolder.getAdapterPosition()).setExpand(true);
                        //insert的話，似乎不用要一個一個呼叫notifyItemInserted
                        notifyItemRangeInserted(viewHolder.getAdapterPosition()+1,childList.size());
                    }
                }
            });
        }else{
            ((ChildViewHolder)holder).card.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onLongClick(workRecordList.get(viewHolder.getAdapterPosition()));
                    return false;
                }
            });
            setTitleAndText(((ChildViewHolder)holder).content,R.string.work_content,workRecordList.get(position).getWorkContent());
            setTitleAndText(((ChildViewHolder)holder).totalTime,R.string.total_time,workRecordList.get(position).getTotalWorkTime());
            setTimeText(((ChildViewHolder)holder).start_end,workRecordList.get(position).getStartTime(),workRecordList.get(position).getEndTime());

        }
    }

    @Override
    public int getItemViewType(int position) {
        return workRecordList.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return workRecordList.size();
    }
    private void setTitleAndText(TextView textView , int resourceId, String content){
        String title = context.getString(resourceId);
        textView.setText(title+content);
    }
    private void setTitleAndText(TextView textView , int resourceId, long time){
        String title = context.getString(resourceId);
        textView.setText(title+ Util.covertTimeToString(time));
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

    public void setListener(OnLongClickListener listener) {
        this.listener = listener;
    }

    public interface OnLongClickListener{
        void onLongClick(WorkRecord workRecord);
    }
}
