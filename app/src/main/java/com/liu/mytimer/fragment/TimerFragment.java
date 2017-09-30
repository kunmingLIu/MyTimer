package com.liu.mytimer.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.liu.mytimer.App;
import com.liu.mytimer.MainActivity;
import com.liu.mytimer.R;
import com.liu.mytimer.dialog.SaveWorkRecordDialog;
import com.liu.mytimer.module.DaoSession;
import com.liu.mytimer.module.WorkRecord;
import com.liu.mytimer.module.WorkRecordDao;
import com.liu.mytimer.service.ForegroundTimerService2;
import com.liu.mytimer.utils.Prefs;
import com.liu.mytimer.view.TimerView1;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by kunming.liu on 2017/9/28.
 */

public class TimerFragment extends BaseFragment {
    private static TimerFragment instance = null;
    @BindView(R.id.timer)
    TimerView1 timer;
    @BindView(R.id.play)
    ImageView play;
    @BindView(R.id.pause)
    ImageView pause;
    @BindView(R.id.replay)
    ImageView replay;
    @BindView(R.id.stop)
    ImageView stop;

    private Unbinder unbinder;

    private boolean isPlay = false;
    private boolean isFinish = false;
    private SaveWorkRecordDialog saveWorkRecordDialog = null;
    private Calendar calendar;
    private SimpleDateFormat timeFormat = null;
    private SimpleDateFormat dateFormat = null;
    private ServiceConnection serviceConnection = null;
    private ForegroundTimerService2.MyBinder myBinder = null;
    private boolean isServiceRunning = false;
    private TextView textView = null;

    public static TimerFragment getInstance() {
        if (instance == null) {
            synchronized (TimerFragment.class) {
                if (instance == null) {
                    instance = new TimerFragment();
                }
            }
        }
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_timer, container, false);
        unbinder = ButterKnife.bind(this, itemView);

        if (timeFormat == null) {
//            timeFormat = new SimpleDateFormat("hh:mm:ss"); 0-12
            timeFormat = new SimpleDateFormat("HH:mm:ss"); //0-24
        }
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        }
        if (Prefs.isFirstTime(getContext())) {
            startTest();
            Prefs.saveFirstTime(getContext(),false);
        }

        return itemView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.play, R.id.pause, R.id.replay, R.id.stop})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.play:
                clickPlay();
                break;
            case R.id.pause:
                clickPause();
                break;
            case R.id.replay:
                clickReplay();
                break;
            case R.id.stop:
                clickStop();
                break;
        }
    }
    /**
     * 把play鍵隱藏起來，然後出現暫停跟停止
     */
    private void startPlayAnimation(boolean isPlayInService) {

        stop.setVisibility(View.VISIBLE);
        play.setVisibility(View.GONE);
        //如果service現在有在計時，那應該是出現暫停按鈕;反之，就出現replay
        if (isPlayInService) {
            replay.setVisibility(View.GONE);
            pause.setVisibility(View.VISIBLE);
        } else {
            replay.setVisibility(View.VISIBLE);
            pause.setVisibility(View.GONE);
        }


        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(stop, "translationX", 0, -100),
                ObjectAnimator.ofFloat(pause, "translationX", 0, 100),
                ObjectAnimator.ofFloat(replay, "translationX", 0, 100),
                ObjectAnimator.ofFloat(pause, "scaleX", 0f, 1f),
                ObjectAnimator.ofFloat(pause, "scaleY", 0f, 1f),
                ObjectAnimator.ofFloat(stop, "scaleX", 0f, 1f),
                ObjectAnimator.ofFloat(stop, "scaleY", 0f, 1f),
                ObjectAnimator.ofFloat(pause, "alpha", 0f, 1),
                ObjectAnimator.ofFloat(stop, "alpha", 0f, 1),
                ObjectAnimator.ofFloat(play, "alpha", 1f, 0));
        animatorSet.setDuration(1 * 1000);
        animatorSet.start();
    }

    /**
     * 點了開始按鈕
     */
    private void clickPlay() {
        isPlay = true;//計時中
        startPlayAnimation(true);//啟動動畫，讓畫面上出現暫停及停止按鈕

        //分別紀錄開始的日期及時間
        calendar = Calendar.getInstance();
        Prefs.saveStartTime(getContext(), timeFormat.format(calendar.getTime()));
        Prefs.saveStartDate(getContext(), dateFormat.format(calendar.getTime()));
        //刷新timer，並開始計時及啟動動畫
        timer.startTimer(0, 0, 0,0);
    }

    /**
     * 點了停止按鈕
     */
    private void clickStop() {
        isPlay = false;//停止計時
        isFinish = true;//這次的計時到此結束
        //記錄這次的停止時間
        calendar = Calendar.getInstance();
        Prefs.saveEndTime(getContext(), timeFormat.format(calendar.getTime()));

        //讓timer歸零並停止計時
        timer.stopTimer();

        replay.setVisibility(View.VISIBLE);//顯示續播放鍵，準備下次的計時
        pause.setVisibility(View.GONE);//隱藏暫停鍵

        //出現對話窗，讓使用者可以維護工作內容
        if (saveWorkRecordDialog == null) {
            saveWorkRecordDialog = new SaveWorkRecordDialog();
        }
        saveWorkRecordDialog.show(getChildFragmentManager(), "[SAVE_WORK_RECORD]");

    }

    /**
     * 按了暫停按鈕
     */
    private void clickPause() {
        isPlay = false;//暫停
        pause.setVisibility(View.GONE);//隱藏暫停鍵
        replay.setVisibility(View.VISIBLE);//出現繼續播放鍵

        timer.pauseTimer();//停止timer的動畫跟計時
    }

    /**
     * 按了繼續播放鍵
     */
    private void clickReplay() {
        isPlay = true;//計時中
        pause.setVisibility(View.VISIBLE);//出現暫停鍵
        replay.setVisibility(View.GONE);//隱藏繼續播放鍵

        //isFinish = true，代表上次的計時已經結束了，這次要開始新的計時
        if (isFinish) {
            calendar = Calendar.getInstance();
            Prefs.saveStartTime(getContext(), timeFormat.format(calendar.getTime()));
            isFinish = false;
        } else {
            //代表只是從暫停狀態繼續播放
            //timer.startTimer();
            timer.reStartTimer();
        }


    }
    private void startTest() {
        saveTestDataInDB("2017/09/22"
                ,new String[]{"00:00:00","12:00:00","16:00:00","18:00:00"}
                ,new String[]{"11:00:00","15:00:00","17:00:00","19:00:00"});
        saveTestDataInDB("2017/09/23"
                ,new String[]{"00:00:00","08:00:00","10:00:00","16:00:00","18:00:00","19:00:00","20:00:00","22:00:00","22:20:00","23:00:00"}
                ,new String[]{"06:00:00","09:00:00","12:00:00","18:00:00","19:00:00","20:00:00","21:00:00","22:10:00","22:30:00","23:59:00"});
        saveTestDataInDB("2017/09/24"
                ,new String[]{"00:00:00","12:00:00","16:00:00","18:00:00","20:00:00","21:00:00"}
                ,new String[]{"11:00:00","15:00:00","17:00:00","19:00:00","20:25:00","23:25:00"});
        saveTestDataInDB("2017/09/25"
                ,new String[]{"00:00:00","12:00:00","16:00:00","18:00:00","19:00:00","20:00:00","22:00:00"}
                ,new String[]{"11:00:00","15:00:00","17:00:00","19:00:00","19:30:00","21:00:00","23:00:00"});
        saveTestDataInDB("2017/09/26"
                ,new String[]{"00:00:00","12:00:00","16:00:00","18:00:00","20:00:00","22:00:00","23:30:00"}
                ,new String[]{"11:00:00","15:00:00","17:00:00","19:00:00","21:00:00","23:00:00","23:35:00"});
    }

    private void saveTestDataInDB(String date, String[] startTime, String[] endTime) {
        DaoSession daoSession = ((App) getActivity().getApplication()).getDaoSession();
        WorkRecordDao workRecordDao = daoSession.getWorkRecordDao();

        WorkRecord workRecord = new WorkRecord();
        workRecord.setDate(date);
        workRecord.setExpand(false);
        workRecord.setType(0);
        workRecord.setTotalWorkTime(3600000*startTime.length);
        workRecordDao.insert(workRecord);

        LinkedList<WorkRecord> childList = new LinkedList<>();
        WorkRecord child = null;
        for (int i = 0; i < startTime.length; i++) {
            child = new WorkRecord();
            child.setDate(date);
            child.setStartTime(startTime[i]);
            child.setEndTime(endTime[i]);
            child.setWorkContent("測試" + i);
            child.setTotalWorkTime(3600000);
            child.setType(1);
            workRecordDao.insert(child);
            childList.add(child);
        }
        workRecord.setChildList(childList);
    }
}
