package com.liu.mytimer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.liu.mytimer.Utils.Prefs;
import com.liu.mytimer.Utils.Util;
import com.liu.mytimer.dialog.MyDialog;
import com.liu.mytimer.dialog.WorkDialog;
import com.liu.mytimer.module.DaoMaster;
import com.liu.mytimer.module.DaoSession;
import com.liu.mytimer.module.GroupWorkTime;
import com.liu.mytimer.module.GroupWorkTimeDao;
import com.liu.mytimer.service.ForegroundTimerService;
import com.liu.mytimer.service.ForegroundTimerService1;
import com.liu.mytimer.view.TimerView;
import com.liu.mytimer.view.TimerView1;

import org.greenrobot.greendao.database.Database;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;

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
    private WorkDialog writeContentDialog = null;
    private Calendar calendar;
    private SimpleDateFormat timeFormat = null;
    private SimpleDateFormat dateFormat = null;
    private ServiceConnection serviceConnection = null;
    private ForegroundTimerService1.MyBinder myBinder = null;
    private boolean isServiceRunning = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (timeFormat == null) {
//            timeFormat = new SimpleDateFormat("hh:mm:ss"); 0-12
            timeFormat = new SimpleDateFormat("HH:mm:ss"); //0-24
        }
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        }
//        startTest();
//        long x = 344321;
//        float y = (float)x/1000.0f;
//        Util.log("y = %f",y);
//        float z = y - x/1000;
//        Util.log("z = %f",z*100);


    }

    @Override
    protected void onResume() {
        super.onResume();

        //去檢查service是否有正在運行，如果有的話，就取得現在service的時間給timer
        isServiceRunning = isTimerServiceRunning(ForegroundTimerService1.class);
        if (serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    myBinder = (ForegroundTimerService1.MyBinder) service;
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
                    timer.reStartTimer(myBinder.getHour(), myBinder.getMinute(), myBinder.getSecond(),myBinder.getMilliSecond(), myBinder.isPlayInService());
                    isPlay = myBinder.isPlayInService();
                    if (play.getVisibility() == View.VISIBLE) {
                        startPlayAnimation(myBinder.isPlayInService());
                    }
                    //拿到資料後，就可以把service銷毀了
                    Intent intent = new Intent(MainActivity.this, ForegroundTimerService1.class);
                    unbindService(serviceConnection);
                    stopService(intent);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            };
        }
        //如果發現service已經在運行了，就做bind(因為要從service中取出所跑到得時分秒)
        if (isServiceRunning == true) {
            Intent intent = new Intent(MainActivity.this, ForegroundTimerService1.class);
            //startService(intent);
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        }
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
            Intent intent = new Intent(MainActivity.this, ForegroundTimerService1.class);
            intent.putExtra("hour", timer.getHour());
            intent.putExtra("minute", timer.getMinute());
            intent.putExtra("second", timer.getSecond());
            intent.putExtra("millisecond", timer.getMilliSecond());
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
        if (writeContentDialog == null) {
            writeContentDialog = new WorkDialog();
        }
        writeContentDialog.show(getSupportFragmentManager(), "[WORK_CONTENT]");

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
        //        saveTestDataInDB("2017/09/22"
//                ,new String[]{"00:00:00","12:00:00","16:00:00","18:00:00"}
//                ,new String[]{"11:00:00","15:00:00","17:00:00","19:00:00"});
//        saveTestDataInDB("2017/09/23"
//                ,new String[]{"00:00:00","08:00:00","10:00:00","16:00:00","18:00:00","19:00:00","20:00:00","22:00:00","22:20:00","23:00:00"}
//                ,new String[]{"06:00:00","09:00:00","12:00:00","18:00:00","19:00:00","20:00:00","21:00:00","22:10:00","22:30:00","23:59:00"});
//        saveTestDataInDB("2017/09/24"
//                ,new String[]{"00:00:00","12:00:00","16:00:00","18:00:00","20:00:00","21:00:00"}
//                ,new String[]{"11:00:00","15:00:00","17:00:00","19:00:00","20:25:00","23:25:00"});
//        saveTestDataInDB("2017/09/25"
//                ,new String[]{"00:00:00","12:00:00","16:00:00","18:00:00","19:00:00","20:00:00","22:00:00"}
//                ,new String[]{"11:00:00","15:00:00","17:00:00","19:00:00","19:30:00","21:00:00","23:00:00"});
//        saveTestDataInDB("2017/09/26"
//                ,new String[]{"00:00:00","12:00:00","16:00:00","18:00:00","20:00:00","22:00:00","23:30:00"}
//                ,new String[]{"11:00:00","15:00:00","17:00:00","19:00:00","21:00:00","23:00:00","23:35:00"});
    }

    private void saveTestDataInDB(String date, String[] startTime, String[] endTime) {
        DaoSession daoSession = ((App) getApplication()).getDaoSession();
        GroupWorkTimeDao groupWorkTimeDao = daoSession.getGroupWorkTimeDao();

        GroupWorkTime groupWorkTime = new GroupWorkTime();
        groupWorkTime.setDate(date);
        groupWorkTime.setChildCount(startTime.length);
        groupWorkTime.setExpand(false);
        groupWorkTime.setType(0);
        groupWorkTimeDao.insert(groupWorkTime);

        for (int i = 0; i < startTime.length; i++) {
            groupWorkTime = new GroupWorkTime();
            groupWorkTime.setDate(date);
            groupWorkTime.setStartTime(startTime[i]);
            groupWorkTime.setEndTime(endTime[i]);
            groupWorkTime.setWorkContent("測試" + i);
            groupWorkTime.setChildCount(0);
            groupWorkTime.setTotalTime("01:00:00");
            groupWorkTime.setType(1);
            groupWorkTimeDao.insert(groupWorkTime);
        }

    }
}
