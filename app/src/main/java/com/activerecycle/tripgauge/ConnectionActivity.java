package com.activerecycle.tripgauge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class ConnectionActivity extends AppCompatActivity {

    ImageButton btn_menu, btn_reload;
    Button btn_settings, btn_trip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_connection);
        setContentView(R.layout.tmplayout);

//        btn_menu = (ImageButton) findViewById(R.id.btn_menu);
//        btn_reload = (ImageButton) findViewById(R.id.btn_reload);
//        btn_settings = (Button) findViewById(R.id.btn_settings);
//        btn_trip = (Button) findViewById(R.id.btn_trip);
//
//        btn_menu.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });
//
//        btn_reload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // 숙피치 앱 freindlist activity 참고
//            }
//        });
//
//        btn_settings.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(ConnectionActivity.this, SettingsActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        btn_trip.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(ConnectionActivity.this, TripLogActivity.class);
//                startActivity(intent);
//            }
//        });
    }
}
