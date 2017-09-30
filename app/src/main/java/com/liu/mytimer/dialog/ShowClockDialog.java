package com.liu.mytimer.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.liu.mytimer.App;
import com.liu.mytimer.R;
import com.liu.mytimer.module.DaoSession;
import com.liu.mytimer.module.WorkRecord;
import com.liu.mytimer.module.WorkRecordDao;
import com.liu.mytimer.view.TimeTableView;

import java.util.List;

/**
 * Created by kunming.liu on 2017/9/25.
 */

public class ShowClockDialog extends DialogFragment {
    private DaoSession daoSession = null;
    private WorkRecordDao workRecordDao = null;
    private TimeTableView timeTableView = null;
    private WorkRecord workRecord = null;
    private List<WorkRecord> childList = null;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.clock_dialog, container);
        timeTableView = (TimeTableView)view.findViewById(R.id.timeTableView);
        daoSession = ((App)getActivity().getApplication()).getDaoSession();
        workRecordDao = daoSession.getWorkRecordDao();
        workRecord = workRecordDao.queryBuilder()
                .where(WorkRecordDao.Properties.Date.eq("2017/09/26"),
                        WorkRecordDao.Properties.Type.eq(0))
                .list().get(0);
        childList = workRecordDao.queryBuilder()
                .where(WorkRecordDao.Properties.Date.eq("2017/09/26"),
                        WorkRecordDao.Properties.Type.eq(1))
                .orderDesc(WorkRecordDao.Properties.Id)
                .list();
        workRecord.setChildList(childList);
        //Log.e("workRecordList", "workRecordList size "+workRecordList.size());
        timeTableView.setGroupWorkRecord(workRecord);
        return view;
    }
}
