package com.liu.mytimer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.liu.mytimer.adapter.WorkTimeAdapter;
import com.liu.mytimer.module.DaoSession;
import com.liu.mytimer.module.GroupWorkTime;
import com.liu.mytimer.module.GroupWorkTimeDao;

import org.greenrobot.greendao.query.Query;

import java.util.List;

/**
 * Created by kunming.liu on 2017/9/19.
 */

public class SecondActivity  extends AppCompatActivity {
    private RecyclerView recyclerView = null;
    private List<GroupWorkTime> groupWorkTimeList = null;
    private WorkTimeAdapter workTimeAdapter = null;
    private DaoSession daoSession = null;
    private GroupWorkTimeDao groupWorkTimeDao = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        initDB();

        workTimeAdapter = new WorkTimeAdapter(SecondActivity.this,groupWorkTimeList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(SecondActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(workTimeAdapter);
    }
    private void initDB(){
        daoSession = ((App)getApplication()).getDaoSession();
        groupWorkTimeDao = daoSession.getGroupWorkTimeDao();
        groupWorkTimeList = groupWorkTimeDao.queryBuilder().where(GroupWorkTimeDao.Properties.Type.eq(0))
                .orderDesc(GroupWorkTimeDao.Properties.Id).build().list();
        if(groupWorkTimeList.size() > 0){
            //預設第一個展開子內容
            groupWorkTimeList.get(0).setExpand(true);
            Query<GroupWorkTime> workTimeQuery = groupWorkTimeDao.queryBuilder()
                    .where(GroupWorkTimeDao.Properties.Date.eq(groupWorkTimeList.get(0).getDate()), GroupWorkTimeDao.Properties.Type.eq(1))
                    .orderDesc(GroupWorkTimeDao.Properties.Id).build();
            List<GroupWorkTime> queryList = workTimeQuery.list();
            for(int i = queryList.size() ; i >= 1 ; i--){
                groupWorkTimeList.add(1,queryList.get(i-1));
            }
        }

    }
}
