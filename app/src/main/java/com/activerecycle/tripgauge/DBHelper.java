package com.activerecycle.tripgauge;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "cycle.db";

    public DBHelper(@Nullable Context context, int version) {
        super(context, DATABASE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE TripLogTable ( tableId INT primary key, logLastId INT )");
        db.execSQL("CREATE TABLE TripLog( logId INT primary key, time TEXT, volt INT, amp INT, w INT )");
        db.execSQL("CREATE TABLE TripSTATS( tripId INTEGER primary key, name TEXT , date DATE not null, max_w INTEGER, used INTEGER, dist INTEGER, avrpwr INTEGER )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS TripLogTable");
        db.execSQL("DROP TABLE IF EXISTS TripLog");
        db.execSQL("DROP TABLE IF EXISTS TripSTATS");
    }

    // TripLogTable Table 데이터 입력
    public void insert_TripLogTable(int tableId, int logLastId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO TripLogTable VALUES("+ tableId +", " + logLastId + ")");
        db.close();
    }

    // TripLog Table 데이터 입력
    public void insert_TripLog(int id, String time, int volt, int amp) {
        int w = volt * amp;
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO TripLog VALUES("+ id +", '" + time + "', " + volt + ", " + amp + ", " + w + ")");
        db.close();
    }

    // TripSTATS Table 데이터 입력
    public void insert_TripSTATS(int id, String name, String date, int max_w, int used, int dist, int avrpwr) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO TripSTATS VALUES("+ id +", '" + name + "', '" + date + "', " + max_w + ", " + used + ", " + dist + ", " + avrpwr + ")");
        db.close();
    }

//    // Table 데이터 수정
//    public void Update(String table, String name, int age, String Addr) {
//        SQLiteDatabase db = getWritableDatabase();
//        db.execSQL("UPDATE "+ table +" SET age = " + age + ", ADDR = '" + Addr + "'" + " WHERE NAME = '" + name + "'");
//        db.close();
//    }

//    // Table 데이터 삭제
//    public void Delete(String table, String name) {
//        SQLiteDatabase db = getWritableDatabase();
//        db.execSQL("DELETE " + table +" WHERE NAME = '" + name + "'");
//        db.close();
//    }

    public void deleteTable(String TABLE_NAME) {
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }

    // TripLog Table 조회
    public String getLog() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM TripLog", null);
        while (cursor.moveToNext()) {
            result += "id : " + cursor.getInt(0)
                    + ", time : " + cursor.getString(1)
                    + ", volt : " + cursor.getInt(2)
                    + ", amp : " + cursor.getInt(3)
                    + ", w : " + cursor.getInt(4)
                    + "\n";
        }

        return result;
    }

    public long getProfilesCount(String TABLE_NAME) {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        db.close();
        return count;
    }

//    public int getMaxW() {
//        SQLiteDatabase db = getReadableDatabase();
//        Cursor cursor = db.rawQuery("SELECT MAX(w) FROM TripLog", null);
//        cursor.moveToFirst();
//        return cursor.getInt(0);
//    }

    public int getUsedW(int tableId) {
        Map map = getTripLog(tableId);
        ArrayList<Integer> wList = (ArrayList<Integer>) map.get("W");
        int first = wList.get(0);
        int last = wList.get(wList.size() - 1);
        int usedW = first - last;
        if (usedW < 0) {
            usedW = -usedW;
        }
        return usedW;
    }

    public int getAvgPwrW(int tableId) {
        Map map = getTripLog(tableId);
        ArrayList<Integer> wList = (ArrayList<Integer>) map.get("W");
        int sum = 0;

        for (int i = 0; i < wList.size(); i++) {
            sum += wList.get(i);
        }
        int avg = sum / wList.size();
        return avg;
    }

    // TripSTATS Table 조회
    public String getTripSTATS() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM TripSTATS", null);
        while (cursor.moveToNext()) {
            result += "id : " + cursor.getInt(0)
                    + " name : " + cursor.getString(1)
                    + ", date : " + cursor.getString(2)
                    + ", max_w : " + cursor.getInt(3)
                    + ", used : " + cursor.getInt(4)
                    + ", dist : " + cursor.getInt(5)
                    + ", avrpwr : " + cursor.getInt(6)
                    + "\n";
        }

        return result;
    }

    public Map getTripLogW(Map map, int tableId) {

        long tripLogTableCount = getProfilesCount("TripLogTable");
        if (tableId >= tripLogTableCount) {
            return map;
        }

        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Integer> list = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT logLastId FROM TripLogTable WHERE tableId = " + tableId, null);
        cursor.moveToFirst();
        int logLastId = cursor.getInt(0);

        cursor = db.rawQuery("SELECT logLastId FROM TripLogTable WHERE tableId = " + (tableId - 1), null);
        cursor.moveToFirst();
        int logFirstId = cursor.getInt(0);

        cursor = db.rawQuery("SELECT w FROM TripLog WHERE logId <= " + logLastId + " AND logId > " + logFirstId, null);
        while (cursor.moveToNext()) {
            list.add(cursor.getInt(0));
        }
        map.put("W", list);
        return map;
    }

    public Map getTripLog(int tableId) {

        Map map = new HashMap();

        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<String> list1 = new ArrayList<>();
        ArrayList<Integer> list2 = new ArrayList<>();
        ArrayList<Integer> list3 = new ArrayList<>();
        ArrayList<Integer> list4 = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT logLastId FROM TripLogTable WHERE tableId = " + tableId, null);
        cursor.moveToFirst();
        int logLastId = cursor.getInt(0);

        cursor = db.rawQuery("SELECT logLastId FROM TripLogTable WHERE tableId = " + (tableId - 1), null);
        cursor.moveToFirst();
        int logFirstId = cursor.getInt(0);

        // 1 : time
        // 2 : volt
        // 3 : amp
        // 4 : W
        cursor = db.rawQuery("SELECT * FROM TripLog WHERE logId <= " + logLastId + " AND logId > " + logFirstId, null);
        while (cursor.moveToNext()) {
            list1.add(cursor.getString(1));
            list2.add(cursor.getInt(2));
            list3.add(cursor.getInt(3));
            list4.add(cursor.getInt(4));
        }
        map.put("TIME", list1);
        map.put("VOLT", list2);
        map.put("AMP", list3);
        map.put("W", list4);
        return map;
    }

    public int getMaxW(int tableId) {
        Map map = getTripLog(tableId);
        ArrayList<Integer> wList = (ArrayList<Integer>) map.get("W");
        int max = wList.get(0);
        for (int i = 0; i < wList.size(); i++) {
            if (wList.get(i) > max) { max = wList.get(i); }
        }

        return max;
    }

    public Map getTripLogTime(Map map) {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Integer> list = new ArrayList<>();

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM TripLog", null);
        while (cursor.moveToNext()) {
            list.add(cursor.getInt(1));
        }
        map.put("TIME", list);
        return map;
    }

    // TripLogTable Table 조회
    public String getTripLogTable() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM TripLogTable", null);
        while (cursor.moveToNext()) {
            result += "#TripLogId : " + cursor.getInt(0)
                    + " lastLogId : " + cursor.getInt(1)
                    + "\n";
        }

        return result;
    }
}
