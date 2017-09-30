package com.liu.mytimer.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.liu.mytimer.R;

/**
 * Created by kunmingliu on 2017/9/20.
 */

public class StarView extends View {
    private Paint mPaint = null;
    private int mRadius = 0;
    private Bitmap bitmap = null;
    private BitmapFactory.Options options = null;
    private Drawable drawable;
    public StarView(Context context) {
        this(context,null);
    }

    public StarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public StarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(getRandomColor());
        mPaint.setStyle(Paint.Style.FILL);

        //bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.star);
        //options = new BitmapFactory.Options();

        drawable = ContextCompat.getDrawable(getContext(),R.drawable.star);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(getWidth()/2,getHeight()/2);
        canvas.drawCircle(0,0,getWidth()/3,mPaint);

        //canvas.drawBitmap(bitmap,-getWidth()/2,-getHeight()/2,mPaint);
        //canvas.drawBit
        drawable.setColorFilter(getRandomColor(), PorterDuff.Mode.SRC_ATOP);
        drawable.setBounds(-getWidth()/3,-getHeight()/3,getWidth()/3,getHeight()/3);
        drawable.draw(canvas);
    }
    @ColorInt
    private int getRandomColor(){
        return Color.argb(255,(int)(Math.random()*255+1),(int)(Math.random()*255+1),(int)(Math.random()*255+1));
    }
}
