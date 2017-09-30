package com.liu.mytimer.view;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.liu.mytimer.R;
import com.liu.mytimer.module.WorkRecord;
import com.liu.mytimer.utils.Util;

import java.util.List;

/**
 * Created by kunming.liu on 2017/9/27.
 */

public class TimeTableView extends View implements View.OnTouchListener{
    //外框圓
    private Paint mCirclePaint = null;
    //用來畫圓餅圖的畫筆
    private Paint mFillPaint = null;
    //用來寫字的畫筆
    private Paint mTextPaint = null;
    //用來畫圓餅圖顏色說明區塊的畫筆
    private Paint mLegendPaint = null;
    //用來記錄child的totalWorkTime的總和
    private long mChildTotalWorkTime = 0;
    private List<WorkRecord> childList = null;
    private WorkRecord groupWorkRecord = null;
    private int width;
    private int height;
    //外圓半徑
    private int mOutsideRadius;
    //內圓半徑
    private int mInnerRadius;
    //中間圓半徑
    private int mCenterRadius;
    //內圓矩形，用來畫內圓弧
    private RectF mInnerRect;
    //中間圓矩形，用來畫中間圓弧
    private RectF mCenterRect;
    //外圓矩形，用來畫外圓弧
    private RectF mOutsideRect;
    //點了圓餅圖後，展開後的中間圓矩形，用來畫展開後的中間圓弧
    private RectF mTouchedCenterRect;
    //點了圓餅圖後，展開後的中間圓矩形，用來畫展開後的中間圓弧
    private RectF mTouchedOutsideRect;
    private Path arcPath;
    private Rect mTextBound = null;
    //顯示圓餅圖上的百分比數字
    private String mValueString = null;
    //touch的x,y
    private float touchX = 0;
    private float touchY = 0;
    //根據touch的x,y算出來的角度
    private float touchAngle = 0;
    //色碼
    private int[] mColors = {0xFF6495ED, 0xFFE32636, 0xFF800000, 0xFF808000, 0xFFFF8C69, 0xFF808080,
            0xFFE6B800, 0xFF7CFC00};
    //touch的是哪一塊圓餅圖
    private int touchedIndex = -1;
    //一開始畫圓餅圖的動畫
    private ValueAnimator animator;
    private float animatedValue;
    private long animatorDuration = 3000;
    private TimeInterpolator timeInterpolator = new AccelerateDecelerateInterpolator();
    //是否需要顯示動畫
    private boolean animatedFlag = true;
    //data size
    private int size = 0;
    //開始的角度
    private float startAngle = -90;
    //掃過的角度
    private float sweepAngle = 0;
    //每塊圓餅圖的間距
    private float offset = 0;

    private float[] startAngles = null;
    private float[] sweepAngles = null;


    private int legendTop;
    private int legendBottom;
    private int legendLeft;
    private int legendRight;

    public TimeTableView(Context context) {
        this(context, null);
    }

    public TimeTableView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public TimeTableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(attrs);

    }
    private void init() {
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setColor(Color.WHITE);
        mCirclePaint.setStrokeWidth(5);

        mFillPaint = new Paint();
        mFillPaint.setAntiAlias(true);
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setColor(Color.BLACK);
        mFillPaint.setStrokeWidth(5);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStrokeWidth(5);
        mTextPaint.setTextSize(Util.sp2px(getContext(),16));

        mLegendPaint = new Paint();
        mLegendPaint.setAntiAlias(true);
        mLegendPaint.setStyle(Paint.Style.STROKE);
        mLegendPaint.setColor(Color.BLACK);
        mLegendPaint.setStrokeWidth(5);

        mTextBound = new Rect();
        mInnerRect = new RectF();
        mCenterRect = new RectF();
        mOutsideRect = new RectF();
        mTouchedCenterRect = new RectF();
        mTouchedOutsideRect = new RectF();

        size = childList.size();
        startAngles = new float[size];
        sweepAngles = new float[size];

        startAngle = -90;
        sweepAngle = 0;
        offset = calOffsetForWorkTime(size);

        mChildTotalWorkTime = groupWorkRecord.getTotalWorkTime();
        for (int i =0; i < size; i++) {
            startAngles[i] = startAngle;
            sweepAngles[i] = calAngleForWorkTime(childList.get(i).getTotalWorkTime());
            startAngle = startAngles[i] + sweepAngles[i] + offset;
        }

        //setOnTouchListener(this);


    }
    private void initAttr(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TimeTableView);

        animatedFlag =  a.getBoolean(R.styleable.TimeTableView_showAnim,false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calRadiusAndSetRect();

        if(animatedFlag){
            initAnimator(animatorDuration);
        }else{
            animatedValue = 360.0f;
        }
    }
    private void initAnimator(long duration){
        if(animator != null){
            if(animator.isRunning()){
                animator.cancel();
                animator.start();
            }
        }else{
            animator = ValueAnimator.ofFloat(-90,270);
            animator.setDuration(duration);
            animator.setInterpolator(timeInterpolator);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    animatedValue = (float)animation.getAnimatedValue();
                    postInvalidate();
                }
            });
            animator.start();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(width / 2, height / 2);
        drawWorkTime(canvas);
        //canvas.drawPoint(0,0,mFillPaint);
    }

    private void drawWorkTime(Canvas canvas) {
        arcPath = new Path();
        for (int i = 0; i < size; i++) {
            mFillPaint.setColor(getColor(i));

            if(animatedValue >= (startAngles[i]+sweepAngles[i]) ){
                drawInnerArc(canvas, i, startAngles[i], sweepAngles[i] );
                drawOutSideArc(canvas, i, startAngles[i], sweepAngles[i] );
                drawTextOnArc(canvas, childList.get(i).getTotalWorkTime(), startAngles[i], (startAngles[i]+sweepAngles[i]) );

            }else{
                if(animatedValue >= startAngles[i] ){
                    drawInnerArc(canvas, i, startAngles[i], (animatedValue-startAngles[i]) );
                    drawOutSideArc(canvas, i, startAngles[i], (animatedValue-startAngles[i]) );
                }
            }
        }
//        if(animatedValue >= modules.get(size-1).getEndAngle()){
//            drawLegend(canvas);
//        }
    }
    private void drawInnerArc(Canvas canvas, int index, float startAngle, float sweepAngle) {
        arcPath.reset();
        if (index == touchedIndex) {
            arcPath.addArc(mCenterRect, startAngle, sweepAngle);
            arcPath.arcTo(mTouchedCenterRect, (startAngle + sweepAngle), -sweepAngle, false);
            arcPath.close();
            //module.setTouch(false);
        } else {
            arcPath.addArc(mInnerRect, startAngle, sweepAngle);
            arcPath.arcTo(mCenterRect, (startAngle + sweepAngle), -sweepAngle, false);
            arcPath.close();
        }
        mFillPaint.setAlpha(150);
        canvas.drawPath(arcPath, mFillPaint);
    }

    private void drawOutSideArc(Canvas canvas, int index, float startAngle, float sweepAngle) {
        arcPath.reset();
        if (index == touchedIndex) {
            arcPath.addArc(mTouchedCenterRect, startAngle, sweepAngle);
            arcPath.arcTo(mTouchedOutsideRect, (startAngle + sweepAngle), -sweepAngle, false);
            arcPath.close();
            touchedIndex = -1;
        } else {
            arcPath.addArc(mCenterRect, startAngle, sweepAngle);
            arcPath.arcTo(mOutsideRect, (startAngle + sweepAngle), -sweepAngle, false);
            arcPath.close();
        }
        mFillPaint.setAlpha(255);
        canvas.drawPath(arcPath, mFillPaint);
    }

    private void drawTextOnArc(Canvas canvas, long totalWorkTime, float startAngle, float endAngle) {
        mTextPaint.setColor(Color.WHITE);
        int value = Math.round(calPercentForWorkTime(totalWorkTime) * 100.0f);
        if (value == 0) {

        } else {
            mValueString = String.valueOf(value) + "%";
            mTextPaint.getTextBounds(mValueString, 0, mValueString.length(), mTextBound);
            int x = getCosPos((startAngle + endAngle) / 2, (mOutsideRadius + mCenterRadius) / 2);
            int y = getSinPos((startAngle + endAngle) / 2, (mOutsideRadius + mCenterRadius) / 2);
            canvas.drawText(mValueString, x - mTextBound.width() / 2, y + mTextBound.height() / 2, mTextPaint);
        }
    }
    private int getColor(int index){
        if (index >= mColors.length) {
            return mColors[index - mColors.length];
        }
        return mColors[index];
    }
    private float calPercentForWorkTime(long totalWorkTime){
        float percent = ((float)totalWorkTime / (float)mChildTotalWorkTime) ;
        return percent;
    }
    private float calAngleForWorkTime(long totalWorkTime) {
        float percent = ((float)totalWorkTime / (float)mChildTotalWorkTime) ;
        float angle = 360.0f * percent;
        return angle;
    }
    public int getSinPos(float angle, float radius) {
        double sin = Math.sin(Math.toRadians(angle));
        return (int) (radius * sin);
    }

    public int getCosPos(float angle, float radius) {
        double cos = Math.cos(Math.toRadians(angle));
        return (int) (radius * cos);
    }
    // TODO: 2017/9/27 調整算法
    private int calOffsetForWorkTime(int size) {
        int offset = 1 ;
        //offset = offset / size;
        return offset;
    }

    public void setGroupWorkRecord(WorkRecord groupWorkRecord) {
        this.groupWorkRecord = groupWorkRecord;
        this.childList = groupWorkRecord.getChildList();
        init();
        //若getWidth不等於0的話，代表已經onMeasure跟onLayout過了，因此不會在觸發到onSizeChange，因此就手動去算半徑
        if(getWidth() != 0 ){
            calRadiusAndSetRect();
        }
        //requestLayout();//當地二次回來的時候，強迫再做一次onMeasure跟onLayout (但是不會觸發到onSizeChange)
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    private void calRadiusAndSetRect(){
        width = getWidth();
        height = getHeight();
        mInnerRadius = (int) ((float) width * 0.1f);
        mCenterRadius = (int) ((float) width * 0.15f);
        mOutsideRadius = (int) ((float) width * 0.35f);

        mInnerRect.left = -mInnerRadius;
        mInnerRect.top = -mInnerRadius;
        mInnerRect.right = mInnerRadius;
        mInnerRect.bottom = mInnerRadius;

        mCenterRect.left = -mCenterRadius;
        mCenterRect.top = -mCenterRadius;
        mCenterRect.right = mCenterRadius;
        mCenterRect.bottom = mCenterRadius;

        mOutsideRect.left = -mOutsideRadius;
        mOutsideRect.top = -mOutsideRadius;
        mOutsideRect.right = mOutsideRadius;
        mOutsideRect.bottom = mOutsideRadius;

        mTouchedCenterRect.left = -mCenterRadius - (mCenterRadius - mInnerRadius);
        mTouchedCenterRect.top = -mCenterRadius - (mCenterRadius - mInnerRadius);
        mTouchedCenterRect.right = mCenterRadius + (mCenterRadius - mInnerRadius);
        mTouchedCenterRect.bottom = mCenterRadius + (mCenterRadius - mInnerRadius);

        mTouchedOutsideRect.left = -mOutsideRadius - (mCenterRadius - mInnerRadius);
        mTouchedOutsideRect.top = -mOutsideRadius - (mCenterRadius - mInnerRadius);
        mTouchedOutsideRect.right = mOutsideRadius + (mCenterRadius - mInnerRadius);
        mTouchedOutsideRect.bottom = mOutsideRadius + (mCenterRadius - mInnerRadius);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchX = event.getX() - (width / 2);
                touchY = event.getY() - (height / 4);
                if(touchX > mOutsideRadius || touchX < -mOutsideRadius
                        || touchY > mOutsideRadius || touchY < -mOutsideRadius){
                    touchedIndex = -1;
                }else{
                    touchAngle = 0;
                    if (touchX > 0 && touchY < 0) {
//                        touchAngle += 90;
                    } else if (touchX > 0 && touchY > 0) {
//                        touchAngle += 90;
                    } else if (touchX < 0 && touchY > 0) {
                        touchAngle += 180;
                    } else {
                        touchAngle += 180;
                    }
                    touchAngle += Math.toDegrees(Math.atan(touchY / touchX));
                    //Log.e("touchAngle","touchAngle  ="+touchAngle);
                    int index = searchAngleInList(touchAngle);
                    if (index >= 0) {
                        touchedIndex = index;
                    }else{
                        touchedIndex = -1;
                    }
                }
                postInvalidate();
                break;
        }
        return false;
    }

    private int searchAngleInList(float angle) {
        for (int i = 0; i < startAngles.length; i++) {
            if (startAngles[i] <= angle && (startAngles[i]+sweepAngles[i]) >= angle) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Util.log("onSaveInstanceState");
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        Util.log("onRestoreInstanceState");
    }
}
