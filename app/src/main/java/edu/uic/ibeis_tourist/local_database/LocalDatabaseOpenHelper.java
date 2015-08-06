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

        // Brookfield Zoo
        values = new ContentValues();
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_ID, 1);
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_LOCATION_NAME, "Brookfield Zoo");
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LAT, 41.82912);
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LON, -87.84460);
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LAT, 41.83625);
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LON, -87.82775);

        db.insert(LocalDatabaseContract.LocationEntry.TABLE_NAME,
                LocalDatabaseContract.LocationEntry.COLUMN_NAME_ID,
                values);

        // Not Brookfield Zoo
        values = new ContentValues();
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_ID, 2);
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_LOCATION_NAME, "Not Brookfield Zoo");
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LAT, 21.231310);
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LON, -125.631407);
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LAT, 71.554712);
        values.put(LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LON, 49.183050);

        db.insert(LocalDatabaseContract.LocationEntry.TABLE_NAME,
                LocalDatabaseContract.LocationEntry.COLUMN_NAME_ID,
                values);
    }
}
