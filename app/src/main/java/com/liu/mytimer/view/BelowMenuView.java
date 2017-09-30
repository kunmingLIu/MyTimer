package com.liu.mytimer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.liu.mytimer.R;
import com.liu.mytimer.utils.Util;

/**
 * Created by kunming.liu on 2017/9/28.
 */

public class BelowMenuView extends View {
    private Paint mRectPaint = null;
    private Paint mLinePaint = null;
    private int width;
    private int height;
    private int menuWidth;
    private int drawableWidth;
    private int drawableHeight;
    private int drawLeft;
    private int drawRight;

    private int space;
    private int offset;
    private int left;
    private int right;
    private int currentPosition = -1;
    private int currentLeft;
    private int currentRight;
    private Drawable timerDrawable = null;
    private Drawable settingDrawable = null;
    private Drawable dateDrawable = null;
    private Drawable[] drawables = null;

    public BelowMenuView(Context context) {
        this(context,null);
    }

    public BelowMenuView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public BelowMenuView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mRectPaint = new Paint();
        mRectPaint.setStrokeWidth(3);
        mRectPaint.setAntiAlias(true);
        mRectPaint.setStyle(Paint.Style.FILL);
        mRectPaint.setColor(Color.DKGRAY);

        mLinePaint = new Paint();
        mLinePaint.setStrokeWidth(8);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mLinePaint.setColor(Color.parseColor("#FF4081"));

        timerDrawable = ContextCompat.getDrawable(getContext(), R.drawable.timer);
        settingDrawable = ContextCompat.getDrawable(getContext(), R.drawable.setting);
        dateDrawable = ContextCompat.getDrawable(getContext(), R.drawable.date);

        drawables = new Drawable[3];
        drawables[0] = dateDrawable;
        drawables[1] = timerDrawable;
        drawables[2] = settingDrawable;

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = getWidth();
        height = getHeight();

        space = getWidth()/5;
        drawableWidth= getWidth()/10;
        drawableHeight= getWidth()/10;
        menuWidth = getWidth() / 8;

        drawLeft = width/2-drawableWidth/2;
        drawRight = drawLeft+drawableWidth;
        timerDrawable.setBounds(drawLeft,drawableHeight/4,drawRight,drawableHeight+drawableHeight/4);

        left = drawables[1].getBounds().left;
        right = drawables[1].getBounds().right;

        drawLeft = left-space-drawableWidth/2;
        drawRight = drawLeft+drawableWidth;
        dateDrawable.setBounds(drawLeft,drawableHeight/4,drawRight,drawableHeight+drawableHeight/4);

        drawLeft = left+space+drawableWidth/2;
        drawRight = drawLeft+drawableWidth;
        settingDrawable.setBounds(drawLeft,drawableHeight/4,drawRight,drawableHeight+drawableHeight/4);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(left, (int)(height*0.95), right, (int)(height*0.95), mLinePaint);

        timerDrawable.draw(canvas);
        dateDrawable.draw(canvas);
        settingDrawable.draw(canvas);
    }

    public void moveToRight(int position , float fraction) {
        if(currentPosition == -1){
            currentPosition = position;
            currentLeft = drawables[position].getBounds().left;
            currentRight = drawables[position].getBounds().right;
            offset = drawables[position+1].getBounds().left - currentLeft;
        }
        left = currentLeft+ Math.round((float) offset * fraction);
        right = currentRight+Math.round((float) offset * fraction);

        postInvalidate();
    }
    public void moveToLeft(int position , float fraction) {
        if(currentPosition == -1){
            currentPosition = position;
            currentLeft = drawables[position].getBounds().left;
            currentRight = drawables[position].getBounds().right;
            offset = currentLeft - drawables[position-1].getBounds().left ;
        }
        left = currentLeft -  Math.round((float) offset * (1-fraction));
        right = currentRight - Math.round((float) offset * (1-fraction));

        postInvalidate();
    }


    public void drawBelowLine(int position) {
        left = drawables[position].getBounds().left;
        right = drawables[position].getBounds().right;
        currentPosition = -1;
        postInvalidate();
    }

}
