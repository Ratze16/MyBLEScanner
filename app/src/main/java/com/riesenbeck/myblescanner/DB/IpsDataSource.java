package com.riesenbeck.myblescanner.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.riesenbeck.myblescanner.Data.Beacon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by michael on 12.10.2016.
 */

public class IpsDataSource {
    private static final String LOG_TAG = IpsDataSource.class.getSimpleName();

    private SQLiteDatabase database;
    private IpsDbHelper dbHelper;

    private String[] BeaconColumns = {
            IpsDbHelper.BEACON_ID,
            IpsDbHelper.ROOM_ID,
            IpsDbHelper.BEACON_LONGITUDE,
            IpsDbHelper.BEACON_LATITUDE,
            IpsDbHelper.BEACON_HEIGHT,
            IpsDbHelper.BEACON_ADDRESSE,
            IpsDbHelper.BEACON_RSSI
    };


    public IpsDataSource(Context context) {
        Log.d(LOG_TAG, "Unsere DataSource erzeugt jetzt den dbHelper.");
        dbHelper = new IpsDbHelper(context);
    }

    public void open() {
        try {
            Log.d(LOG_TAG, "Eine Referenz auf die Datenbank wird jetzt angefragt.");
            database = dbHelper.getWritableDatabase();
            Log.d(LOG_TAG, "Datenbank-Referenz erhalten. Pfad zur Datenbank: " + database.getPath());
        }catch (SQLException e){
            Log.d(LOG_TAG, "Datenbankverbindung konnte nicht geöffnet werden");
        }
    }

    public void close() {
        dbHelper.close();
        Log.d(LOG_TAG, "Datenbank mit Hilfe des DbHelpers geschlossen.");
    }

    public Beacon createBeacon(int room_id, double longitude, double latitude, double height, String addresse, int rssi){
        //1.Beacon in Datenbank eingeben
        ContentValues values = new ContentValues();
        values.put(IpsDbHelper.BEACON_ROOM_ID,room_id);
        values.put(IpsDbHelper.BEACON_LONGITUDE,longitude);
        values.put(IpsDbHelper.BEACON_LATITUDE,latitude);
        values.put(IpsDbHelper.BEACON_HEIGHT,height);
        values.put(IpsDbHelper.BEACON_ADDRESSE,addresse);
        values.put(IpsDbHelper.BEACON_RSSI,rssi);
        long insertId = database.insert(IpsDbHelper.Table_Beacon, null, values);

        Cursor cursor;

        //Wenn Objekt noch nicht vorhanden ist
        if(insertId!=-1){
            cursor = database.query(IpsDbHelper.Table_Beacon,BeaconColumns,
                    IpsDbHelper.BEACON_ID+" = "+insertId,null,null,null,null);
        }else{
            cursor = database.query(IpsDbHelper.Table_Beacon,BeaconColumns,
                   null,null,null,null,null);
        }


        cursor.moveToFirst();
        Beacon beacon = cursor2Beacon(cursor);
        cursor.close();

        return beacon;
    }

    private Beacon cursor2Beacon(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(IpsDbHelper.BEACON_ID));
        int room_id = cursor.getInt(cursor.getColumnIndex(IpsDbHelper.BEACON_ROOM_ID));
        double longitude = cursor.getDouble(cursor.getColumnIndex(IpsDbHelper.BEACON_LONGITUDE));
        double latitude = cursor.getDouble(cursor.getColumnIndex(IpsDbHelper.BEACON_LATITUDE));
        double height = cursor.getDouble(cursor.getColumnIndex(IpsDbHelper.BEACON_HEIGHT));
        String addresse = cursor.getString(cursor.getColumnIndex(IpsDbHelper.BEACON_ADDRESSE));
        int rssi = cursor.getInt(cursor.getColumnIndex(IpsDbHelper.BEACON_RSSI));

        Beacon beacon = new Beacon(id, room_id, longitude, latitude,height,addresse,rssi);
        return beacon;
    }

    public List<Beacon> getAllBeacons(){
        List<Beacon> beaconList = new ArrayList<Beacon>();

        Cursor cursor = database.query(IpsDbHelper.Table_Beacon,BeaconColumns,null,null,null,null,null);

        cursor.moveToFirst();
        Beacon beacon;
        while (!cursor.isAfterLast()){
            beacon = cursor2Beacon(cursor);
            beaconList.add(beacon);
            Log.d(LOG_TAG,"ID: "+beacon.getId()+", Adresse: "+beacon.getAddresse());
            cursor.moveToNext();
        }
        cursor.close();

        return beaconList;
    }

}
//http://www.programmierenlernenhq.de/mit-sqlite-app-auf-benutzereingaben-reagieren-in-android/