package com.liu.mytimer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by kunming.liu on 2017/9/25.
 */

public class StartAndEndClockView extends View{
    private Paint mClockPaint = null;
    private PathMeasure mClockPathMeasure = null;
    private Path mClockPath = null;
    private Path mInnerPath = null;
    private PathMeasure mInnerPathMeasure = null;
    private Paint mStartPaint = null;
    private Paint mEndPaint = null;
    private Paint mTotalTimePaint = null;

    private Paint mTimeHandPaint = null;
    private Path mTextPath = null;
    private float[] mTextPos = null;
    private String mTimeHandString;
    private Rect mTextBound = null;
    private PathMeasure mTextPathMeasure = null;

    private Paint mLegendPaint = null;
    private Paint mLegendTextPaint = null;

    private int mInnerRadius = 0;
    private int mRadius = 0;
    private int mTextPathRadius = 0;

    private int width;
    private int height;
    private int mStartHour = 8;
    private int mStartMin = 20;
    private int mStartSec = 0;
    private int mEndHour = 11;
    private int mEndMin = 0;
    private int mEndSec = 0;
    private float[] mStartTimePos = null;
    private float[] mEndTimePos = null;
    private float startAngle = 0;
    private float endAngle = 0;
    private String mLegendString;

    public StartAndEndClockView(Context context) {
        this(context,null);
    }

    public StartAndEndClockView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public StartAndEndClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

        mStartPaint = new Paint();
        mStartPaint.setAntiAlias(true);
        mStartPaint.setColor(Color.MAGENTA);
        mStartPaint.setStyle(Paint.Style.FILL);
        mStartPaint.setStrokeWidth(5);
        mStartPaint.setStrokeCap(Paint.Cap.ROUND);

        mEndPaint = new Paint();
        mEndPaint.setAntiAlias(true);
        mEndPaint.setColor(Color.argb(255,0,221,221));
        mEndPaint.setStyle(Paint.Style.FILL);
        mEndPaint.setStrokeWidth(5);
        mEndPaint.setStrokeCap(Paint.Cap.ROUND);

        mTotalTimePaint = new Paint();
        mTotalTimePaint.setAntiAlias(true);
        mTotalTimePaint.setColor(Color.YELLOW);
        mTotalTimePaint.setStyle(Paint.Style.FILL);
        mTotalTimePaint.setStrokeWidth(5);
        mTotalTimePaint.setAlpha(155);

        mLegendPaint = new Paint();
        mLegendPaint.setAntiAlias(true);
        mLegendPaint.setColor(Color.WHITE);
        mLegendPaint.setStyle(Paint.Style.STROKE);
        mLegendPaint.setStrokeWidth(8);

        mLegendTextPaint = new Paint();
        mLegendTextPaint.setAntiAlias(true);
        mLegendTextPaint.setColor(Color.BLACK);
        mLegendTextPaint.setStyle(Paint.Style.FILL);
        mLegendTextPaint.setStrokeWidth(1);
        mLegendTextPaint.setTextSize(sp2px(14));


        mTextBound = new Rect();

        mClockPath = new Path();
        mClockPathMeasure = new PathMeasure();
        mInnerPath = new Path();
        mInnerPathMeasure = new PathMeasure();
        mTextPath = new Path();
        mTextPathMeasure = new PathMeasure();

        mTextPos = new float[2];
        mStartTimePos = new float[2];
        mEndTimePos = new float[2];

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = getWidth();
        height = getHeight();
        mInnerRadius = (int)(width * 0.20f);
        mRadius = (int)(width * 0.25f);
        mTextPathRadius = (int)(width * 0.30f);

        mInnerPath.addCircle(0,0,mInnerRadius, Path.Direction.CW);
        mClockPath.addCircle(0,0,mRadius, Path.Direction.CW);
        mTextPath.addCircle(0,0,mTextPathRadius, Path.Direction.CW);

        mInnerPathMeasure.setPath(mInnerPath,false);
        mClockPathMeasure.setPath(mClockPath,false);
        mTextPathMeasure.setPath(mTextPath,false);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0,0,width,height,mLegendPaint);
        canvas.translate(width/2,height/2-mTextPathRadius);

        drawTimeHands(canvas);
        drawStartTimeHand(canvas);
        drawEndTimeHand(canvas);
        drawTotalTime(canvas);
        drawLegend(canvas);


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
    private void drawStartTimeHand(Canvas canvas){
        startAngle = covertHourToAngle(mStartHour,mStartMin);
        mInnerPathMeasure.getPosTan((float)(Math.toRadians(startAngle)*mInnerRadius),mStartTimePos,null);
        canvas.drawLine(0,0,mStartTimePos[0],mStartTimePos[1],mStartPaint);
    }
    private void drawEndTimeHand(Canvas canvas){
        endAngle = covertHourToAngle(mEndHour,mEndMin);
        mInnerPathMeasure.getPosTan((float)(Math.toRadians(endAngle)*mInnerRadius),mEndTimePos,null);
        canvas.drawLine(0,0,mEndTimePos[0],mEndTimePos[1],mEndPaint);
    }
    private void drawTotalTime(Canvas canvas){
        Path arc = new Path();
        arc.moveTo(0,0);
        arc.lineTo(mStartTimePos[0],mStartTimePos[1]);
        arc.arcTo(new RectF(-mInnerRadius,-mInnerRadius,mInnerRadius,mInnerRadius),startAngle,(endAngle-startAngle),false);
        arc.close();
        canvas.drawPath(arc,mTotalTimePaint);
    }
    private void drawLegend(Canvas canvas){
        Rect rect  = new Rect(-mTextPathRadius,mTextPathRadius+20,-mTextPathRadius+20,mTextPathRadius+40);
        canvas.drawRect(rect, mLegendPaint);
        canvas.drawRect(rect, mStartPaint);

        mLegendString = "開始時間: 08:00:00";
        canvas.drawText(mLegendString , -mTextPathRadius+30,mTextPathRadius+40, mLegendTextPaint);

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
