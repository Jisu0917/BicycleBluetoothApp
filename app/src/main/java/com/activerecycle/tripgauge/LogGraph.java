package com.activerecycle.tripgauge;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class LogGraph extends View {

    static Map map;
    static int n = 0, m = 0;  // 카테고리 개수
    static float max, min;
    static int maxW;
    final static int DEFINED_MAX_W = 750;

    public LogGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);

        //map = TripLogActivity.dataMap;  // - 그래프 확인을 위해 주석 처리 해둠.
        map = ConsumptionActivity.dataMap;

        // 데이터가 비어있으면 그래프를 그릴 수 없다.
        if (map.size() == 0) {
            System.out.println("!!! map size is 0.");
            return;
        }

        ArrayList<Integer> original_wList = (ArrayList<Integer>) map.get("W");
        n = original_wList.size();

        System.out.println("@@@@@ original_wList : " + original_wList);

        ArrayList<Float> value = new ArrayList<>();
        float fitC = 0.5f;
        for (int i = 0; i < n; i++) {
            int k = original_wList.get(i);
            value.add(k * fitC);
        }

        System.out.println("@@@@@ adjust wList : " + value);

        // 최대값, 최소값 구하기
        max = value.get(0);
        min = value.get(0);
        for (int i=1; i < n; i++) {
            if (value.get(i) > max) max = value.get(i);
            if (value.get(i) < min) min = value.get(i);
        }

        final int top = 150;
        final int bottom = getHeight() -550;
        final int margin = 120;
        m = n;
        final int dotDistance = (getWidth() - 2*margin) / m;  // 간격 개수 m - 1, 시작끝 좌우여백
        //final int dotDistance = 10;
        final int firstDotX = margin + dotDistance/2;

        Paint dotPaint = new Paint();
        dotPaint.setColor(Color.RED);
        Paint linePaint = new Paint();
        DashPathEffect dashPath = new DashPathEffect(new float[]{5,10}, 2);
        linePaint.setStyle( Paint.Style.STROKE );
        linePaint.setPathEffect(dashPath);
        linePaint.setStrokeWidth(3);
        Path p = new Path();
        p.moveTo(firstDotX, value.get(0)/max);

        float adjustC = 0.5f * DEFINED_MAX_W;
        int adjustY = -200;
        for (int i = 0; i < n; i++) {
            // 꼭짓점 그리기
            //canvas.drawCircle(firstDotX + dotDistance * i, bottom - (adjustC) * (value.get(i)/max), 5, dotPaint);
            canvas.drawCircle(firstDotX + dotDistance * i, bottom - (value.get(i) * max / adjustC) + adjustY, 5, dotPaint);

        }







        for (int j = 0; j < m; j++) {

            // 점선 그리기 (optional)
            if (value.get(j) == max) {
                canvas.drawLine(firstDotX + dotDistance * j,  value.get(j) / max, firstDotX + dotDistance * j, bottom, linePaint);  // 세로 점선 그리기 (optional)

            }
            if (value.get(j) == max) {
                canvas.drawLine(margin, value.get(j) / max, firstDotX + dotDistance * j, value.get(j) / max, linePaint);  // 가로 점선 그리기 (optional)

            }

            // "이전 꼭짓점"과 연결해주는 선 그리기
            if (j > 0) {
                p.lineTo(firstDotX + dotDistance * (j-1), value.get(j-1) / max);
            }
        }

    }
}
