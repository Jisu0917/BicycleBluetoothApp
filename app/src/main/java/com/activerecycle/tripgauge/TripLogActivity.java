package com.activerecycle.tripgauge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TripLogActivity extends AppCompatActivity {

    LinearLayout recordlist_layout;
    ImageButton imgbtn_back, btn_share;
    TextView tv_back, tv_untitled, tv_date, tv_used_wh, tv_dist_km, tv_avrpwr_w;
    LogGraph graph_log;

    DBHelper dbHelper;
    static Map dataMap = new HashMap();
    ArrayList<Map> statList;

    int TABLE_ID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triplog);

        dbHelper = new DBHelper(TripLogActivity.this, 1);

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

        setTripInfo();
        getTripListInfo();
    }

    private void setTripInfo() {
        if (TABLE_ID == -1) {
            //마지막 트립 불러오기
            long tripLogTableCount = dbHelper.getProfilesCount("TripLogTable");
            TABLE_ID = (int) tripLogTableCount - 1;
        }

        graph_log.map = dbHelper.getTripLogW(dataMap, TABLE_ID);
        graph_log.maxW = dbHelper.getMaxW(TABLE_ID);
        graph_log.invalidate();

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

    private void getTripListInfo() {
        statList = new ArrayList<>();
        int tripLogTableCount = (int) dbHelper.getProfilesCount("TripLogTable");
        for (int i = tripLogTableCount - 1; i >= 0; i--) {
            statList.add(dbHelper.getTripSTATSbyID(i));
        }

        setTripListView();
    }

    private void setTripListView() {
        recordlist_layout.removeAllViews();

        LayoutInflater layoutInflater = LayoutInflater.from(TripLogActivity.this);
        //LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (statList != null) {
//            //중복 제거 & id(tripSTATS table의 tripId)순으로 정렬
//            for (int i = 0; i < statList.size(); i++) {
//                System.out.println("statList.get(i) : " + statList.get(i));  //임시, 확인용
//                statList.get(i).put(friendInfoList.get(i).getId(), friendInfoList.get(i));
//            }
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
}
