package com.activerecycle.tripgauge;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    ImageButton imgbtn_back;
    TextView tv_back;
    Switch switch1, switch2, switch3, switch4;
    Button btn_mph, btn_reset, btn_clear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        imgbtn_back = (ImageButton) findViewById(R.id.imgbtn_back);
        tv_back = (TextView) findViewById(R.id.tv_back);
        switch1 = (Switch) findViewById(R.id.switch1);
        switch2 = (Switch) findViewById(R.id.switch2);
        switch3 = (Switch) findViewById(R.id.switch3);
        switch4 = (Switch) findViewById(R.id.switch4);
        btn_mph = (Button) findViewById(R.id.btn_mph);
        btn_reset = (Button) findViewById(R.id.btn_reset);
        btn_clear = (Button) findViewById(R.id.btn_clear);

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
