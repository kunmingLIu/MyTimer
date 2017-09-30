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

import com.liu.mytimer.App;
import com.liu.mytimer.MainActivity;
import com.liu.mytimer.R;

/**
 * Created by kunming.liu on 2017/9/20.
 */
// TODO: 2017/9/23 如果切換到servcie的話 然後一直觀察時間的話，會大概慢個一秒鐘左右，然後會隨著時間，越慢越多
public class ForegroundTimerService extends Service {
    NotificationManager notificationManager = null;
//    Notification.Builder builder = null;
    NotificationCompat.Builder builder = null;
    Intent intent = null;
    PendingIntent pi = null;
    Notification notification = null;
    private static Handler handler = null;
    private  int hour;
    private  int minute;
    private  int second;
    private int hourFromActivity;
    private int minuteFromActivity;
    private int secondFromActivity;
    private static long millSecond;
    private TimerRunnable timerRunnable = null;
    private TimerRunnable1 timerRunnable1 = null;

    private RemoteViews remoteView = null;
    private MyBinder myBinder = null ;
    private static boolean isPlayInService = false;
    private int temp ;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("service", "onCreate");
        //android.app.remoteserviceexception bad notification posted from package
        remoteView = new RemoteViews(getPackageName(), R.layout.notification_layout2);

        handler = new Handler();
        timerRunnable = new TimerRunnable();
        timerRunnable1 = new TimerRunnable1();
        myBinder = new MyBinder();
        hour = 0;
        minute = 0;
        second = 0;
        millSecond = 0;

        hourFromActivity = 0;
        minuteFromActivity = 0;
        secondFromActivity = 0;
        isPlayInService = false;

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
                .setContentIntent(pi);
        notification = builder.build();



        Intent stopIntent = new Intent(this, PlayAndStopReceiver.class);
        stopIntent.putExtra("id",R.id.stop);

        Intent playIntent = new Intent(this, PlayAndStopReceiver.class);
        playIntent.putExtra("id",R.id.play);

        PendingIntent stopPI = PendingIntent.getBroadcast(this,1,stopIntent,0);
        PendingIntent playPI = PendingIntent.getBroadcast(this,2,playIntent,0);

        remoteView.setOnClickPendingIntent(R.id.play,playPI);
        remoteView.setOnClickPendingIntent(R.id.stop,stopPI);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("service", "onStartCommand");
        if(intent != null){
            hour = intent.getIntExtra("hour",0);
            minute = intent.getIntExtra("minute",0);
            second = intent.getIntExtra("second",0);
            millSecond = intent.getIntExtra("millsecond",0)*10;
//            millSecond = millSecond+ secondFromActivity*1000;
//            millSecond = millSecond+ minuteFromActivity*1000*60;
//            millSecond = millSecond+ hourFromActivity*1000*60*60;

        }
        //如果丟過了的事450毫秒，那我就可以知道說550毫秒後，就是1秒了，到時候我就可以讓millSecond每次都由0開始算
        handler.postDelayed(timerRunnable1,(1000-millSecond));
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
            millSecond += 1000;
            setCurrentTime();
//            builder.setContentTitle(String.format("%02d:%02d:%02d",hour,minute,second));
            remoteView.setTextViewText(R.id.txtTile,String.format("%02d:%02d:%02d:%04d",hour,minute,second,millSecond));
            notification = builder.build();
            notificationManager.notify(1, notification);
            handler.postDelayed(this, 1 * 1000);

        }
    }
    private class TimerRunnable1 implements Runnable{

        @Override
        public void run() {
            millSecond = 0 ;
            second++;
            if(second == 60){
                second = 0 ;
                minute ++;
            }
            if(minute == 60){
                minute = 0 ;
                hour ++;
            }
            handler.postDelayed(timerRunnable, 1 * 1000);

        }
    }
    //intent中取到activity的值，但是這邊又做了reset的動作。應該要先把時分秒都先轉成毫秒數
    private void setCurrentTime(){
//        temp = (int) (millSecond / 1000);
//        second = temp % 60;
//        minute = (temp / 60) % 60;
//        hour = temp / 3600;


//        hour = (int) ((millSecond / 1000) / 3600);
//        minute = (int) (((millSecond / 1000) / 60) % 60);
//        second = (int) ((millSecond / 1000) % 60);
        if(millSecond == 1000){
            millSecond = 0;
            second++;
        }
        if(second == 60){
            second = 0 ;
            minute ++;
        }
        if(minute == 60){
            minute = 0 ;
            hour ++;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("service", "onDestroy");
        isPlayInService = false;
        handler.removeCallbacks(timerRunnable);//如果不加的話，service無法被回收
//        stopForeground(true);
        //stopSelf();

    }
    public class MyBinder extends Binder {
        public int getHour(){
            return hour;
        }
        public int getMinute(){
            return minute;
        }
        public int getSecond(){
            return second;
        }
        public int getMillSecond(){
            float x = (float)millSecond/1000.0f;
            x = x - millSecond/1000;//取得小數點後的數字
            return (int)(x *100);
        }
        public void stopService(){
            isPlayInService = false;
            handler.removeCallbacks(timerRunnable);
        }
        public void reStartService(){
            isPlayInService = true;
            handler.post(timerRunnable);
        }
        public boolean isPlayInService(){return isPlayInService;}
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
    public  static class PlayAndStopReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Here", "I am here");
            int id = intent.getIntExtra("id",-1);
            if(id == R.id.stop){
                isPlayInService = false;
//                handler.removeCallbacks(timerRunnable);
            }else{
                isPlayInService = true;
//                handler.post(timerRunnable);
            }
        }
    }
}
