package com.liu.mytimer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.liu.mytimer.dialog.SaveWorkRecordDialog;
import com.liu.mytimer.service.ForegroundTimerService2;
import com.liu.mytimer.utils.Prefs;
import com.liu.mytimer.utils.Util;
import com.liu.mytimer.module.DaoSession;
import com.liu.mytimer.module.WorkRecord;
import com.liu.mytimer.module.WorkRecordDao;
import com.liu.mytimer.service.ForegroundTimerService1;
import com.liu.mytimer.view.TimerView1;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {
    @BindView(R.id.timer)
    TimerView1 timer;
    @BindView(R.id.play)
    ImageView play;
    @BindView(R.id.pause)
    ImageView pause;
    @BindView(R.id.stop)
    ImageView stop;
    @BindView(R.id.setting)
    ImageView setting;
    @BindView(R.id.replay)
    ImageView replay;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Util.log("onCreate");
        if (timeFormat == null) {
//            timeFormat = new SimpleDateFormat("hh:mm:ss"); 0-12
            timeFormat = new SimpleDateFormat("HH:mm:ss"); //0-24
        }
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        }
        if (Prefs.isFirstTime(MainActivity.this)) {
            startTest();
            Prefs.saveFirstTime(MainActivity.this,false);
        }

        textView = (TextView)findViewById(R.id.text);

//        long x = 344321;
//        float y = (float)x/1000.0f;
//        Util.log("y = %f",y);
//        float z = y - x/1000;
//        Util.log("z = %f",z*100);


    }

    @Override
    protected void onResume() {
        super.onResume();
        Util.log("onResume : %s","onResume");
        //去檢查service是否有正在運行，如果有的話，就取得現在service的時間給timer
        isServiceRunning = isTimerServiceRunning(ForegroundTimerService2.class);
        if (serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {

                    myBinder = (ForegroundTimerService2.MyBinder) service;
                    //post會導致畫面會先出現00000，再去刷新
//                    timer.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (myBinder != null) {
//                                timer.setTime(myBinder.getHour(), myBinder.getMinute(), myBinder.getSecond(), myBinder.isRunning());
//                            }
//                        }
//                    });
                    Util.log("server return millisecond : %d",myBinder.getMilliSecond());
                    //取得現在service所跑到的時分秒，然後刷新畫面
                    timer.reStartTimer(myBinder.getHour(), myBinder.getMinute(), myBinder.getSecond(),myBinder.getMilliSecond()+(Calendar.getInstance().get(Calendar.MILLISECOND)-myBinder.getNow()), myBinder.isPlayInService());
                    isPlay = myBinder.isPlayInService();
                    if (play.getVisibility() == View.VISIBLE) {
                        startPlayAnimation(myBinder.isPlayInService());
                    }
                    //拿到資料後，就可以把service銷毀了
                    unbindService(serviceConnection);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Util.log("onServiceDisconnected");
                }
            };
        }
        //如果發現service已經在運行了，就做bind(因為要從service中取出所跑到得時分秒)
        if (isServiceRunning == true) {
            Intent intent = new Intent(MainActivity.this, ForegroundTimerService2.class);
            //startService(intent);
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        }
        Util.log("onResume");

    }


    /**
     * 當程式進入背景的時候，則就啟動serivce
     */
    @Override
    protected void onPause() {
        super.onPause();
//        if(myBinder != null){
//            unbindService(serviceConnection);
//        }
        //判斷是否已經開始了，若開始了才需要啟動service
        if (isPlay == true) {
            timer.pauseTimer();
            Log.e("test", "onPause");
            Intent intent = new Intent(MainActivity.this, ForegroundTimerService2.class);
            intent.putExtra("hour", timer.getHour());
            intent.putExtra("minute", timer.getMinute());
            intent.putExtra("second", timer.getSecond());
            intent.putExtra("millisecond", timer.getMilliSecond());
            intent.putExtra("now", Calendar.getInstance().getTimeInMillis());
            intent.setAction(ForegroundTimerService2.MAIN_ACTION);
            startService(intent);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @OnClick({R.id.play, R.id.pause, R.id.stop, R.id.setting, R.id.replay})
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
            case R.id.setting:
                // TODO: 2017/9/19 跳出fragment 顯示recyclerView
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
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
        Drawable drawable = ContextCompat.getDrawable(MainActivity.this,R.drawable.black_stop);
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());//一定要呼叫這個，不然下面method部會有效果
        textView.setText("test123");
        textView.setCompoundDrawables(drawable, null,null,null);
        //textView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.black_pause, 0, 0);//也可以直接用這個
        isPlay = true;//計時中
        startPlayAnimation(true);//啟動動畫，讓畫面上出現暫停及停止按鈕

        //分別紀錄開始的日期及時間
        calendar = Calendar.getInstance();
        Prefs.saveStartTime(MainActivity.this, timeFormat.format(calendar.getTime()));
        Prefs.saveStartDate(MainActivity.this, dateFormat.format(calendar.getTime()));
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
        Prefs.saveEndTime(MainActivity.this, timeFormat.format(calendar.getTime()));

        //讓timer歸零並停止計時
        timer.stopTimer();

        replay.setVisibility(View.VISIBLE);//顯示續播放鍵，準備下次的計時
        pause.setVisibility(View.GONE);//隱藏暫停鍵

        //出現對話窗，讓使用者可以維護工作內容
        if (saveWorkRecordDialog == null) {
            saveWorkRecordDialog = new SaveWorkRecordDialog();
        }
        saveWorkRecordDialog.show(getSupportFragmentManager(), "[SAVE_WORK_RECORD]");

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
            Prefs.saveStartTime(MainActivity.this, timeFormat.format(calendar.getTime()));
            isFinish = false;
        } else {
            //代表只是從暫停狀態繼續播放
            //timer.startTimer();
            timer.reStartTimer();
        }


    }

    /**
     * 檢查service是否有在運行
     * @param serviceClass
     * @return true : service is running
     *         false : service is already stop
     */
    private boolean isTimerServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
        DaoSession daoSession = ((App) getApplication()).getDaoSession();
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
