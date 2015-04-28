package edu.uic.ibeis_tourist.local_database;

import android.provider.BaseColumns;

/**
 * Contract class for table containing pictures info in local database
 */
public class LocalDatabaseContract {

    // Increment database version when schema is changed
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "IbeisLocal.db";

    public static final String TEXT_TYPE = " TEXT";
    public static final String INT_TYPE = " INTEGER";
    public static final String REAL_TYPE = " REAL";
    public static final String PRIMARY_KEY = " PRIMARY KEY";
    public static final String COMMA_SEP = ",";

    /**
     * empty constructor to prevent instantiation
     */
    public LocalDatabaseContract() {}

    /**
     * Inner class that defines the tables contents
     */
    public static abstract class PictureInfoEntry implements BaseColumns {
        public static final String TABLE_NAME = "pictures_table";
        public static final String COLUMN_NAME_FILENAME = "filename";
        public static final String COLUMN_NAME_DATETIME = "datetime";
        public static final String COLUMN_NAME_LATITUDE = "lat";
        public static final String COLUMN_NAME_LONGITUDE = "lon";
        public static final String COLUMN_NAME_FACING_DIRECTION = "facing_deg";
        public static final String COLUMN_NAME_INDIVIDUAL_NAME = "ind_name";
        public static final String COLUMN_NAME_INDIVIDUAL_SEX = "ind_sex";
        public static final String COLUMN_NAME_INDIVIDUAL_SPECIES = "ind_species";
        public static final String COLUMN_NAME_LOCATION_ID = "location_id";
    }

    public static abstract class LocationEntry implements BaseColumns {
        public static final String TABLE_NAME = "locations_table";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_LOCATION_NAME = "name";
        public static final String COLUMN_NAME_SW_BOUND_LAT = "sw_lat";
        public static final String COLUMN_NAME_SW_BOUND_LON = "sw_lon";
        public static final String COLUMN_NAME_NE_BOUND_LAT = "ne_lat";
        public static final String COLUMN_NAME_NE_BOUND_LON = "ne_lon";
    }

    public static final String SQL_CREATE_PICTURES_TABLE =
            "CREATE TABLE " + PictureInfoEntry.TABLE_NAME + " (" +
                    PictureInfoEntry.COLUMN_NAME_FILENAME + TEXT_TYPE + PRIMARY_KEY + COMMA_SEP +
                    PictureInfoEntry.COLUMN_NAME_DATETIME + TEXT_TYPE + COMMA_SEP +
                    PictureInfoEntry.COLUMN_NAME_LATITUDE + TEXT_TYPE + COMMA_SEP +
                    PictureInfoEntry.COLUMN_NAME_LONGITUDE + TEXT_TYPE + COMMA_SEP +
                    PictureInfoEntry.COLUMN_NAME_FACING_DIRECTION + TEXT_TYPE + COMMA_SEP +
                    PictureInfoEntry.COLUMN_NAME_INDIVIDUAL_NAME + TEXT_TYPE + COMMA_SEP +
                    PictureInfoEntry.COLUMN_NAME_INDIVIDUAL_SEX + TEXT_TYPE + COMMA_SEP +
                    PictureInfoEntry.COLUMN_NAME_INDIVIDUAL_SPECIES + TEXT_TYPE + COMMA_SEP +
                    PictureInfoEntry.COLUMN_NAME_LOCATION_ID + INT_TYPE +
                    " )";

    public static final String SQL_DELETE_PICTURES_TABLE =
            "DROP TABLE IF EXISTS " + PictureInfoEntry.TABLE_NAME;

    public static final String SQL_CREATE_LOCATIONS_TABLE =
            "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                    LocationEntry.COLUMN_NAME_ID + INT_TYPE + PRIMARY_KEY + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_LOCATION_NAME + TEXT_TYPE + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_SW_BOUND_LAT + REAL_TYPE + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_SW_BOUND_LON + REAL_TYPE + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_NE_BOUND_LAT + REAL_TYPE + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_NE_BOUND_LON + REAL_TYPE +
                    " )";

    public static final String SQL_DELETE_LOCATIONS_TABLE =
            "DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME;

}
