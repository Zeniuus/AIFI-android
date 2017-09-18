package com.zeniuus.www.reactiontagging.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by zeniuus on 2017. 9. 18..
 */

public class DrawingView extends View{
    private static final String TAG = "DrawingView";

    private float left = 0, right = 0, top = 0, bottom = 0;
    private float center_x, center_y, select_x, select_y;

    Paint paint;
    boolean drawFlag;

    Callback callback;

    public interface Callback {
        public void callback(float left, float top, float right, float bottom);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);;
        paint.setColor(Color.RED);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);

        drawFlag = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "MotionEvent.ACTION_DOWN");
                center_x = e.getX();
                center_y = e.getY();
                left = center_x;
                right = center_x;
                top = center_y;
                bottom = center_y;
                Log.d(TAG, left + " / " + right);
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "MotionEvent.ACTION_UP");
                callback.callback(left, top, right, bottom);
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "MotionEvent.ACTION_MOVE");
                select_x = e.getX();
                select_y = e.getY();
                calculate_coordinate();
                break;
        }

        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (drawFlag)
            canvas.drawRect(left, top, right, bottom, paint);
        else
            canvas.drawColor(Color.TRANSPARENT);
    }

    private void calculate_coordinate() {
        if (center_x <= select_x && center_y <= select_y) {
            left = center_x;
            top = center_y;
            right = select_x;
            bottom = select_y;
        } else if (center_x >= select_x && center_y >= select_y) {
            left = select_x;
            top = select_y;
            right = center_x;
            bottom = center_y;
        } else if (center_x < select_x && center_y > select_y) {
            left = center_x;
            top = select_y;
            right = select_x;
            bottom = center_y;
        } else if (center_x > select_x && center_y < select_y) {
            left = select_x;
            top = center_y;
            right = center_x;
            bottom = select_y;
        }
    }

    public void setDrawFlag(boolean flag) {
        drawFlag = flag;
        if (!flag)
            invalidate();
    }

    public void setCallback(Callback callback) { this.callback = callback; }
}
