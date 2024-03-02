package com.activerecycle.tripgauge;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TripLogActivity extends AppCompatActivity {

    ImageButton imgbtn_back, btn_share;
    TextView tv_back, tv_untitled, tv_date, tv_used_wh, tv_dist_km, tv_avrpwr_w;
    LogGraph graph_log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triplog);

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
    }
}
