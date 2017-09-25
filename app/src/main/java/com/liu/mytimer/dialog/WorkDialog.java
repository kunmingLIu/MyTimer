package com.liu.mytimer.dialog;

import android.content.Context;
import android.graphics.Color;
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
import com.liu.mytimer.Utils.Prefs;
import com.liu.mytimer.module.DaoSession;
import com.liu.mytimer.module.GroupWorkTime;
import com.liu.mytimer.module.GroupWorkTimeDao;

import org.greenrobot.greendao.query.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by kunming.liu on 2017/9/19.
 */

public class WorkDialog extends DialogFragment {
    @BindView(R.id.txtStartTime)
    TextView txtStartTime;
    @BindView(R.id.txtEndTime)
    TextView txtEndTime;
    @BindView(R.id.txtPause)
    TextView txtPause;
    @BindView(R.id.btnExpand)
    Button btnExpand;
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
    private GroupWorkTime groupWorkTime;
    private GroupWorkTimeDao groudWorkTimeDao = null;

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
        txtTotalTime.setText(getTimeString(totalWorkTime));


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.btnExpand, R.id.btnOK})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnExpand:
                break;
            case R.id.btnOK:
                saveDataInDB(getContext());
                getDialog().dismiss();
                break;
        }
    }
    private String getTimeString(long totalTime){

        long second = totalTime / 1000;
        long minute = 0;
        long hour = 0;

        if(second >= 60){
            minute = second /60;
            second = second % 60;
        }
        if(minute >= 60){
            hour = minute /60;
            minute = minute % 60;
        }

        String totalTimeString = "";
        totalTimeString = String.format("%02d:%02d:%02d",hour,minute,second);

        return totalTimeString;
    }

    private void saveDataInDB(Context context){
        if(daoSession == null){
            daoSession = ((App)getActivity().getApplication()).getDaoSession();
        }
        groudWorkTimeDao = daoSession.getGroupWorkTimeDao();
        Query<GroupWorkTime> groupWorkTimeQuery = groudWorkTimeDao.queryBuilder().where(GroupWorkTimeDao.Properties.Date.eq(Prefs.getStartDate(context))).build();

        if(groupWorkTimeQuery.list().size() == 0){
            groupWorkTime = new GroupWorkTime();
            groupWorkTime.setDate(Prefs.getStartDate(context));
            groupWorkTime.setChildCount(1);
            groupWorkTime.setExpand(false);
            groupWorkTime.setType(0);
            groudWorkTimeDao.insert(groupWorkTime);
        }else{
            groupWorkTime = groupWorkTimeQuery.list().get(0);
            groupWorkTime.setChildCount(groupWorkTime.getChildCount()+1);
            groupWorkTime.setExpand(false);
            groupWorkTime.setType(0);
            groudWorkTimeDao.update(groupWorkTime);
        }
        groupWorkTime = new GroupWorkTime();
        groupWorkTime.setDate(Prefs.getStartDate(context));
        groupWorkTime.setStartTime(Prefs.getStartTime(context));
        groupWorkTime.setEndTime(Prefs.getEndTime(context));
        groupWorkTime.setWorkContent(editContent.getText().toString());
        groupWorkTime.setChildCount(0);
        groupWorkTime.setTotalTime(getTimeString(totalWorkTime));
        groupWorkTime.setType(1);
        groudWorkTimeDao.insert(groupWorkTime);
//        workTime = new WorkTime();
//        workTime.setDate(Prefs.getStartDate(context));
//        workTime.setStartTime(Prefs.getStartTime(context));
//        workTime.setEndTime(Prefs.getEndTime(context));
//        workTime.setWorkContent(editContent.getText().toString());
//        workTime.setTotalTime(getTimeString(totalWorkTime));
//        workTime.setType(1);
//        workTimeDao.insert(workTime);

    }

}

