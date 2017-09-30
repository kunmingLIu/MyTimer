package com.liu.mytimer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.liu.mytimer.MainActivity;
import com.liu.mytimer.R;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;

/**
 * Created by kunming.liu on 2017/9/20.
 */
// TODO: 2017/9/23 如果切換到servcie的話 然後一直觀察時間的話，會大概慢個一秒鐘左右，然後會隨著時間，越慢越多
public class ForegroundTimerService2 extends Service {
    NotificationManager notificationManager = null;
    NotificationCompat.Builder builder = null;
    Intent intent = null;
    PendingIntent pi = null;
    Notification notification = null;
    private Handler handler = null;
    private int hour;
    private int minute;
    private int second;
    private int milliSecond;
    private TimerRunnable timerRunnable = null;

    private RemoteViews remoteView = null;
    private MyBinder myBinder = null;
    private boolean isPlayInService = false;


    private ScheduledThreadPoolExecutor exec;
    private ScheduledFuture scheduledFuture = null;
    private long now;

    public static String MAIN_ACTION = "com.liu.mytimer.service.action.main";
    public static String PAUSE_ACTION = "com.liu.mytimer.service.action.pause";
    public static String STOP_ACTION = "com.liu.mytimer.service.action.stop";
    public static String PLAY_ACTION = "com.liu.mytimer.service.action.play";

    private Intent leftIntent;
    private Intent rightIntent;
    private PendingIntent leftPI;
    private PendingIntent rightPI;
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
       handler = new Handler();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        builder = new Notification.Builder(this);
        intent = new Intent(this, MainActivity.class);
        pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

//        builder.setSmallIcon(R.drawable.play)
//                .setContentIntent(pi)
//                .setContentTitle("This is title")
//                .setWhen(System.currentTimeMillis());
//        notification = builder.build();

//        Bitmap icon = BitmapFactory.decodeResource(getResources(),
//                R.drawable.ic_timer_white_48px);

        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.timer)
                .setContent(remoteView) //一般狀態下的樣式，如果不加這個，當服務不是在順序第一位的時候，就只會出現空白
                .setCustomBigContentView(remoteView)//不使用big的話，按鈕出現不了
                .setContentIntent(pi)
                .setPriority(PRIORITY_MAX)
                .setContentInfo(String.format("%02d:%02d:%02d", hour, minute, second))
                .setAutoCancel(true);
        notification = builder.build();

        leftIntent = new Intent(this, ForegroundTimerService2.class);
        leftIntent.setAction(STOP_ACTION);

        rightIntent = new Intent(this, ForegroundTimerService2.class);
        rightIntent.setAction(PAUSE_ACTION);
//
        leftPI = PendingIntent.getService(this,1,leftIntent,0);
        rightPI = PendingIntent.getService(this, 2, rightIntent, 0);
//
        remoteView.setOnClickPendingIntent(R.id.left, leftPI);
        remoteView.setOnClickPendingIntent(R.id.right, rightPI);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("service", "onStartCommand");
        if (intent != null) {
            if(intent.getAction().equals(MAIN_ACTION)){
                hour = intent.getIntExtra("hour", 0);
                minute = intent.getIntExtra("minute", 0);
                second = intent.getIntExtra("second", 0);
                milliSecond = intent.getIntExtra("millisecond", 0) * 10;
                now = intent.getLongExtra("now", 0);
                Log.e("service", "milliSecond : "+milliSecond);
                Log.e("service", "(int)(Calendar.getInstance().getTimeInMillis()-now) : "+(int)(Calendar.getInstance().getTimeInMillis()-now));
                milliSecond = milliSecond + (int)(Calendar.getInstance().getTimeInMillis()-now);

                handler.postDelayed(timerRunnable , (1000-milliSecond));
                milliSecond = 0;
                startForeground(1, notification);
            }else{
                leftIntent = new Intent(this, ForegroundTimerService2.class);
                rightIntent = new Intent(this, ForegroundTimerService2.class);

                if(intent.getAction().equals(PLAY_ACTION)){
                    remoteView.setImageViewResource(R.id.right_icon,R.drawable.black_pause);
                    remoteView.setTextViewText(R.id.right_text,getString(R.string.pause));
                    rightIntent.setAction(PAUSE_ACTION);
                    isPlayInService = true;
                    handler.postDelayed(timerRunnable, (1000-milliSecond));
                    milliSecond = 0;
                }else if(intent.getAction().equals(STOP_ACTION)){
                    remoteView.setImageViewResource(R.id.right_icon,R.drawable.black_stop);
                }else if(intent.getAction().equals(PAUSE_ACTION)){
                    handler.removeCallbacks(timerRunnable);
                    isPlayInService = false;
                    remoteView.setImageViewResource(R.id.right_icon,R.drawable.black_play);
                    remoteView.setTextViewText(R.id.right_text,getString(R.string.play));
                    rightIntent.setAction(PLAY_ACTION);
                }

                leftPI = PendingIntent.getService(this,1,leftIntent,0);
                rightPI = PendingIntent.getService(this, 2, rightIntent, 0);

                remoteView.setOnClickPendingIntent(R.id.left, leftPI);
                remoteView.setOnClickPendingIntent(R.id.right, rightPI);
                notificationManager.notify(1, notification);
            }
        }
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
            milliSecond += 250;
            calToSecMinAndHour();
//            builder.setContentTitle(String.format("%02d:%02d:%02d",hour,minute,second));
            remoteView.setTextViewText(R.id.txtTile, String.format("%02d:%02d:%02d", hour, minute, second));

            builder.setContentInfo(String.format("%02d:%02d:%02d", hour, minute, second));
            notification = builder.build();
            notificationManager.notify(1, notification);
            handler.postDelayed(this, 1 * 250);


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

        public int getNow(){
            return Calendar.getInstance().get(Calendar.MILLISECOND);
        }

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("service", "onUnbind");
        handler.removeCallbacks(timerRunnable);
        stopForeground(true);
        stopSelf();
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.e("service", "onRebind");
        super.onRebind(intent);
    }
}
