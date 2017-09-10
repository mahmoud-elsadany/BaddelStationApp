package baddel.baddelstationapp.internalStorage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;


public class SQliteDB extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "stationInternalDatabase.db";
    //station data table
    public static final String STATION_TABLE_NAME = "station";
    public static final String STATION_COLUMN_ID = "id";
    public static final String STATION_COLUMN_STATIONID = "stationid";

    //finished Trips log table
    public static final String finishedBikes_TABLE_NAME = "finishedTrips";
    public static final String finishedBikes_COLUMN_ID = "finishedTripsId";
    public static final String finishedBikes_COLUMN_finishedTripsObject = "finishedTripsObject";

    //started Trips log table
    public static final String startedBikes_TABLE_NAME = "startedTrips";
    public static final String startedBikes_COLUMN_ID = "startedTripsId";
    public static final String startedBikes_COLUMN_startedTripsObject = "startedTripsObject";

    //logs table
    public static final String logs_TABLE_NAME = "logs";
    public static final String logs_COLUMN_ID = "logsId";
    public static final String logs_COLUMN_logsObject = "logsObject";

    private HashMap hp;

    public SQliteDB(Context context)
    {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table "+STATION_TABLE_NAME + " ( "
                        + STATION_COLUMN_ID +" integer primary key AUTOINCREMENT,"
                        + STATION_COLUMN_STATIONID +" text )"
        );
        db.execSQL(
                "create table "+finishedBikes_TABLE_NAME + " ( "
                        + finishedBikes_COLUMN_ID +" integer primary key AUTOINCREMENT,"
                        + finishedBikes_COLUMN_finishedTripsObject +" text )"
        );
        db.execSQL(
                "create table "+startedBikes_TABLE_NAME + " ( "
                        + startedBikes_COLUMN_ID +" integer primary key AUTOINCREMENT,"
                        + startedBikes_COLUMN_startedTripsObject +" text )"
        );
        db.execSQL(
                "create table "+logs_TABLE_NAME + " ( "
                        + logs_COLUMN_ID +" integer primary key AUTOINCREMENT,"
                        + logs_COLUMN_logsObject +" text )"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS "+STATION_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+finishedBikes_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+startedBikes_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+logs_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertStationID (String stationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(STATION_COLUMN_STATIONID, stationId);
        db.insert(STATION_TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }
    public int numberOfStations(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, STATION_TABLE_NAME);
        db.close();
        return numRows;
    }


    public boolean insertFinishedTrip (String jsonObject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(finishedBikes_COLUMN_finishedTripsObject, jsonObject);
        db.insert(finishedBikes_TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }
    public boolean insertStartedTrip (String jsonObject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(startedBikes_COLUMN_startedTripsObject, jsonObject);
        db.insert(startedBikes_TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }
    public boolean insertNewLog (String newLog) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(logs_COLUMN_logsObject, newLog);
        db.insert(logs_TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }

    public boolean updateLogs (int rowId, String newLog) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(logs_COLUMN_logsObject, newLog);
        db.update(logs_TABLE_NAME, contentValues, logs_COLUMN_ID+" = ? ", new String[] { Integer.toString(rowId) } );
        db.close();
        return true;
    }

    public int numberOfFinishedTrips(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, finishedBikes_TABLE_NAME);
        db.close();
        return numRows;
    }
    public int numberOfStartedTrips(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, startedBikes_TABLE_NAME);
        db.close();
        return numRows;
    }

    public void deleteAllFinishedTrips(){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from "+ finishedBikes_TABLE_NAME);
        db.close();
    }
    public void deleteAllStartedTrips(){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from "+ startedBikes_TABLE_NAME);
        db.close();
    }

    public ArrayList<String> getAllFinishedTrips() {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+finishedBikes_TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(finishedBikes_COLUMN_finishedTripsObject)));
            res.moveToNext();
        }

        db.close();
        return array_list;
    }
    public ArrayList<String> getAllStartedTrips() {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+startedBikes_TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(startedBikes_COLUMN_startedTripsObject)));
            res.moveToNext();
        }
        db.close();

        return array_list;
    }
    public String getAllLogs() {
        String myLogs  = "";

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+logs_TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            myLogs += res.getString(res.getColumnIndex(logs_COLUMN_logsObject));
            res.moveToNext();
        }
        db.close();
        return myLogs;
    }


    public boolean updateStationID (int id, String stationID, String note_content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(STATION_COLUMN_STATIONID, stationID);
        db.update(STATION_TABLE_NAME, contentValues, STATION_COLUMN_ID+" = ? ", new String[] { Integer.toString(id) } );

        db.close();
        return true;
    }
    public Integer deleteStation (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(STATION_TABLE_NAME,
                STATION_COLUMN_ID+" = ? ",
                new String[] { Integer.toString(id) });

    }

    public void deleteAllStationRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from "+ STATION_TABLE_NAME);
        db.close();
    }


    public String getFinishedTripByID(int finishedTripID) {
        String specialFinishedTrip = "";

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+finishedBikes_TABLE_NAME+" where "+finishedTripID+" = "+finishedTripID, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            specialFinishedTrip = res.getString(res.getColumnIndex(finishedBikes_COLUMN_finishedTripsObject));
            res.moveToNext();
        }
        db.close();
        return specialFinishedTrip;
    }

    public String getStationID() {
        String stationID = null;

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+STATION_TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            stationID=res.getString(res.getColumnIndex(STATION_COLUMN_STATIONID));
            res.moveToNext();
        }
        db.close();
        return stationID;
    }
    public ArrayList<String> getAllStations() {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+STATION_TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(STATION_COLUMN_STATIONID)));
            res.moveToNext();
        }
        return array_list;
    }
}
