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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS "+STATION_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+finishedBikes_TABLE_NAME);
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
    public boolean insertFinishedTrip (String jsonObject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(finishedBikes_COLUMN_finishedTripsObject, jsonObject);
        db.insert(finishedBikes_TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }

    public int numberOfStations(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, STATION_TABLE_NAME);
        return numRows;
    }

    public int numberOfFinishedTrips(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, finishedBikes_TABLE_NAME);
        return numRows;
    }

    public void deleteAllFinishedTrips(){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from "+ finishedBikes_TABLE_NAME);
        db.close();
    }

    public boolean updateStationID (int id, String stationID, String note_content)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(STATION_COLUMN_STATIONID, stationID);
        db.update(STATION_TABLE_NAME, contentValues, STATION_COLUMN_ID+" = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteStation (Integer id)
    {
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

    public void deleteAllFinishedBikesRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from "+ finishedBikes_TABLE_NAME);
        db.close();
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
        return array_list;
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
        return stationID;
    }
}
