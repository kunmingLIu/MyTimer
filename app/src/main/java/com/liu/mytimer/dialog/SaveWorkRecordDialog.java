package com.liu.mytimer.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.liu.mytimer.App;
import com.liu.mytimer.R;
import com.liu.mytimer.utils.Prefs;
import com.liu.mytimer.module.DaoSession;
import com.liu.mytimer.module.WorkRecord;
import com.liu.mytimer.module.WorkRecordDao;
import com.liu.mytimer.utils.Util;

import org.greenrobot.greendao.query.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by kunming.liu on 2017/9/19.
 */

public class SaveWorkRecordDialog extends DialogFragment {
    @BindView(R.id.txtStartTime)
    TextView txtStartTime;
    @BindView(R.id.txtEndTime)
    TextView txtEndTime;
    @BindView(R.id.editContent)
    EditText editContent;
    @BindView(R.id.btnOK)
    Button btnOK;
    @BindView(R.id.txtTotalTime)
    TextView txtTotalTime;
    private Unbinder unbinder;
    private String mStartTime;
    private String mEndTime;
    private SimpleDateFormat simpleDateFormat = null;
    private Calendar calendar ;
    private Calendar calendar1;
    private long totalWorkTime = 0;
    private DaoSession daoSession = null;
    private WorkRecord workRecord;
    private WorkRecordDao workRecordDao = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);//可以去掉title
        setCancelable(false);//不能被取消，不用加getDialog
        View view = inflater.inflate(R.layout.write_content_dialog, container);
        unbinder = ButterKnife.bind(this, view);

        mStartTime = Prefs.getStartTime(getContext());
        mEndTime = Prefs.getEndTime(getContext());

        txtStartTime.setText(mStartTime);
        txtEndTime.setText(mEndTime);

        if(simpleDateFormat == null){
            simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        }
        try {
            calendar = Calendar.getInstance();
            calendar1 = Calendar.getInstance();
            calendar.setTime(simpleDateFormat.parse(mStartTime));
            calendar1.setTime(simpleDateFormat.parse(mEndTime));
            totalWorkTime = calendar1.getTimeInMillis() - calendar.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        txtTotalTime.setText(Util.covertTimeToString(totalWorkTime));


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick( R.id.btnOK)
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.btnOK:
                saveDataInDB(getContext());
                getDialog().dismiss();
                break;
        }
    }

    private void saveDataInDB(Context context){
        if(daoSession == null){
            daoSession = ((App)getActivity().getApplication()).getDaoSession();
        }
        workRecordDao = daoSession.getWorkRecordDao();
        Query<WorkRecord> workRecordQuery = workRecordDao.queryBuilder().where(WorkRecordDao.Properties.Date.eq(Prefs.getStartDate(context))).build();

        if(workRecordQuery.list().size() == 0){
            workRecord = new WorkRecord();
            workRecord.setDate(Prefs.getStartDate(context));
            workRecord.setExpand(false);
            workRecord.setType(0);
            workRecord.setTotalWorkTime(totalWorkTime);
            workRecordDao.insert(workRecord);
        }else{
            workRecord = workRecordQuery.list().get(0);
            workRecord.setExpand(false);
            workRecord.setType(0);
            workRecord.setTotalWorkTime(workRecord.getTotalWorkTime()+totalWorkTime);
            workRecordDao.update(workRecord);
        }
        workRecord = new WorkRecord();
        workRecord.setDate(Prefs.getStartDate(context));
        workRecord.setStartTime(Prefs.getStartTime(context));
        workRecord.setEndTime(Prefs.getEndTime(context));
        workRecord.setWorkContent(editContent.getText().toString());
        workRecord.setTotalWorkTime(totalWorkTime);
        workRecord.setType(1);
        workRecordDao.insert(workRecord);

    }

}

