package com.activerecycle.tripgauge;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "cycle.db";

    public DBHelper(@Nullable Context context, int version) {
        super(context, DATABASE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /*
         * 1. AUTOINCREMENT 설정은 시작한다. - tableId, logId, tripId 및 rowid 해당
         * 2. Trip 한 회당 TripLogTable 1개, TripSTATS 1개.
         * */
        db.execSQL("CREATE TABLE TripLogTable ( tableId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, logLastId INTEGER )");
        db.execSQL("CREATE TABLE TripLog( logId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, time TEXT, volt INTEGER, amp INTEGER, w INTEGER )");
        db.execSQL("CREATE TABLE TripSTATS( tripId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT , date DATE not null, max_w INTEGER, used INTEGER, dist INTEGER, avrpwr INTEGER )");

        /*
        * 각 테이블의 마지막 Id를 기록한다.
        * */
        db.execSQL("CREATE TABLE TripLogTableLastId ( rowid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, lastId INTEGER DEFAULT -1 )");
        db.execSQL("CREATE TABLE TripLogLastId ( rowid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, lastId INTEGER DEFAULT -1 )");
        db.execSQL("CREATE TABLE TripSTATSLastId ( rowid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, lastId INTEGER DEFAULT -1 )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS TripLogTable");
        db.execSQL("DROP TABLE IF EXISTS TripLog");
        db.execSQL("DROP TABLE IF EXISTS TripSTATS");
    }

    public void insertTripLogTableLastId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(tableId) FROM TripLogTable", null);
        if (cursor != null && cursor.moveToFirst()) {
            int lastId = cursor.getInt(0);
            db.execSQL("INSERT INTO TripLogTableLastId ( lastId ) VALUES (" + lastId + ")");
        } else {
            System.out.println("##### insertTripLogTableLastId : Error 발생!");
        }
        cursor.close();
        db.close();
    }

    public void insertTripLogLastId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(logId) FROM TripLog", null);
        if (cursor != null && cursor.moveToFirst()) {
            int lastId = cursor.getInt(0);
            db.execSQL("INSERT INTO TripLogLastId ( lastId ) VALUES (" + lastId + ")");
        }
        cursor.close();
        db.close();
    }

    public void insertTripSTATSLastId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(tripId) FROM TripSTATS", null);
        if (cursor != null && cursor.moveToFirst()) {
            int lastId = cursor.getInt(0);
            db.execSQL("INSERT INTO TripSTATSLastId ( lastId ) VALUES (" + lastId + ")");
        }
        cursor.close();
        db.close();
    }


    // TripLogTable Table 데이터 입력
    public void insert_TripLogTable(int tableId, int logLastId) {
        SQLiteDatabase db = getWritableDatabase();
        //db.execSQL("INSERT INTO TripLogTable VALUES("+ tableId +", " + logLastId + ")");
        db.execSQL("INSERT INTO TripLogTable (logLastId) VALUES(" + logLastId + ")");
        db.close();
    }

    // TripLog Table 데이터 입력
    public void insert_TripLog(int id, String time, int volt, int amp) {
        int w = volt * amp;
        SQLiteDatabase db = getWritableDatabase();
        //db.execSQL("INSERT INTO TripLog VALUES("+ id +", '" + time + "', " + volt + ", " + amp + ", " + w + ")");
        db.execSQL("INSERT INTO TripLog (time, volt, amp, w) VALUES('" + time + "', " + volt + ", " + amp + ", " + w + ")");
        db.close();
    }

    // TripSTATS Table 데이터 입력
    public void insert_TripSTATS(int id, String name, String date, int max_w, int used, int dist, int avrpwr) {
        SQLiteDatabase db = getWritableDatabase();
        //db.execSQL("INSERT INTO TripSTATS VALUES("+ id +", '" + name + "', '" + date + "', " + max_w + ", " + used + ", " + dist + ", " + avrpwr + ")");
        db.execSQL("INSERT INTO TripSTATS (name, date, max_w, used, dist, avrpwr) VALUES('" + name + "', '" + date + "', " + max_w + ", " + used + ", " + dist + ", " + avrpwr + ")");
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

    public void deleteTrip(int tripId) {
        //tripId == tableId, 1부터 시작
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT logLastId FROM TripLogTable WHERE tableId = " + tripId, null);
        if (cursor != null && cursor.moveToFirst()) {
            int logLastId = cursor.getInt(0);
            int logFirstId = 1;

            if (tripId != 1) {
                cursor = db.rawQuery("SELECT logLastId FROM TripLogTable WHERE tableId = " + (tripId - 1), null);
                if (cursor != null && cursor.moveToFirst()) {
                    logFirstId = cursor.getInt(0);
                }
            }

            deleteTripLogTable(tripId);
            deleteTripSTATS(tripId);
            deleteTripLog(logFirstId, logLastId);
        }

        cursor.close();
        db.close();
    }

    public void deleteTripLogTable(int tableId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM TripLogTable WHERE tableId = " + tableId);
        db.close();
    }

    public void deleteTripSTATS(int tripId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM TripSTATS WHERE tripId = " + tripId);
        db.close();
    }

    public void deleteTripLog(int firstLogId, int lastLogId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM TripLog WHERE logId > " + firstLogId + " AND logId <= " + lastLogId);
        db.close();
    }



    // TripLog Table 조회
    public String getLog() {
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
        cursor.close();
        db.close();
        return result;
    }

    public long getProfileCount(String TABLE_NAME) {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        db.close();
        return count;
    }

    public int getTripLogTableLastId() {
        if (getProfileCount("TripLogTable") <= 0) {
            return 0;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TripLogTableLastId ORDER BY rowid DESC LIMIT 1", null);
        if (cursor != null && cursor.moveToFirst()) {
            int lastId = cursor.getInt(1);
            if (lastId != -1) {  // DEFAULT 값인 1이 아니면 = 값이 들어있으면
                cursor.close();
                db.close();
                return lastId;
            }

            lastId = -999;

            cursor.close();
            db.close();

            return lastId;
        }

        cursor.close();
        return -888;

    }

    public int getTripLogLastId() {
        if (getProfileCount("TripLog") <= 0) {
            return 0;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TripLogLastId ORDER BY rowid DESC LIMIT 1", null);
        if (cursor != null && cursor.moveToFirst()) {  // DEFAULT 값인 1이 아니면 = 값이 들어있으면
            int lastId = cursor.getInt(1);   //the latest column
            if (lastId != -1) {
                cursor.close();
                db.close();
                return lastId;
            }

            lastId = -999;

            cursor.close();
            db.close();

            return lastId;
        }

        cursor.close();
        return -888;

    }

    public int getTripSTATSLastId() {
        if (getProfileCount("TripSTATS") <= 0) {
            return 0;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TripSTATSLastId ORDER BY rowid DESC LIMIT 1", null);
        if (cursor != null && cursor.moveToFirst()) {  // DEFAULT 값인 1이 아니면 = 값이 들어있으면
            int lastId = cursor.getInt(1);
            if (lastId != -1) {
                cursor.close();
                db.close();
                return lastId;
            }

            lastId = -999;

            cursor.close();
            db.close();

            return lastId;
        }

        cursor.close();
        return -888;
    }

//    public int getTablesCount(String TABLE_NAME) {
//        if (getProfileCount(TABLE_NAME) <= 0) {
//            return -1;
//        }
//
//        System.out.println("@@@@ START of getTablesCount() //////");
//
//        SQLiteDatabase db = this.getReadableDatabase();
////        Cursor cursor = db.rawQuery("SELECT '" + TABLE_NAME + "' FROM TablesCounts ORDER BY column DESC LIMIT 1", null);
////        cursor.moveToFirst();  //the latest column
//
//        Cursor cursor = db.rawQuery("SELECT '" + TABLE_NAME + "' FROM TablesCounts", null);
//        cursor.moveToPosition(cursor.getCount() - 1);
//        int index = cursor.getInt(0);
//        if (index != -1) {
//            db.close();
//            return index;
//        }
//
//        for (int i = cursor.getCount() - 2; i >= 0; i++) {
//            cursor.moveToPosition(i);
//            index = cursor.getInt(0);
//            if (index != -1) { break; }
//        }
//
////        while (cursor.moveToPrevious()) {
////            index = cursor.getInt(0);
////            if (index != -1) { break; }
////        }
//
//        if (index == -1) { index = -999; }
//
//        db.close();
//
//        return index;
//    }

//    // TripLog Table 조회
//    public void printTablesCounts() {
//        SQLiteDatabase db = getReadableDatabase();
//        String result = "";
//
//        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
//        Cursor cursor = db.rawQuery("SELECT * FROM TablesCounts", null);
//        while (cursor.moveToNext()) {
//            result += "TripLogTable count : " + cursor.getInt(0)
//                    + ", TripLog count : " + cursor.getInt(1)
//                    + ", TripSTATS count : " + cursor.getInt(2)
//                    + "\n";
//        }
//        System.out.println(result);
//        db.close();
//    }

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
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

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

        cursor.close();
        db.close();
        return result;
    }

    public Map getTripSTATSbyID(int tripId) {  //tripId equals tableId (probably)
        SQLiteDatabase db = getReadableDatabase();
        Map map = new HashMap();

        Cursor cursor = db.rawQuery("SELECT * FROM TripSTATS WHERE tripId = " + tripId, null);
        if (cursor != null && cursor.moveToFirst()) {
            map.put("ID", cursor.getInt(0));
            map.put("NAME", cursor.getString(1));
            map.put("DATE", cursor.getString(2));
            map.put("MAX_W", cursor.getInt(3));
            map.put("USED", cursor.getInt(4));
            map.put("DIST", cursor.getInt(5));
            map.put("AVRPWR", cursor.getInt(6));
        }

        cursor.close();
        db.close();
        return map;
    }

    public void update_TripName(int tripId, String tripName) {
        SQLiteDatabase db = getWritableDatabase();
        //UPDATE 테이블명 SET 컬럼명1=값1, 컬럼명2=값2 WHERE 조건식
        db.execSQL("UPDATE TripSTATS SET name = '" + tripName + "' WHERE tripId = " + tripId);
        db.close();
    }

    public Map getTripLogW(Map map, int tableId) {

        int tripLogTableLastId = getTripLogTableLastId();
        if (tableId > tripLogTableLastId) {
            //TODO: 이 부분 정교하게 !! 보완
            return map;
        }

        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Integer> list = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT logLastId FROM TripLogTable WHERE tableId = " + tableId, null);
        if (cursor != null && cursor.moveToFirst()) {
            int logLastId = cursor.getInt(0);


            int logFirstId = 0;
            if (tableId != 0) {
                cursor = db.rawQuery("SELECT logLastId FROM TripLogTable WHERE tableId = " + (tableId - 1), null);
                if (cursor != null && cursor.moveToFirst()) {
                    logFirstId = cursor.getInt(0);
                }
            }

            cursor = db.rawQuery("SELECT w FROM TripLog WHERE logId <= " + logLastId + " AND logId > " + logFirstId, null);
            while (cursor.moveToNext()) {
                list.add(cursor.getInt(0));
            }
            map.put("W", list);
        }

        cursor.close();
        db.close();
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
        int logLastId, logFirstId;
        if (cursor != null && cursor.moveToFirst()) {
            logLastId = cursor.getInt(0);

            logFirstId = 0;
            if (tableId != 0) {
                cursor = db.rawQuery("SELECT logLastId FROM TripLogTable WHERE tableId = " + (tableId - 1), null);
                if (cursor != null && cursor.moveToFirst()) {
                    logFirstId = cursor.getInt(0);
                }
            }

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

            cursor.close();
            db.close();
            return map;

        } else {
            cursor.close();
            return null;
        }
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
        db.close();
        cursor.close();
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

        cursor.close();
        db.close();
        return result;
    }
}
