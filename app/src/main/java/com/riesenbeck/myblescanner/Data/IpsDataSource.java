package com.riesenbeck.myblescanner.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by michael on 12.10.2016.
 */

public class IpsDataSource {
    private static final String LOG_TAG = IpsDataSource.class.getSimpleName();

    private SQLiteDatabase database;
    private IpsDbHelper dbHelper;


    public IpsDataSource(Context context) {
        Log.d(LOG_TAG, "Unsere DataSource erzeugt jetzt den dbHelper.");
        dbHelper = new IpsDbHelper(context);
    }
}
//http://www.programmierenlernenhq.de/sqlite-datenbank-in-android-app-integrieren/