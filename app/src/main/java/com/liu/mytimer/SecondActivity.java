package com.liu.mytimer;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.liu.mytimer.adapter.WorkRecordAdapter;
import com.liu.mytimer.dialog.EditWorkRecordDialog;
import com.liu.mytimer.module.DaoSession;
import com.liu.mytimer.module.WorkRecord;
import com.liu.mytimer.module.WorkRecordDao;

import org.greenrobot.greendao.query.Query;

import java.util.List;

/**
 * Created by kunming.liu on 2017/9/19.
 */

public class SecondActivity  extends AppCompatActivity {
    private RecyclerView recyclerView = null;
    private List<WorkRecord> groupRecordList = null;
    private List<WorkRecord> childRecordList = null;
    private WorkRecordAdapter workRecordAdapter = null;
    private DaoSession daoSession = null;
    private WorkRecordDao workRecordDao = null;
    private WorkRecordAdapter.OnLongClickListener listener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        initDB();

        workRecordAdapter = new WorkRecordAdapter(SecondActivity.this,groupRecordList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(SecondActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(workRecordAdapter);

        listener = new WorkRecordAdapter.OnLongClickListener() {
            @Override
            public void onLongClick(WorkRecord workRecord) {
                EditWorkRecordDialog editWorkRecordDialog = new EditWorkRecordDialog();
                Bundle bundle = new Bundle();
                bundle.putLong("total_work_time",workRecord.getTotalWorkTime());
                bundle.putString("start_time",workRecord.getStartTime());
                bundle.putString("end_time",workRecord.getEndTime());
                bundle.putString("work_content",workRecord.getWorkContent());
                editWorkRecordDialog.setArguments(bundle);
                editWorkRecordDialog.show(getSupportFragmentManager(),"[EDIT_WORK_RECORD]");

            }
        };
        workRecordAdapter.setListener(listener);
    }
    private void initDB(){
        daoSession = ((App)getApplication()).getDaoSession();
        workRecordDao = daoSession.getWorkRecordDao();
        groupRecordList = workRecordDao.queryBuilder().where(WorkRecordDao.Properties.Type.eq(0))
                .orderDesc(WorkRecordDao.Properties.Id).build().list();
        for(int i = 0 ; i < groupRecordList.size(); i ++){
            childRecordList =  workRecordDao.queryBuilder()
                    .where(WorkRecordDao.Properties.Date.eq(groupRecordList.get(i).getDate()), WorkRecordDao.Properties.Type.eq(1))
                    .orderDesc(WorkRecordDao.Properties.Id).build().list();
            groupRecordList.get(i).setChildList(childRecordList);
        }
        if(groupRecordList.size() > 0){
            groupRecordList.get(0).setExpand(true);
            childRecordList = groupRecordList.get(0).getChildList();
            groupRecordList.addAll(1,childRecordList);
        }
    }
}
