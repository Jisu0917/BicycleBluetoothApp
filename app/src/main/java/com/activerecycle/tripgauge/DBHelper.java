package com.activerecycle.tripgauge;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "cycle.db";

    public DBHelper(@Nullable Context context, int version) {
        super(context, DATABASE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE TripLog( logId INT primary key, time TEXT, volt INT, amp INT, w INT )");
        db.execSQL("CREATE TABLE Trip( tripId INTEGER primary key, name TEXT , date DATE not null, max_w INTEGER, used INTEGER, dist INTEGER, avrpwr INTEGER )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS TripLog");
        onCreate(db);
    }

    // TripLog Table 데이터 입력
    public void insert_TripLog(int id, String time, int volt, int amp) {
        int w = volt * amp;
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO TripLog VALUES("+ id +", '" + time + "', " + volt + ", " + amp + ", " + w + ")");
        db.close();
    }

    // Trip Table 데이터 입력
    public void insert_Trip(int id, String name, String date, int max_w, int used, int dist, int avrpwr) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO Trip VALUES("+ id +", '" + name + "', '" + date + "', " + max_w + ", " + used + ", " + dist + ", " + avrpwr + ")");
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

    public int getMaxW() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(w) FROM TripLog", null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public int getUsedW() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TripLog", null);
        cursor.moveToFirst();
        int first = cursor.getInt(4);
        cursor.moveToLast();
        int last = cursor.getInt(4);
        int usedW = first - last;
        if (usedW < 0) {
            usedW = -usedW;
        }
        return usedW;
    }

    public int getAvgPwrW() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT AVG(w) FROM TripLog", null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    // Trip Table 조회
    public String getTrip() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM Trip", null);
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
}
