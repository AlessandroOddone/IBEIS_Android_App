package edu.uic.ibeis_tourist.local_database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalDatabaseOpenHelper extends SQLiteOpenHelper {

    public LocalDatabaseOpenHelper(Context context) {
        super(context, LocalDatabaseContract.DATABASE_NAME, null, LocalDatabaseContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LocalDatabaseContract.SQL_CREATE_PICTURES_TABLE);
        db.execSQL(LocalDatabaseContract.SQL_CREATE_LOCATIONS_TABLE);
        // Insert values in Location table
        addLocations(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Upgrade policy: discard the data and start over
        db.execSQL(LocalDatabaseContract.SQL_DELETE_PICTURES_TABLE);
        db.execSQL(LocalDatabaseContract.SQL_DELETE_LOCATIONS_TABLE);
        onCreate(db);
    }

    // TODO download locations when app starts
    public void addLocations(SQLiteDatabase db) {
        ContentValues values;

        // UIC East Campus
        values = new ContentValues();
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_ID, 1);
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_LOCATION_NAME, "UIC East Campus");
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LAT, 41.8591);
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LON, -87.6663);
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LAT, 41.8734);
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LON, -87.6326);

        db.insert(LocalDatabaseContract.LocationEntry.TABLE_NAME,
                  LocalDatabaseContract.LocationEntry.COLUMN_NAME_ID,
                  values);

        // SSR
        values = new ContentValues();
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_ID, 2);
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_LOCATION_NAME, "SSR");
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LAT, 41.86931);
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LON, -87.67987);
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LAT, 41.87287);
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LON, -87.67145);

        db.insert(LocalDatabaseContract.LocationEntry.TABLE_NAME,
                LocalDatabaseContract.LocationEntry.COLUMN_NAME_ID,
                values);

        // Brookfield Zoo
        values = new ContentValues();
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_ID, 3);
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_LOCATION_NAME, "Brookfield Zoo");
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LAT, 41.82912);
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LON, -87.84460);
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LAT, 41.83625);
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LON, -87.82775);

        db.insert(LocalDatabaseContract.LocationEntry.TABLE_NAME,
                LocalDatabaseContract.LocationEntry.COLUMN_NAME_ID,
                values);

    }
}
