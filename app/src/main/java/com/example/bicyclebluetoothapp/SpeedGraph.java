package com.example.bicyclebluetoothapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class SpeedGraph extends View {
    public SpeedGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    int sweepAngle = 190;
    final int maxAngle = 240;

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
        super.onDraw(canvas);

        setBackgroundColor(Color.BLACK);

        Paint pnt_gray = new Paint();
        pnt_gray.setStrokeWidth(6f);
        pnt_gray.setColor(Color.GRAY);
        pnt_gray.setStyle(Paint.Style.STROKE);

        RectF rect = new RectF();
        rect = new RectF();
        rect.set(50, 30, 850, 780);
        canvas.drawArc(rect, 140, 260, false, pnt_gray);

        if (sweepAngle < maxAngle) {
            Paint pnt_green = new Paint();
            pnt_green.setStrokeWidth(50f);
            pnt_green.setColor(Color.rgb(146, 208, 80));
            pnt_green.setStyle(Paint.Style.STROKE);

            canvas.drawArc(rect, 140, sweepAngle, false, pnt_green);
        } else {
            Paint pnt_orange = new Paint();
            pnt_orange.setStrokeWidth(50f);
            pnt_orange.setColor(Color.rgb(255, 192, 0));
            pnt_orange.setStyle(Paint.Style.STROKE);

            canvas.drawArc(rect, 140, sweepAngle, false, pnt_orange);
        }

    }
}
