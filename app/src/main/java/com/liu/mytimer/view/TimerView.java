package com.liu.mytimer.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;


/**
 * Created by kunmingliu on 2017/9/18.
 */

public class TimerView extends View {
    public static final int CHANGE_TIME = 0 ;
    private Paint mPaint = null;
    private Paint mTextPaint = null;
    private Paint mBallPaint = null;
    private Path mCirclePath = null;
    private PathMeasure mPathMeasure = null;
    private int radius;
    private int innerRadius;
    private int ballRadius;
    private int width;
    private int height;
    private String timerText = null;
    private Rect mRect = null;
    private float[] pos;
    private Handler handler = null;
    private Message message = null;
    private int second;
    private int minute;
    private int hour ;
    private TimerRunnable timerRunnable = null;
    private ValueAnimator animator= null;
    public TimerView(Context context) {
        super(context);
        init();
    }

    public TimerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }
    private void init(){
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(8);
        mPaint.setAntiAlias(true);

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setStrokeWidth(3);
        //mPaint.setTextSize(30);//不好的寫法，pixel每台手機都不同
        mTextPaint.setTextSize(sp2px(22));
        mTextPaint.setAntiAlias(true);

        mBallPaint = new Paint();
        mBallPaint.setStyle(Paint.Style.FILL);
        mBallPaint.setColor(Color.RED);
        mBallPaint.setAntiAlias(true);

        mCirclePath = new Path();
        mPathMeasure = new PathMeasure();
        timerText = "00:00:00";

        mRect = new Rect();

        mPaint.getTextBounds(timerText,0,timerText.length(),mRect);
        pos = new float[2];

        second = 0;
        minute = 0;
        hour  = 0;

        timerRunnable = new TimerRunnable();

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case CHANGE_TIME :
                        postInvalidate();
                        break;
                }
            }
        };
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width  = getWidth();
        height = getHeight();
        radius = width/3;
        innerRadius = radius - 20;
        ballRadius = radius/10;
        mCirclePath.addCircle(0,0,innerRadius, Path.Direction.CW);
        mPathMeasure.setPath(mCirclePath,false);
        mPathMeasure.getPosTan(0,pos,null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(width/2,height/2);

        //外圍的圓
        canvas.drawCircle(0,0,getWidth()/3,mPaint);
        //碼表的數字
        mTextPaint.getTextBounds(timerText,0,timerText.length(),mRect);
        canvas.drawText(timerText, -mRect.width()/2,mRect.height()/2,mTextPaint);
        //小球
        canvas.rotate(-90);
//        canvas.drawCircle(pos[0],pos[1],10,mPaint);//不好的寫法，10pixel每台手機位置都不同
        canvas.drawCircle(pos[0],pos[1],ballRadius,mBallPaint);
    }

    /**
     * 小球的動畫
     */
    private void startAnimation(){
        float length = mPathMeasure.getLength();
        animator = ValueAnimator.ofFloat(0,length);
        animator.setDuration(1*1000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPathMeasure.getPosTan((float)animation.getAnimatedValue(),pos,null);
                postInvalidate();
            }
        });
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }
    //用來計時的Runnable
    private class TimerRunnable implements Runnable {

        @Override
        public void run() {
            timerText = String.format("%02d:%02d:%02d",hour,minute,second);
            second++;
            if (second  >= 60) {
                second = 0;
                minute++;
            }
            if (minute  == 60) {
                minute = 0;
                hour++;
            }
            message = handler.obtainMessage(CHANGE_TIME);
            handler.sendMessage(message);
            handler.postDelayed(this, 1 * 1000);
        }

    }

    /**
     * 暫停計時
     */
    public void pauseTimer(){
        animator.cancel();
        handler.removeCallbacks(timerRunnable);
        //postInvalidate();
    }

    /**
     * 停止計時(歸零)
     */
    public void stopTimer(){
        animator.cancel();
        handler.removeCallbacks(timerRunnable);
        second = 0;
        minute = 0;
        hour  = 0;
        timerText = "00:00:00";
        mPathMeasure.getPosTan(0,pos,null);
        postInvalidate();
    }
    public void startTimer(){
        handler.post(timerRunnable);
        startAnimation();
    }
    public void startTimer(int hour ,int minute, int second){
        this.second = second;
        this.minute = minute;
        this.hour  = hour;
        timerText = String.format("%02d:%02d:%02d",hour,minute,second);
        handler.post(timerRunnable);
        startAnimation();
    }
    public void startTimer(int hour ,int minute, int second, boolean isPlayInService){
        this.second = second;
        this.minute = minute;
        this.hour  = hour;
        timerText = String.format("%02d:%02d:%02d",hour,minute,second);
        if(isPlayInService){
            handler.post(timerRunnable);
            startAnimation();
        }

    }
    private int sp2px(int sp){
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }
    public int getHour(){
        return hour;
    }
    public int getMinute(){
        return minute;
    }
    public int getSecond(){
        return second;
    }

}
