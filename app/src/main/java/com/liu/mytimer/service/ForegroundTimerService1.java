package com.liu.mytimer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.liu.mytimer.MainActivity;
import com.liu.mytimer.R;
import com.liu.mytimer.module.DaoSession;
import com.liu.mytimer.module.GroupWorkTime;
import com.liu.mytimer.module.GroupWorkTimeDao;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by kunming.liu on 2017/9/20.
 */
// TODO: 2017/9/23 如果切換到servcie的話 然後一直觀察時間的話，會大概慢個一秒鐘左右，然後會隨著時間，越慢越多
public class ForegroundTimerService1 extends Service {
    NotificationManager notificationManager = null;
    //    Notification.Builder builder = null;
    NotificationCompat.Builder builder = null;
    Intent intent = null;
    PendingIntent pi = null;
    Notification notification = null;
    private static Handler handler = null;
    private int hour;
    private int minute;
    private int second;
    private int milliSecond;
    private TimerRunnable timerRunnable = null;

    private RemoteViews remoteView = null;
    private MyBinder myBinder = null;
    private static boolean isPlayInService = false;

    private DaoSession daoSession = null;
    private GroupWorkTimeDao groupWorkTimeDao = null;
    private List<GroupWorkTime> groupWorkTimeList = null;
    private ScheduledThreadPoolExecutor exec;
    private ScheduledFuture scheduledFuture = null;
    private List<Future> futures= null;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("service", "onCreate");
        //android.app.remoteserviceexception bad notification posted from package
        remoteView = new RemoteViews(getPackageName(), R.layout.notification_layout2);

        timerRunnable = new TimerRunnable();
        myBinder = new MyBinder();
        hour = 0;
        minute = 0;
        second = 0;
        milliSecond = 0;

        isPlayInService = false;
        exec = new ScheduledThreadPoolExecutor(1);


        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        builder = new Notification.Builder(this);
        intent = new Intent(this, MainActivity.class);
        pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

//        builder.setSmallIcon(R.drawable.play)
//                .setContentIntent(pi)
//                .setContentTitle("This is title")
//                .setWhen(System.currentTimeMillis());
//        notification = builder.build();


        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.timer)
                .setCustomBigContentView(remoteView)//不使用big的話，按鈕出現不了
                //.setCustomContentView(remoteView)//如果不用NotificationCompat的話，此method必須要24以上
                .setContentIntent(pi)
                .setAutoCancel(true);
        notification = builder.build();


        Intent stopIntent = new Intent(this, PlayAndStopReceiver.class);
        stopIntent.putExtra("id", R.id.stop);

        Intent playIntent = new Intent(this, PlayAndStopReceiver.class);
        playIntent.putExtra("id", R.id.play);

        PendingIntent stopPI = PendingIntent.getBroadcast(this, 1, stopIntent, 0);
        PendingIntent playPI = PendingIntent.getBroadcast(this, 2, playIntent, 0);

        remoteView.setOnClickPendingIntent(R.id.play, playPI);
        remoteView.setOnClickPendingIntent(R.id.stop, stopPI);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("service", "onStartCommand");
        if (intent != null) {
            hour = intent.getIntExtra("hour", 0);
            minute = intent.getIntExtra("minute", 0);
            second = intent.getIntExtra("second", 0);
            milliSecond = intent.getIntExtra("millisecond", 0) * 10;

        }
        scheduledFuture = exec.scheduleAtFixedRate(timerRunnable, (1000 - milliSecond), 1 * 100, TimeUnit.MILLISECONDS);
        milliSecond = 0;
        startForeground(1, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("service", "onBind");
        return myBinder;
    }

    //activity丟過來的不一定是整數，可能是5X之類的，因此一直由100＋上去 ，會不符合==1000的條件，因此不會歸0
    //導致return activity的數字太大了
    private class TimerRunnable implements Runnable {

        @Override
        public void run() {
//            builder.setContentText()
            isPlayInService = true;
            milliSecond += 100;
            calToSecMinAndHour();
//            builder.setContentTitle(String.format("%02d:%02d:%02d",hour,minute,second));
            remoteView.setTextViewText(R.id.txtTile, String.format("%02d:%02d:%02d", hour, minute, second));
            notification = builder.build();
            notificationManager.notify(1, notification);
            //handler.postDelayed(this, 1 * 1000);


        }
    }

    private void calToSecMinAndHour() {
        if (milliSecond == 1000) {
            milliSecond = 0;
            second++;
        }
        if (second == 60) {
            second = 0;
            minute++;
        }
        if (minute == 60) {
            minute = 0;
            hour++;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("service", "onDestroy");
        isPlayInService = false;
        //有時後前台服務的圖示不會消失，但是目前尚未解決
        scheduledFuture.cancel(true);
        exec.schedule(new MyRunnable(),100,TimeUnit.MILLISECONDS);
        exec.shutdown();
        exec.shutdownNow();
        stopForeground(true);

    }

    public class MyBinder extends Binder {
        public int getHour() {
            return hour;
        }

        public int getMinute() {
            return minute;
        }

        public int getSecond() {
            return second;
        }

        public int getMilliSecond() {
            return milliSecond / 10;
        }

        public void stopService() {
            isPlayInService = false;
            handler.removeCallbacks(timerRunnable);
        }

        public void reStartService() {
            isPlayInService = true;
            handler.post(timerRunnable);
        }

        public boolean isPlayInService() {
            return isPlayInService;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("service", "onUnbind");
        //太神奇了，居然可以使用!!!!
//        daoSession = ((App)getApplication()).getDaoSession();
//        groupWorkTimeDao = daoSession.getGroupWorkTimeDao();
//        groupWorkTimeList = groupWorkTimeDao.queryBuilder().where(GroupWorkTimeDao.Properties.Type.eq(0))
//                .orderDesc(GroupWorkTimeDao.Properties.Id).build().list();
//        for(int i = 0 ; i < groupWorkTimeList.size(); i ++){
//            Log.e("service","i = "+i+" list id= "+groupWorkTimeList.get(i).getId());
//        }

        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.e("service", "onRebind");
        super.onRebind(intent);
    }

    //不加static ，會crush
    public static class PlayAndStopReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Here", "I am here");
            int id = intent.getIntExtra("id", -1);
            if (id == R.id.stop) {
                isPlayInService = false;
//                handler.removeCallbacks(timerRunnable);
            } else {
                isPlayInService = true;
//                handler.post(timerRunnable);
            }
        }
    }
    private class MyRunnable implements Runnable{

        @Override
        public void run() {
//            builder.setPriority(NotificationCompat.PRIORITY_MIN);
//            notification = builder.build();
//            notificationManager.notify(1, notification);
        }
    }

}
