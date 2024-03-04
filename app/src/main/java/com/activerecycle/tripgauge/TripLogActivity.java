package com.activerecycle.tripgauge;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TripLogActivity extends AppCompatActivity {

    ImageButton imgbtn_back, btn_share;
    TextView tv_back, tv_untitled, tv_date, tv_used_wh, tv_dist_km, tv_avrpwr_w;
    LogGraph graph_log;

    DBHelper dbHelper;
    static Map dataMap = new HashMap();

    int TABLE_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triplog);

        dbHelper = new DBHelper(TripLogActivity.this, 1);

        imgbtn_back = (ImageButton) findViewById(R.id.imgbtn_back);
        btn_share = (ImageButton) findViewById(R.id.btn_share);

        tv_back = (TextView) findViewById(R.id.tv_back);
        tv_untitled = (TextView) findViewById(R.id.tv_untitled);
        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_used_wh = (TextView) findViewById(R.id.tv_used_wh);
        tv_dist_km = (TextView) findViewById(R.id.tv_dist_km);
        tv_avrpwr_w = (TextView) findViewById(R.id.tv_avrpwr_w);

        graph_log = (LogGraph) findViewById(R.id.graph_log);


        imgbtn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TABLE_ID = 11;
        graph_log.map = dbHelper.getTripLogW(dataMap, TABLE_ID);
        graph_log.maxW = dbHelper.getMaxW(TABLE_ID);

        Map tripSTATSmap = dbHelper.getTripSTATSbyID(TABLE_ID);
        String tripName = (String) tripSTATSmap.get("NAME");
        String tripDateTime = (String) tripSTATSmap.get("DATE");
        String[] s = tripDateTime.split(" ");
        String tripDate = s[0];
        int usedWh = (int) tripSTATSmap.get("USED");
        int dist = (int) tripSTATSmap.get("DIST");
        double ddist = dist * 0.01;
        int avrpwr = (int) tripSTATSmap.get("AVRPWR");

        tv_untitled.setText(tripName);
        tv_date.setText(tripDate);
        tv_used_wh.setText(usedWh + "Wh");
        tv_dist_km.setText(ddist + "KM");
        tv_avrpwr_w.setText(avrpwr + "W");
    }
}
