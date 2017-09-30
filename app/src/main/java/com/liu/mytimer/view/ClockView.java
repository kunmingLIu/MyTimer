package com.liu.mytimer.view;

import android.graphics.PathMeasure;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by kunming.liu on 2017/9/25.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;


import java.util.Calendar;

/**
 * Created by kunming.liu on 2017/9/15.
 */

public class ClockView extends View{
    private Paint mClockPaint = null;
    private Paint mTimeHandPaint = null;
    private Paint mChoosePaint = null;
    private Rect mTextBound = null;
    private Path mCirclePath = null;
    private Path mTextPath = null;
    private PathMeasure mTextPathMeasure = null;
    private int mRadius = 0;
    private int mTextPathRadius = 0;
    private int mChooseRadius = 0;
    private int width;
    private int height;
    private String mTimeHandString;
    private float[] mTextPos = null;
    private int mHour;
    private int mMinute = 20;
    private int mSecond;
    public ClockView(Context context) {
        this(context,null);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        mClockPaint = new Paint();
        mClockPaint.setAntiAlias(true);
        mClockPaint.setColor(Color.WHITE);
        mClockPaint.setStyle(Paint.Style.STROKE);
        mClockPaint.setStrokeWidth(1);
        setLayerType(LAYER_TYPE_SOFTWARE, mClockPaint);

        mTimeHandPaint = new Paint();
        mTimeHandPaint.setAntiAlias(true);
        mTimeHandPaint.setColor(Color.WHITE);
        mTimeHandPaint.setStyle(Paint.Style.FILL);
        mTimeHandPaint.setStrokeWidth(1);
        mTimeHandPaint.setTextSize(sp2px(14));

        mChoosePaint = new Paint();
        mChoosePaint.setAntiAlias(true);
        mChoosePaint.setColor(Color.argb(255,0,221,221));
        mChoosePaint.setStyle(Paint.Style.FILL);
        mChoosePaint.setStrokeWidth(5);
        mChoosePaint.setAlpha(155);

        mTextBound = new Rect();

        mCirclePath = new Path();
        mTextPath = new Path();
        mTextPathMeasure = new PathMeasure();

        mTextPos = new float[2];


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = getWidth();
        height = getHeight();
        mRadius = (int)(width * 0.4f);
        mTextPathRadius = (int)(width * 0.43f);
        mChooseRadius = (int)(width * 0.05f);

        mCirclePath.addCircle(0,0,mRadius, Path.Direction.CW);
        mTextPath.addCircle(0,0,mTextPathRadius, Path.Direction.CW);
        mTextPathMeasure.setPath(mTextPath,false);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(width/2,height/2);

        drawTimeHands(canvas);
        drawChooseTime(canvas);

    }
    private void drawTimeHands(Canvas canvas){
        for(int i = 3 ; i < 15; i++){
            if(i >= 12){
                mTimeHandString = String.format("%02d",i-12);
            }else{
                mTimeHandString = String.format("%02d",i);
            }
            mTimeHandPaint.getTextBounds(mTimeHandString,0,mTimeHandString.length(),mTextBound);
            mTextPathMeasure.getPosTan((float)(Math.toRadians((i-3)*30)*mTextPathRadius),mTextPos,null);
            canvas.drawText(mTimeHandString, mTextPos[0] - mTextBound.width()/2 , mTextPos[1] + mTextBound.height()/2,mTimeHandPaint);
        }
    }
    private void drawChooseTime(Canvas canvas){
        float angle = covertMinuteToAngle(mMinute);
        mTimeHandString = String.format("%02d",mMinute);
        mTimeHandPaint.getTextBounds(mTimeHandString,0,mTimeHandString.length(),mTextBound);
        mTextPathMeasure.getPosTan((float)(Math.toRadians(angle)*mTextPathRadius),mTextPos,null);
        canvas.drawCircle(mTextPos[0],mTextPos[1],mChooseRadius,mChoosePaint);
        canvas.drawLine(0,0,mTextPos[0],mTextPos[1],mChoosePaint);
        canvas.drawPoint(0,0,mChoosePaint);
    }

    private int sp2px(int sp){
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }
    public float covertMinuteToAngle(int minute) {
        float angle = (minute - 15) * 6;
        if (angle < 0) {
            angle = angle + 360;
        }
        return angle;
    }
    public float covertHourToAngle(int hour, int minute) {
        float angle = (hour - 3) * 30;
        if (angle < 0) {
            angle = angle + 360;
            angle = angle + (minute / 12) * 6;
        } else {
            angle = angle + (minute / 12) * 6;
        }
        return angle;
    }
}
