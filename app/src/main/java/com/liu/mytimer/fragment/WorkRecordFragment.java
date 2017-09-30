package com.liu.mytimer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.liu.mytimer.App;
import com.liu.mytimer.R;
import com.liu.mytimer.SecondActivity;
import com.liu.mytimer.adapter.WorkRecordAdapter;
import com.liu.mytimer.dialog.EditWorkRecordDialog;
import com.liu.mytimer.module.DaoSession;
import com.liu.mytimer.module.WorkRecord;
import com.liu.mytimer.module.WorkRecordDao;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by kunming.liu on 2017/9/28.
 */

public class WorkRecordFragment extends BaseFragment {
    private static WorkRecordFragment instance = null;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private List<WorkRecord> groupRecordList = null;
    private List<WorkRecord> childRecordList = null;
    private WorkRecordAdapter workRecordAdapter = null;
    private DaoSession daoSession = null;
    private WorkRecordDao workRecordDao = null;
    private WorkRecordAdapter.OnLongClickListener listener = null;

    private Unbinder unbinder;

    public static WorkRecordFragment getInstance(){
        if(instance == null){
            synchronized (WorkRecordFragment.class) {
                if (instance == null) {
                    instance = new WorkRecordFragment();
                }
            }
        }
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_work_record, container, false);
        unbinder = ButterKnife.bind(this, itemView);
        initDB();

        workRecordAdapter = new WorkRecordAdapter(getContext(),groupRecordList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
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
                editWorkRecordDialog.show(getChildFragmentManager(),"[EDIT_WORK_RECORD]");

            }
        };
        workRecordAdapter.setListener(listener);
        return itemView;
    }

    private void initDB(){
        daoSession = ((App)getActivity().getApplication()).getDaoSession();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
