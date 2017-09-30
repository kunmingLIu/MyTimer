package com.liu.mytimer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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


    private ScheduledThreadPoolExecutor exec;
    private ScheduledFuture scheduledFuture = null;
    private int now;

    public static String MAIN_ACTION = "com.marothiatechs.customnotification.action.main";
    public static String INIT_ACTION = "com.marothiatechs.customnotification.action.init";
    public static String STOP_ACTION = "com.marothiatechs.customnotification.action.prev";
    public static String PLAY_ACTION = "com.marothiatechs.customnotification.action.play";
    public static String NEXT_ACTION = "com.marothiatechs.customnotification.action.next";
    public static String STARTFOREGROUND_ACTION = "com.marothiatechs.customnotification.action.startforeground";
    public static String STOPFOREGROUND_ACTION = "com.marothiatechs.customnotification.action.stopforeground";

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

//        Bitmap icon = BitmapFactory.decodeResource(getResources(),
//                R.drawable.ic_timer_white_48px);

        Bitmap icon = drawableToBitmap(ContextCompat.getDrawable(this,R.drawable.timer));

        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.timer)
                //.setContent(remoteView) //一般狀態下的樣式，如果不加這個，當服務不是在順序第一位的時候，就只會出現空白
                .setCustomBigContentView(remoteView)//不使用big的話，按鈕出現不了
                //.setCustomContentView(remoteView)//如果不用NotificationCompat的話，此method必須要24以上
                //.setContentIntent(pi)
                .setPriority(PRIORITY_MAX)
                .setContentInfo(String.format("%02d:%02d:%02d", hour, minute, second))
                .setAutoCancel(true);
        notification = builder.build();

        //set click listener
        Intent leftIntent = new Intent(this, ForegroundTimerService1.class);
//        leftIntent.putExtra("id", R.id.left);
        leftIntent.setAction(PLAY_ACTION);
//
        Intent rightIntent = new Intent(this, ForegroundTimerService1.class);
//        rightIntent.putExtra("id", R.id.right);
        rightIntent.setAction(STOP_ACTION);
//        PendingIntent leftPI = PendingIntent.getBroadcast(this, 1, leftIntent, 0);
//        PendingIntent rightPI = PendingIntent.getBroadcast(this, 2, rightIntent, 0);
        PendingIntent leftPI = PendingIntent.getService(this,1,leftIntent,0);
        PendingIntent rightPI = PendingIntent.getService(this, 2, rightIntent, 0);
//
        remoteView.setOnClickPendingIntent(R.id.left, leftPI);
        remoteView.setOnClickPendingIntent(R.id.right, rightPI);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("service", "onStartCommand");
        if (intent != null) {
            if(intent.getAction() != null){
                if(intent.getAction().equals(PLAY_ACTION)){
                    remoteView.setImageViewResource(R.id.right_icon,R.drawable.black_pause);
                }else if(intent.getAction().equals(STOP_ACTION)){
                    remoteView.setImageViewResource(R.id.right_icon,R.drawable.black_stop);
                }
                notificationManager.notify(1, notification);//改變完要刷新，不然要等到下一秒才會刷新 這樣體驗不好，感覺很LAG
            }else{
                hour = intent.getIntExtra("hour", 0);
                minute = intent.getIntExtra("minute", 0);
                second = intent.getIntExtra("second", 0);
                milliSecond = intent.getIntExtra("millisecond", 0) * 10;
                now = intent.getIntExtra("now", 0);
                milliSecond = milliSecond + (Calendar.getInstance().get(Calendar.MILLISECOND)-now);

                scheduledFuture = exec.scheduleAtFixedRate(timerRunnable, (1000 - milliSecond), 1 * 1000, TimeUnit.MILLISECONDS);
                milliSecond = 0;
                startForeground(1, notification);
            }

        }
        //milliSecond = Calendar.getInstance().get(Calendar.MILLISECOND);


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
            milliSecond += 1000;
            calToSecMinAndHour();
//            builder.setContentTitle(String.format("%02d:%02d:%02d",hour,minute,second));
            remoteView.setTextViewText(R.id.txtTile, String.format("%02d:%02d:%02d", hour, minute, second));

            builder.setContentInfo(String.format("%02d:%02d:%02d", hour, minute, second));
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
//        scheduledFuture.cancel(true);
//        exec.schedule(new MyRunnable(),100,TimeUnit.MILLISECONDS);
//        exec.shutdown();
//        exec.shutdownNow();
        stopForeground(true);
        //stopSelf();

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
        scheduledFuture.cancel(true);
        exec.schedule(new MyRunnable(),100,TimeUnit.MILLISECONDS);
        exec.shutdown();
        exec.shutdownNow();
        stopForeground(true);
        stopSelf();
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
            if (id == R.id.left) {
                Log.d("Here", "I am left");

                //isPlayInService = false;
//                handler.removeCallbacks(timerRunnable);
            } else {
                Log.d("Here", "I am right");
                //isPlayInService = true;
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
    public  Bitmap drawableToBitmap (Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}
