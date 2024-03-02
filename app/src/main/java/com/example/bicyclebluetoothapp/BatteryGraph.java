package com.example.bicyclebluetoothapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;

public class BatteryGraph extends View {

    int percent = 75;

    public BatteryGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
        super.onDraw(canvas);

        //핸드폰 화면크기 가져오기
        DisplayMetrics metrics = new DisplayMetrics();
        getDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        setBackgroundColor(Color.BLACK);

        // 막대 길이 계산
        int full_length = screenWidth * 90 / 100;
        int length = percent * full_length / 100;

        Paint paint = new Paint(); // 페인트 객체 생성
        if (percent > 10) {
            paint.setShader(new LinearGradient(100,0, length/2 +200,0, Color.BLACK, Color.rgb(146, 208, 80), Shader.TileMode.CLAMP));
        } else {
            paint.setShader(new LinearGradient(40,0, length/2 +200,0, Color.BLACK, Color.rgb(255, 20, 20), Shader.TileMode.CLAMP));
        }
        canvas.drawRect(screenWidth/2-500, 30, screenWidth/2-500 + length, 170, paint);

        Paint paint2 = new Paint(); // 페인트 객체 생성
        paint2.setColor(Color.WHITE);
        paint2.setStrokeWidth(4f);
        paint2.setStyle(Paint.Style.STROKE);
        canvas.drawRect(screenWidth/2-500, 30, screenWidth/2-500 + full_length, 170, paint2);


    }
}
