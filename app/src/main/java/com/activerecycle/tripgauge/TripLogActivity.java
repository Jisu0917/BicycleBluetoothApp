package com.activerecycle.tripgauge;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class TripLogActivity extends AppCompatActivity {

    final String APP_NAME = "RECYCLE-Trip_gauge";

    ConstraintLayout container;
    LinearLayout recordlist_layout;
    ImageButton imgbtn_back, btn_share;
    TextView tv_back, tv_untitled, tv_date, tv_used_wh, tv_dist_km, tv_avrpwr_w;
    LogGraph graph_log;

    DBHelper dbHelper;
    static Map dataMap = new HashMap();
    ArrayList<Map> statList;

    int TABLE_ID = -1;
    String tripName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triplog);

        dbHelper = new DBHelper(TripLogActivity.this, 1);

        container = (ConstraintLayout) findViewById(R.id.container);
        recordlist_layout = (LinearLayout) findViewById(R.id.recordlist_layout);

        imgbtn_back = (ImageButton) findViewById(R.id.imgbtn_back);
        btn_share = (ImageButton) findViewById(R.id.btn_share);

        tv_back = (TextView) findViewById(R.id.tv_back);
        tv_untitled = (TextView) findViewById(R.id.tv_untitled);
        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_used_wh = (TextView) findViewById(R.id.tv_used_wh);
        tv_dist_km = (TextView) findViewById(R.id.tv_dist_km);
        tv_avrpwr_w = (TextView) findViewById(R.id.tv_avrpwr_w);

        graph_log = (LogGraph) findViewById(R.id.graph_log);

        tv_untitled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTripReviseDialog();
            }
        });


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

        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bitmap bitmap = getBitmapFromView(container, container.getChildAt(0).getHeight(), container.getChildAt(0).getWidth());
                try {

                    File directory;
                    if (Build.VERSION.SDK_INT >= 30){

                        if (!Environment.isExternalStorageManager()){
                            Intent getpermission = new Intent();
                            getpermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            startActivity(getpermission);
                        }


                        File destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                        if (!destination.exists()) { // 원하는 경로에 폴더가 있는지 확인
                            destination.mkdirs();
                            Log.d("TripLogActivity", "destination Created");
                        }

                        directory = new File(destination + File.separator + APP_NAME);
                        if (!directory.exists()) { // 원하는 경로에 폴더가 있는지 확인
                            directory.mkdirs();
                            Log.d("TripLogActivity", "Directory Created");
                        }
                    } else{
                        directory = new File(Environment.getExternalStorageDirectory() + File.separator + APP_NAME);
                        if (!directory.exists()) { // 원하는 경로에 폴더가 있는지 확인
                            directory.mkdirs();
                            Log.d("TripLogActivity", "Directory Created");
                        }
                    }

                    String filename = APP_NAME + "+" + TABLE_ID + "_" + getNowTime() +".jpg";
                    filename = filename.replaceAll(" ", "+");
                    filename = filename.replaceAll(":", "");

                    File file = new File(directory, filename);
                    if (file.exists()) {
                        file.delete();
                        file = new File(directory,filename);
                    }

                    System.out.println("file: " + file);  //TODO: 삭제하기 ( 임시, 확인용 )

                    FileOutputStream output = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
                    output.flush();
                    output.close();


                    Intent intent = new Intent(Intent.ACTION_SEND);
                    Uri uri = Uri.parse(file.getPath());
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(intent, "Share img"));

                    //Toast.makeText(TripLogActivity.this, "이미지를 공유했습니다.", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(TripLogActivity.this, "이미지를 공유하는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setTripInfo();
        getTripListInfo();
    }

    private void setTripInfo() {
        boolean isCurrent = false;
        int tripLogTableLastId = dbHelper.getTripLogTableLastId();
        if (TABLE_ID == tripLogTableLastId) {
            isCurrent = true;
        } else if (TABLE_ID == -1) {
            //마지막 트립 불러오기
            TABLE_ID = tripLogTableLastId;
        }

        graph_log.map = dbHelper.getTripLogW(dataMap, TABLE_ID);
        graph_log.maxW = dbHelper.getMaxW(TABLE_ID);
        graph_log.invalidate();

        Map tripSTATSmap = dbHelper.getTripSTATSbyID(TABLE_ID);
        String tripName = (String) tripSTATSmap.get("NAME");
        String tripDateTime = (String) tripSTATSmap.get("DATE");
        String[] s = tripDateTime.split(" ");
        String tripDate = s[0];
        //if (isCurrent) { tripDate += " (CURRENT)"; }
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

    private void getTripListInfo() {
        statList = new ArrayList<>();
        int tripLogTableLastId = (int) dbHelper.getTripLogTableLastId();
        for (int i = tripLogTableLastId; i >= 1; i--) {
            statList.add(dbHelper.getTripSTATSbyID(i));
        }

        setTripListView();
    }

    private void setTripListView() {
        recordlist_layout.removeAllViews();

        LayoutInflater layoutInflater = LayoutInflater.from(TripLogActivity.this);
        //LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (statList != null) {
            Map map;
            int tripID;
            String tripName, tripDateTime, tripDate;
            for (int i = 0; i < statList.size(); i++) {
                map = statList.get(i);
                if ( map.size() !=0 ) {
                    tripID = (int) map.get("ID");
                    tripName = (String) map.get("NAME");
                    tripDateTime = (String) map.get("DATE");
                    String[] s = tripDateTime.split(" ");
                    tripDate = s[0];

                    View customView = layoutInflater.inflate(R.layout.custom_record_info, null);
                    ((LinearLayout) customView.findViewById(R.id.container)).setTag(tripID + "");
                    ((TextView) customView.findViewById(R.id.tv_name)).setText(tripName);
                    ((TextView) customView.findViewById(R.id.tv_date)).setText(tripDate);

                    recordlist_layout.addView(customView);
                }
            }

        } else {
            System.out.println("statList is null...");
        }
    }

    // Trip 목록에서 특정 Trip을 클릭했을 때
    public void onClickRecord(View view) {
        TABLE_ID = Integer.parseInt(view.getTag().toString());

        setTripInfo();
    }

    private Bitmap getBitmapFromView(View view, int height, int width) {
        Bitmap bitmap = Bitmap.createBitmap(width+190, height,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return bitmap;
    }

    private void showTripReviseDialog() {
        View dialogView = (View) View.inflate(
                TripLogActivity.this, R.layout.dialog_savetrip, null);
        AlertDialog.Builder dig = new AlertDialog.Builder(TripLogActivity.this, R.style.Theme_Dialog);
        dig.setView(dialogView);
        dig.setTitle("Revise this trip title!");

        Toast.makeText(getApplicationContext(), "한글, 영문, 숫자만 입력 가능합니다.", Toast.LENGTH_LONG).show();

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

        editText.setText(tv_untitled.getText().toString());

        dig.setNegativeButton("Cancel", null);
        dig.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                tripName = String.valueOf(editText.getText());

                if (tripName != null) {
                    dbHelper.update_TripName(TABLE_ID, tripName);

                    tv_untitled.setText(tripName);
                    getTripListInfo();
                    setTripListView();
                }
            }
        });
        dig.setCancelable(false);
        dig.show();

    }

    private String getNowTime() {

        Date currentTime = Calendar.getInstance().getTime();
        return currentTime.toString();
    }
}
