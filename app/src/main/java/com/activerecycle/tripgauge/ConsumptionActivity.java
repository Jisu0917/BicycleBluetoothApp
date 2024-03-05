package com.activerecycle.tripgauge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class ConsumptionActivity extends AppCompatActivity {
    static String EXTERNAL_STORAGE_PATH = "";

    // 앱에서 디바이스에게 주는 데이터
    int speed = 0;
    boolean btconnect = true;

    // 디바이스로부터 받는 데이터
    int volt = 0; // 전압값 (0~25)
    int amp = 0; // 전류값 (0~30)
    int soc = 0;  // 배터리 잔량

    // w = volt * amp;

    TextView tv_title, tv_w, tv_ready, tv_speed, tv_KPH, tv_percent, tv_soc, tv_odo, tv_distance;
    ImageButton btn_menu;
    SpeedGraph graph_speed;
    BatteryGraph graph_battery;

    // DBHelper
    DBHelper dbHelper;
    String tripName;

    static Map dataMap = new HashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumption);

        // For Record Activity
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getApplicationContext(), "외장 메모리가 마운트 되지 않았습니다.", Toast.LENGTH_LONG).show();
        } else {
            EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
        }

        checkDangerousPermissions();


        dbHelper = new DBHelper(ConsumptionActivity.this, 1);

        //tv_title = (TextView) findViewById(R.id.tv_title);
        tv_w = (TextView) findViewById(R.id.tv_w);
        tv_ready = (TextView) findViewById(R.id.tv_ready);
        tv_speed = (TextView) findViewById(R.id.tv_speed);
        tv_KPH = (TextView) findViewById(R.id.tv_KPH);
        tv_percent = (TextView) findViewById(R.id.tv_percent);
        //tv_soc = (TextView) findViewById(R.id.tv_soc);
        tv_odo = (TextView) findViewById(R.id.tv_odo);
        tv_distance = (TextView) findViewById(R.id.tv_distance);

        btn_menu = (ImageButton) findViewById(R.id.btn_menu);

        graph_speed = (SpeedGraph) findViewById(R.id.graph_speed);
        graph_battery = (BatteryGraph) findViewById(R.id.graph_battery);

        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConsumptionActivity.this, ConnectionActivity.class);
                startActivity(intent);
            }
        });

        // Connect 여부 표시
        if (btconnect) {
            // 블루투스 연결된 상태이면
            tv_ready.setText("Ready");
            tv_ready.setTextColor(Color.rgb(146, 208, 80));  //green

        } else {
            // 블루투스 연결 안 된 상태이면
            tv_ready.setText("Connect");
            tv_ready.setTextColor(Color.rgb(255, 0, 0));  //red

            graph_speed.speed = 99;
            graph_speed.invalidate();

            return;  // 아래 내용 무시
        }

        // gps 주행 속도 측정
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final LocationListener gpsLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                String provider = location.getProvider();  // 위치 정보
                //System.out.println("위치 정보 : " + provider + ", 속도 : " + location.getSpeed());
                speed = (int) location.getSpeed();

                // 주행 속도 화면에 반영
                graph_speed.speed = speed;
                graph_speed.invalidate();  //그래프 화면 갱신

                tv_speed.setText(speed+"");

                if (speed > 25) {
                    tv_speed.setTextColor(Color.rgb(255, 192, 0));
                    tv_KPH.setTextColor(Color.rgb(255, 192, 0));
                } else {
                    tv_speed.setTextColor(Color.WHITE);
                    tv_KPH.setTextColor(Color.WHITE);
                }
            }
        };

        if (Build.VERSION.SDK_INT >= 23
                && ContextCompat.checkSelfPermission(
                        getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ConsumptionActivity.this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            // 위치 정보를 원하는 시간, 거리마다 갱신해준다.
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000,
                    0,
                    gpsLocationListener);
        } else {
            // 위치 정보를 원하는 시간, 거리마다 갱신해준다.
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000,
                    0,
                    gpsLocationListener);
        }



        // 배터리
        soc = 75;

        graph_battery.soc = soc;
        // 받아오는 배터리 값 달라질 때마다 graph_battery.invalidate();
        tv_percent.setText(soc+"%");


        // ODO
        tv_odo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tv_odo.getText().equals("ODO")) {
                    tv_odo.setText("TRIPA");
                } else if (tv_odo.getText().equals("TRIPA")) {
                    tv_odo.setText("TRIPB");
                } else if (tv_odo.getText().equals("TRIPB")) {
                    tv_odo.setText("ODO");
                }
            }
        });





        // 로그 db에 기록
        final long[] mNow = new long[1];
        final Date[] mDate = new Date[1];
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
        mFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));


        final int[] endOfTrip = {0};
        // 5초마다 실행
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // 수행할 작업
                    mNow[0] = System.currentTimeMillis();
                    mDate[0] = new Date(mNow[0]);
                    String nowTime = mFormat.format(mDate[0]);

                    // W 표시
                    /*
                     * random() 난수 발생 코드는 확인용 코드임.
                     * - 추후 삭제 요망
                     * */
                    volt = (int) (Math.random() * 25);
                    amp = (int) (Math.random() * 30);
                    tv_w.setText(volt * amp + "W");
                    tv_w.invalidate();

                    int tripLogId = dbHelper.getTripLogLastId() + 1;

                    //dbHelper.printTablesCounts();

                    dbHelper.insert_TripLog(tripLogId, nowTime, volt, amp);
                    String allLog = dbHelper.getLog();
                    System.out.println(allLog);
                    dbHelper.insertTripLogLastId();


                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (endOfTrip[0] == 5) {
                        System.out.println("##### end Of Trip ! ");

                        Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showSaveTripDialog(tripLogId, nowTime);
                            }
                        }, 0);


                        break;
                    }

                    endOfTrip[0] += 1;
                }
            }
        }).start();
        
    }

    private void showSaveTripDialog(int tripLogCount, String nowTime) {
        View dialogView = (View) View.inflate(
                ConsumptionActivity.this, R.layout.dialog_savetrip, null);
        AlertDialog.Builder dig = new AlertDialog.Builder(ConsumptionActivity.this, R.style.Theme_Dialog);
        dig.setView(dialogView);
        dig.setTitle("Save this trip!");

        Toast.makeText(getApplicationContext(), "한글, 영문, 숫자만 입력 가능합니다.", Toast.LENGTH_SHORT).show();

        final EditText editText = (EditText) dialogView.findViewById(R.id.editText_tripTitle);
        editText.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Pattern ps = Pattern.compile("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ \\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55]+$");
                if (source.equals("") || ps.matcher(source).matches()) {
                    return source;
                }
                return "";
            }
        }});

        dig.setNegativeButton("Cancel", null);
        dig.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                tripName = String.valueOf(editText.getText());

                // 트립 저장
                int tripLogTableId = dbHelper.getTripLogTableLastId() + 1;

                dbHelper.insert_TripLogTable(tripLogTableId, tripLogCount);
                dbHelper.insertTripLogTableLastId();

                String allTripLogTable = dbHelper.getTripLogTable();
                System.out.println(allTripLogTable);

                int tripCount = dbHelper.getTripSTATSLastId();

                if (tripName == null) { tripName = "Untitled"; }
                dbHelper.insert_TripSTATS((int) tripCount, tripName, nowTime, dbHelper.getMaxW((int) tripLogTableId), dbHelper.getUsedW((int) tripLogTableId), 2150, dbHelper.getAvgPwrW((int) tripLogTableId));
                dbHelper.insertTripSTATSLastId();

                String allTrip = dbHelper.getTripSTATS();
                System.out.println(allTrip);


                // Trip 기록 개수 20개 넘으면 자동 삭제
                if (tripLogTableId + 1 > 20) {
                    long count;
                    for (int id = 1; id < tripLogTableId; id++) {

                        dbHelper.deleteTrip(id);
                        count = dbHelper.getProfileCount("TripLogTable");

                        dbHelper.insertTripLogLastId();
                        dbHelper.insertTripLogTableLastId();
                        dbHelper.insertTripSTATSLastId();

                        if (count < 20) { break; }
                    }
                }

            }
        });

        dig.setCancelable(false);
        dig.show();
    }

    private void checkDangerousPermissions() {
        String[] permissions = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            permissions = new String[] {
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        } else {
            permissions = new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (String permission : permissions) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "권한 있음", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }
}