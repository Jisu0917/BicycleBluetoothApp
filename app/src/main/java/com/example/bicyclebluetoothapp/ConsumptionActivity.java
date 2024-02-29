package com.example.bicyclebluetoothapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class ConsumptionActivity extends AppCompatActivity {

    TextView tv_title, tv_w, tv_ready, tv_speed, tv_KPH, tv_percent, tv_soc, tv_odo, tv_distance;
    ImageButton btn_menu;
    Canvas graph_speed, graph_battery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumption);

        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_w = (TextView) findViewById(R.id.tv_w);
        tv_ready = (TextView) findViewById(R.id.tv_ready);
        tv_speed = (TextView) findViewById(R.id.tv_speed);
        tv_KPH = (TextView) findViewById(R.id.tv_KPH);
        tv_percent = (TextView) findViewById(R.id.tv_percent);
        tv_soc = (TextView) findViewById(R.id.tv_soc);
        tv_odo = (TextView) findViewById(R.id.tv_odo);
        tv_distance = (TextView) findViewById(R.id.tv_distance);

        btn_menu = (ImageButton) findViewById(R.id.btn_menu);

        graph_speed = (Canvas) findViewById(R.id.graph_speed);
        graph_battery = (Canvas) findViewById(R.id.graph_battery);

        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConsumptionActivity.this, ConnectionActivity.class);
                startActivity(intent);
            }
        });

    }
}