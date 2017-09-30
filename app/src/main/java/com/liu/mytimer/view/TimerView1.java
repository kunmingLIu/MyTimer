package com.liu.mytimer.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.liu.mytimer.utils.Util;


/**
 * Created by kunmingliu on 2017/9/18.
 */

public class TimerView1 extends View {
//    public static final int CHANGE_TIME = 0 ;
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
//    private Handler handler = null;
//    private Message message = null;
    private int milliSecond;
    private int second;
    private int minute;
    private int hour ;
//    private TimerRunnable timerRunnable = null;
    private ValueAnimator animator= null;
    private ValueAnimator pauseAnimator = null;
    public TimerView1(Context context) {
        super(context);
        init();
    }

    public TimerView1(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimerView1(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        timerText = "99:99:99:99";

        mRect = new Rect();

        mTextPaint.getTextBounds(timerText,0,timerText.length(),mRect);
        timerText = "00:00:00:00";
        pos = new float[2];

        second = 0;
        minute = 0;
        hour  = 0;
        milliSecond = 0;

//        setWillNotDraw(true);

//        timerRunnable = new TimerRunnable();

//        handler = new Handler(Looper.getMainLooper()) {
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                switch (msg.what){
//                    case CHANGE_TIME :
//                        postInvalidate();
//                        break;
//                }
//            }
//        };
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
        //如果一直去算的話，會造成說畫面有點震動的感覺，因為某個字可能都會有點差異，因此可能會突然往左一點然後下次又往右一點，這樣看起來就有點在震動
        //mTextPaint.getTextBounds(timerText,0,timerText.length(),mRect);
        canvas.drawText(timerText, -mRect.width()/2,mRect.height()/2,mTextPaint);
        //小球
        canvas.rotate(-90);
//        canvas.drawCircle(pos[0],pos[1],10,mPaint);//不好的寫法，10pixel每台手機位置都不同
        canvas.drawCircle(pos[0],pos[1],ballRadius,mBallPaint);
    }

    /**
     * 小球的動畫
     */
//    private void startAnimation(){
//        float length = mPathMeasure.getLength();
//        animator = ValueAnimator.ofFloat(0,length);
//        animator.setDuration(1*1000);
//        animator.setRepeatCount(ValueAnimator.INFINITE);
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                mPathMeasure.getPosTan((float)animation.getAnimatedValue(),pos,null);
//                postInvalidate();
//            }
//        });
//        animator.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                Util.log("onAnimationStart");
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                Util.log("onAnimationEnd");
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//                Util.log("onAnimationCancel");
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//                Util.log("onAnimationRepeat");
//            }
//        });
//        animator.setInterpolator(new AccelerateDecelerateInterpolator());
//        animator.start();
//    }
    private void startAnimation(){
        final float length = mPathMeasure.getLength();
        animator = ValueAnimator.ofInt(0,100);
        animator.setDuration(1*1000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPathMeasure.getPosTan(length*(animation.getAnimatedFraction()),pos,null);
                milliSecond = (int)animation.getAnimatedValue();
                timerText = String.format("%02d:%02d:%02d:%02d",hour,minute,second,milliSecond);
                postInvalidate();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Util.log("onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Util.log("onAnimationEnd");
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Util.log("onAnimationCancel");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                second++;
                if(second == 60){
                    minute ++;
                    second = 0;
                }
                if(minute == 60){
                    hour ++;
                    minute = 0;
                }
                timerText = String.format("%02d:%02d:%02d:%02d",hour,minute,second,milliSecond);
                //postInvalidate();//因為已經每100毫秒都去刷新畫面了，所以這邊不用刷新，不然會造成某個瞬間連續刷新兩次
            }
        });
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }
    private void reStartAnimation(int pauseMillSecond){
        //pauseMillSecond/100 會等於0 ，要先轉成float再算
        //先算出說此刻的pauseMillSecond，佔了整個元的多少長度
        final float length = mPathMeasure.getLength()*((float)pauseMillSecond/100.0f);
        //再算出說還有多少距離可以繞完一整圈
        final float offset = mPathMeasure.getLength() - length;
        //變化區間就是pauseMillSecond~100
        ValueAnimator restartAnimator = ValueAnimator.ofInt(pauseMillSecond,100);
        //原本繞完一圈是要1秒，但是要用此刻pauseMillSecond還有多久才會到1秒來當作時間
        restartAnimator.setDuration((100-pauseMillSecond)*10);

        restartAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //每次都前進offset的某個比例，當全部跑完時，那就是length+offset，那就剛好繞回到原點了
                mPathMeasure.getPosTan(length+(offset*(animation.getAnimatedFraction())),pos,null);
                milliSecond = (int)animation.getAnimatedValue();
                timerText = String.format("%02d:%02d:%02d:%02d",hour,minute,second,milliSecond);
                postInvalidate();
            }
        });
        restartAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Util.log("onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                second++;
                if(second == 60){
                    minute ++;
                    second = 0;
                }
                if(minute == 60){
                    hour ++;
                    minute = 0;
                }
                timerText = String.format("%02d:%02d:%02d:%02d",hour,minute,second,milliSecond);
//                postInvalidate();
//                animation.cancel();
                //啟動小球的動畫
                startAnimation();

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        restartAnimator.setInterpolator(new LinearInterpolator());
        restartAnimator.start();
    }
    private void pauseAnimation(){
        pauseAnimator = ValueAnimator.ofInt(1,0);
        pauseAnimator.setDuration(1*800);
        pauseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pauseAnimator.setInterpolator(new LinearInterpolator());
        pauseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mTextPaint.setAlpha(animation.getAnimatedFraction() >= 0.5 ? 255 : 0);
                postInvalidate();
            }
        });
        pauseAnimator.start();
    }
    //用來計時的Runnable
    //用這種寫法，變得說我小球動畫要刷新，但是這邊文字的部分也要再刷新一次，太浪費資源
//    private class TimerRunnable implements Runnable {
//
//        @Override
//        public void run() {
//            timerText = String.format("%02d:%02d:%02d:%02d",hour,minute,second,millSecond);
//            millSecond += 1;
//            if(millSecond == 10){
//                second ++;
//                millSecond = 0;
//            }
//            if(second == 60 ){
//                minute++;
//                second = 0;
//            }
//            if(minute == 60){
//                minute = 0;
//                hour++;
//            }
//
//            message = handler.obtainMessage(CHANGE_TIME);
//            handler.sendMessage(message);
//            handler.postDelayed(this, 1 * 100);
//            //handler.postAtFrontOfQueue(message);
//        }
//
//    }

    /**
     * 暫停計時
     */
    public void pauseTimer(){
        cancelAnimation();
        pauseAnimation();
        //handler.removeCallbacks(timerRunnable);
        //postInvalidate();
    }

    /**
     * 停止計時(歸零)
     */
    public void stopTimer(){
        cancelAnimation();
        //handler.removeCallbacks(timerRunnable);
        second = 0;
        minute = 0;
        hour  = 0;
        milliSecond = 0;
        timerText = "00:00:00:00";
        mPathMeasure.getPosTan(0,pos,null);
//        stopAnimation();
        postInvalidate();
    }
    public void startTimer(){
        //handler.post(timerRunnable);
        startAnimation();
    }
    public void reStartTimer(){
        cancelAnimation();
        reStartAnimation(milliSecond);
    }
    public void reStartTimer(int hour ,int minute, int second,int millSecond, boolean isPlayInService){
        this.second = second;
        this.minute = minute;
        this.hour  = hour;
        this.milliSecond = millSecond;
        timerText = String.format("%02d:%02d:%02d:%02d",hour,minute,second,millSecond);
        if(isPlayInService){
            if(millSecond == 0){
                cancelAnimation();
                startAnimation();
            }
            else{
                cancelAnimation();
                reStartAnimation(millSecond);
            }

        }
    }
    public void startTimer(int hour ,int minute, int second){
        this.second = second;
        this.minute = minute;
        this.hour  = hour;
        timerText = String.format("%02d:%02d:%02d:%02d",hour,minute,second,milliSecond);
        //handler.post(timerRunnable);
        cancelAnimation();
        startAnimation();
    }
    public void startTimer(int hour ,int minute, int second,int milliSecond){
        this.second = second;
        this.minute = minute;
        this.hour  = hour;
        this.milliSecond = milliSecond;
        timerText = String.format("%02d:%02d:%02d:%02d",hour,minute,second,milliSecond);
        //handler.post(timerRunnable);
        cancelAnimation();
        startAnimation();
    }
    public void startTimer(int hour ,int minute, int second, boolean isPlayInService){
        this.second = second;
        this.minute = minute;
        this.hour  = hour;
        timerText = String.format("%02d:%02d:%02d:%02d",hour,minute,second,milliSecond);
        if(isPlayInService){
            //handler.post(timerRunnable);
            cancelAnimation();
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
    public int getMilliSecond(){return milliSecond;}
    public void cancelAnimation(){
        if(pauseAnimator != null){
            pauseAnimator.cancel();
            mTextPaint.setAlpha(255);
        }
        if( animator != null){
            animator.cancel();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }
}
