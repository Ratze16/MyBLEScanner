package com.riesenbeck.myblescanner.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Matrix;
import android.util.Log;

/**
 * Created by michael on 12.10.2016.
 */


public class IpsDbHelper extends SQLiteOpenHelper {
    protected static final String DB_NAME = "IPS.db";
    protected static final int DB_VERSION = 1;
    protected static String Table_Beacon = "Beacon", Table_Room, Table_RoomPerson, Table_Person;
    protected static String BEACON_ID = "_id", BEACON_ROOM_ID = "room_id",BEACON_LONGITUDE = "longitude",
            BEACON_LATITUDE = "latitude", BEACON_HEIGHT = "height", BEACON_ADDRESSE = "addresse",
            BEACON_RSSI = "rssi";
    protected static String ROOM_ID = "room_id", ROOM_NUMBER = "roomnumber", ROOM_DESCRIPTION = "description";
    protected static String ROOMPERSON_ROOM_ID = "room_id", ROOMPERSON_PERSON_ID = "peson_id";
    protected static String PERSON_ID = "_id", PERSON_LASTNAME = "last_name", PERSON_FIRSTNAME = "first_name",
            PERSON_DESCRIPTION = "description";

    protected static final String CREATE_TABLE_BEACON =
            "CREATE TABLE "+Table_Beacon+" (" +
                    "`"+BEACON_ID+"` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "`"+BEACON_ROOM_ID+"` INTEGER, " +
                    "`"+BEACON_LONGITUDE+"` REAL, " +
                    "`"+BEACON_LATITUDE+"` REAL, " +
                    "`"+BEACON_HEIGHT+"` REAL, " +
                    "`"+BEACON_ADDRESSE+"` TEXT UNIQUE, " +
                    "`"+BEACON_RSSI+"` INTEGER " +
                    ");";
    protected static final String UPDATE_TABLE_BEACON =
            "UPDATE TABLE "+Table_Beacon+" (" +
                    "`"+BEACON_ID+"` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "`"+BEACON_ROOM_ID+"` INTEGER, " +
                    "`"+BEACON_LONGITUDE+"` REAL, " +
                    "`"+BEACON_LATITUDE+"` REAL, " +
                    "`"+BEACON_HEIGHT+"` REAL, " +
                    "`"+BEACON_ADDRESSE+"` TEXT UNIQUE, " +
                    "`"+BEACON_RSSI+"` INTEGER " +
                    ");";
    protected static final String CREATE_TABLE_ROOM =
            "CREATE TABLE "+Table_Room+" (\n" +
                    "\t`"+ROOM_ID+"`\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                    "\t`"+ROOM_NUMBER+"`\tTEXT\n" +
                    "\t`"+ROOM_DESCRIPTION+"`\tTEXT\n" +
                    ");";
    protected static final String CREATE_TABLE_ROOMPERSON =
            "CREATE TABLE "+Table_RoomPerson+" (\n" +
                    "\t`"+ROOMPERSON_ROOM_ID+"`\tINTEGER NOT NULL,\n" +
                    "\t`"+ROOMPERSON_PERSON_ID+"`\tINTEGER NOT NULL,\n" +
                    "\tPRIMARY KEY(`room_id`,`person_id`)\n" +
                    ");";
    protected static final String CREATE_TABLE_PERSON =
            "CREATE TABLE "+Table_Person+" (\n" +
                    "\t`"+PERSON_ID+"`\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                    "\t`"+PERSON_LASTNAME+"`\tTEXT,\n" +
                    "\t`"+PERSON_FIRSTNAME+"`\tTEXT,\n" +
                    "\t`"+PERSON_DESCRIPTION+"`\tTEXT\n" +
                    ");";

    private static final String LOG_TAG = IpsDbHelper.class.getSimpleName();

    public IpsDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public IpsDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(LOG_TAG, "DbHelper hat die Datenbank: " + getDatabaseName() + " erzeugt.");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            //Tabellen anlegen
            Log.d(LOG_TAG, "Die Tabelle wird mit SQL-Befehl: "+Table_Beacon+" angelegt.");
            db.execSQL(Table_Beacon);
            db.execSQL(Table_Person);
            db.execSQL(Table_Room);
            db.execSQL(Table_RoomPerson);

        }catch (SQLException e){
            Log.e(LOG_TAG, "Fehler beim Anlegen der Tabelle: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
