package com.activerecycle.tripgauge;

import static android.graphics.Path.FillType.EVEN_ODD;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
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

        map = TripLogActivity.dataMap;  // - 그래프 확인을 위해 주석 처리 해둠.
        //map = ConsumptionActivity.dataMap;

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
        final int margin = 150;
        m = n;
        final int dotDistance = (getWidth() - 2*margin) / m;  // 간격 개수 m - 1, 시작끝 좌우여백
        //final int dotDistance = 10;
        final int firstDotX = 80 + margin + dotDistance/2;

        Paint dotPaint = new Paint();
        dotPaint.setColor(Color.WHITE);
        dotPaint.setStrokeWidth(0);

        Paint pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setStyle(Paint.Style.FILL);
        LinearGradient linearGradient = new LinearGradient(200, 300, 200, 600, Color.rgb(235, 0, 0), Color.BLACK, Shader.TileMode.CLAMP);
        pathPaint.setShader(linearGradient);

        Paint pathStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathStrokePaint.setStyle(Paint.Style.STROKE);
        pathStrokePaint.setColor(Color.WHITE);
        pathStrokePaint.setStrokeWidth(5f);

        Path p = new Path();
        p.setFillType(EVEN_ODD);


        float adjustC = 0.5f * DEFINED_MAX_W;
        int adjustY = 230;

        p.moveTo(210, getHeight() - 40);
        //p.moveTo(firstDotX, bottom - (value.get(0) * max / adjustC) + adjustY);
        p.lineTo(firstDotX, bottom - (0 * max / adjustC) + adjustY);
        for (int i = 0; i < n; i++) {
            // 꼭짓점 그리기
            //canvas.drawCircle(firstDotX + dotDistance * i, bottom - (value.get(i) * max / adjustC) + adjustY, 5, dotPaint);

            // "이전 꼭짓점"과 연결해주는 선 그리기
            if (i > 0) {
                p.lineTo(firstDotX + dotDistance * (i-1), bottom - (value.get(i-1) * max / adjustC) + adjustY);
            }

        }
        p.lineTo(firstDotX + dotDistance * (n-1), bottom - (value.get(n-1) * max / adjustC) + adjustY);
        p.lineTo((getWidth() - 60), (getHeight() - 40));
        //p.close();
        canvas.drawPath(p, pathPaint);
        canvas.drawPath(p, pathStrokePaint);

        p.setFillType(Path.FillType.INVERSE_WINDING);


        // 흰색 사각형 틀 (X, Y축을 대신한다.)
        Paint paint = new Paint(); // 페인트 객체 생성
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        canvas.drawRoundRect(200, 100, getWidth() - 60, getHeight() - 40, 25f, 25f, paint);

        // 0 그리기
        Paint txtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        txtPaint.setColor(Color.WHITE);
        txtPaint.setTextSize(50f);
        txtPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("0", 150, getHeight() - 50, txtPaint);

        // 최댓값 숫자
        canvas.drawText(maxW + "", 150, bottom - (max * max / adjustC) + adjustY, txtPaint);

        // 최댓값 점선
        Paint linePaint = new Paint();
        DashPathEffect dashPath = new DashPathEffect(new float[]{20,20}, 2);
        linePaint.setStyle( Paint.Style.STROKE );
        linePaint.setPathEffect(dashPath);
        linePaint.setStrokeWidth(4);
        linePaint.setColor(Color.WHITE);

        Path path = new Path();
        for (int i = 0; i < n; i++) {
            if (value.get(i) == max) {
                path.moveTo(200, bottom - (value.get(i) * max / adjustC) + adjustY);
                path.lineTo(firstDotX + dotDistance * i, bottom - (value.get(i) * max / adjustC) + adjustY);
            }
        }
        canvas.drawPath(path, linePaint);

        // Consumption Log 글씨 넣기
        txtPaint.setTextSize(60f);
        canvas.drawText("CONSUMPTION LOG", getWidth() - 80, 180, txtPaint);

    }
}
