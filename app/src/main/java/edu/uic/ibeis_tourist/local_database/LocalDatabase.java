package edu.uic.ibeis_tourist.local_database;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import edu.uic.ibeis_tourist.MainActivity;
import edu.uic.ibeis_tourist.PictureDetailActivity;
import edu.uic.ibeis_tourist.MyPicturesActivity;
import edu.uic.ibeis_tourist.exceptions.InvalidSexException;
import edu.uic.ibeis_tourist.exceptions.InvalidSpeciesException;
import edu.uic.ibeis_tourist.interfaces.LocalDatabaseInterface;
import edu.uic.ibeis_tourist.model.Location;
import edu.uic.ibeis_tourist.model.LocationBounds;
import edu.uic.ibeis_tourist.model.PictureInfo;
import edu.uic.ibeis_tourist.model.Position;
import edu.uic.ibeis_tourist.model.SexEnum;
import edu.uic.ibeis_tourist.model.SpeciesEnum;
import edu.uic.ibeis_tourist.utils.DateTimeUtils;
import edu.uic.ibeis_tourist.utils.LocationUtils;

public class LocalDatabase implements LocalDatabaseInterface {

    private static final String NOT_AVAILABLE = "N/A";

    private LocalDatabaseOpenHelper dbHelper;

    @Override
    public void addPicture(PictureInfo pictureInfo, Context context) {
        new AddPictureAsyncTask(pictureInfo, context).execute();
    }

    @Override
    public void getPicture(String fileName, Context context) {
        new GetPictureAsyncTask(fileName, context).execute();
    }

    @Override
    public void removePicture(String fileName, Context context) {
        new RemovePictureAsyncTask(fileName, context).execute();
    }

    @Override
    public void getAllPictures(Context context) {
        new GetAllPicturesAsyncTask(context).execute();
    }

    @Override
    public void getAllPicturesAtLocation(int locationId, Context context) {
        new GetAllPicturesAtLocationAsyncTask(locationId, context).execute();
    }

    @Override
    public void getAllLocations(Context context) {
        new GetAllLocationsAsyncTask(context).execute();
    }

    @Override
    public void getCurrentLocation(double latitude, double longitude, Context context) {
        new GetCurrentLocationAsyncTask(latitude, longitude, context).execute();
    }

    /**
     * Get Location object from location ID
     * @param locationId
     * @return
     */
    public Location getLocation(int locationId, final Context context) {
        dbHelper = new LocalDatabaseOpenHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String projection[] = {
                LocalDatabaseContract.LocationEntry.COLUMN_NAME_LOCATION_NAME,
                LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LAT,
                LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LON,
                LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LAT,
                LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LON
        };

        String selection = LocalDatabaseContract.LocationEntry.COLUMN_NAME_ID + " = ?";

        String[] selectionArgs = {String.valueOf(locationId)};

        Cursor cursor = db.query(LocalDatabaseContract.LocationEntry.TABLE_NAME, projection,
                selection, selectionArgs, null, null, null);

        cursor.moveToFirst();
        String locationName = cursor.getString(cursor.getColumnIndex
                (LocalDatabaseContract.LocationEntry.COLUMN_NAME_LOCATION_NAME));
        double swBoundLat = cursor.getDouble(cursor.getColumnIndex
                (LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LAT));
        double swBoundLon = cursor.getDouble(cursor.getColumnIndex
                (LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LON));
        double neBoundLat = cursor.getDouble(cursor.getColumnIndex
                (LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LAT));
        double neBoundLon = cursor.getDouble(cursor.getColumnIndex
                (LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LON));

        cursor.close();
        db.close();

        Location location = new Location();
        location.setId(locationId);
        location.setName(locationName);
        location.setBounds(new LocationBounds(new Position(swBoundLat, swBoundLon), new Position(neBoundLat, neBoundLon)));

        return location;
    }


    // AsyncTask classes implementation

    // Add Picture
    private class AddPictureAsyncTask extends AsyncTask<Void, Void, Void> {

        private Context mContext;
        private PictureInfo mPictureInfo;

        private AddPictureAsyncTask(PictureInfo pictureInfo, Context context) {
            mContext = context;
            mPictureInfo = pictureInfo;
        }

        @Override
        protected Void doInBackground(Void... params) {
            System.out.println("LocalDatabase: AddPictureAsyncTask");

            dbHelper = new LocalDatabaseOpenHelper(mContext);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();

            values.put(LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_FILENAME, mPictureInfo.getFileName());

            Position position = mPictureInfo.getPosition();
            values.put(LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_LATITUDE, position != null ? String.valueOf(position.getLatitude()) : NOT_AVAILABLE);
            values.put(LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_LONGITUDE, position != null ? String.valueOf(position.getLongitude()) : NOT_AVAILABLE);
            values.put(LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_FACING_DIRECTION, position != null ? String.valueOf(position.getFacingDirection()) : NOT_AVAILABLE);

            values.put(LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_DATETIME,
                       DateTimeUtils.calendarToString(mPictureInfo.getDateTime(), DateTimeUtils.DateFormat.DATABASE));

            String name = mPictureInfo.getIndividualName();
            values.put(LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_INDIVIDUAL_NAME, name != null ? name : NOT_AVAILABLE);

            values.put(LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_LOCATION_ID,
                    mPictureInfo.getLocation().getId());

            SpeciesEnum species = mPictureInfo.getIndividualSpecies();
            values.put(LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_INDIVIDUAL_SPECIES,
                    species != null ? species.asString() : NOT_AVAILABLE);

            SexEnum sex = mPictureInfo.getIndividualSex();
            values.put(LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_INDIVIDUAL_SEX,
                    sex != null ? sex.asString() : NOT_AVAILABLE);

            db.insert(LocalDatabaseContract.PictureInfoEntry.TABLE_NAME,
                      LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_FILENAME, values);

            db.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void voidParam) {
            if (mContext instanceof Activity) {
                displayPictureInfo((Activity) mContext);
            }
        }

        private void displayPictureInfo (Activity activity) {

            System.out.println("AddPictureAsyncTask: displayPictureInfo");

            String activityName = activity.getClass().getSimpleName();

            switch (activityName) {
                case "PictureDetailActivity":
                    PictureDetailActivity PictureDetailActivity =
                            (PictureDetailActivity) activity;
                    PictureDetailActivity.displayPictureInfo(mPictureInfo);
            }
        }
    }

    // Get Picture
    private class GetPictureAsyncTask extends AsyncTask<Void, Void, PictureInfo> {

        // TODO if picture not found in folder, delete row

        private Context mContext;
        private String mFileName;

        private GetPictureAsyncTask(String fileName, Context context) {
            mContext = context;
            mFileName = fileName;
        }

        @Override
        protected PictureInfo doInBackground(Void... params) {
            // TODO implement
            return null;
        }

        @Override
        protected void onPostExecute(PictureInfo pictureInfo) {
            // TODO implement
        }
    }

    // Remove Picture
    private class RemovePictureAsyncTask extends AsyncTask<Void, Void, Void> {

        // TODO if picture not found in folder, delete row

        private Context mContext;
        private String mFileName;

        private RemovePictureAsyncTask(String fileName, Context context) {
            mContext = context;
            mFileName = fileName;
        }

        @Override
        protected Void doInBackground(Void... params) {
            System.out.println("LocalDatabase: RemovePicture AsyncTask");

            dbHelper = new LocalDatabaseOpenHelper(mContext);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            String selection = LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_FILENAME;
            String[] selectionArgs = { mFileName };
            db.delete(LocalDatabaseContract.PictureInfoEntry.TABLE_NAME, selection, selectionArgs);

            db.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void voidParam) {
            // TODO implement
        }
    }


    //Get All Pictures
    private class GetAllPicturesAsyncTask extends AsyncTask<Void, Void, List<PictureInfo>> {

        // TODO if picture not found in folder, delete row

        private Context mContext;

        private GetAllPicturesAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected List<PictureInfo> doInBackground(Void... params) {
            System.out.println("LocalDatabase: GetAllPictures AsyncTask");

            dbHelper = new LocalDatabaseOpenHelper(mContext);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String projection[] = {
                    LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_FILENAME,
                    LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_INDIVIDUAL_NAME,
                    LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_INDIVIDUAL_SEX,
                    LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_INDIVIDUAL_SPECIES,
                    LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_LATITUDE,
                    LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_LONGITUDE,
                    LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_FACING_DIRECTION,
                    LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_DATETIME,
                    LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_LOCATION_ID
            };

            String sortOrder = LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_DATETIME + " DESC";

            Cursor cursor = db.query(LocalDatabaseContract.PictureInfoEntry.TABLE_NAME, projection,
                    null, null, null, null, sortOrder);

            List<PictureInfo> pictureInfoList = new ArrayList<>();

            while (cursor.moveToNext())
            {
                PictureInfo pictureInfo = new PictureInfo();

                pictureInfo.setFileName(cursor.getString(cursor.getColumnIndex
                        (LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_FILENAME)));

                String individualName = cursor.getString(cursor.getColumnIndex
                        (LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_INDIVIDUAL_NAME));
                if(individualName.equals(NOT_AVAILABLE)) { // check if name is not available
                    // set individual name to null
                    pictureInfo.setIndividualName(null);
                }
                else {
                    pictureInfo.setIndividualName(individualName);
                }

                try {
                    String sex = cursor.getString(cursor.getColumnIndex
                            (LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_INDIVIDUAL_SEX));
                    if(sex.equals(NOT_AVAILABLE)) {
                        pictureInfo.setIndividualSex(SexEnum.UNKNOWN);
                    }
                    else {
                        pictureInfo.setIndividualSex(SexEnum.fromString(sex));
                    }

                    String species = cursor.getString(cursor.getColumnIndex
                            (LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_INDIVIDUAL_SPECIES));
                    if(species.equals(NOT_AVAILABLE)) {
                        pictureInfo.setIndividualSpecies(SpeciesEnum.UNKNOWN);
                    }
                    else {
                        pictureInfo.setIndividualSpecies(SpeciesEnum.fromString(species));
                    }

                } catch (InvalidSexException | InvalidSpeciesException e) {
                    pictureInfo.setIndividualSex(SexEnum.UNKNOWN);
                    e.printStackTrace();
                }

                try {
                    double lat = Double.parseDouble(cursor.getString(cursor.getColumnIndex
                                    (LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_LATITUDE)));
                    double lon = Double.parseDouble(cursor.getString(cursor.getColumnIndex
                                    (LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_LONGITUDE)));
                    float facing = Float.parseFloat(cursor.getString(cursor.getColumnIndex
                            (LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_FACING_DIRECTION)));
                    pictureInfo.setPosition(new Position(lat, lon, facing));
                } catch (NumberFormatException e) {
                    pictureInfo.setPosition(null);
                    e.printStackTrace();
                }

                pictureInfo.setDateTime((GregorianCalendar)DateTimeUtils.stringToCalendar(cursor.getString
                        (cursor.getColumnIndex(LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_DATETIME)), DateTimeUtils.DateFormat.DATABASE));

                pictureInfo.setLocation(getLocation(cursor.getInt(cursor.getColumnIndex
                        (LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_LOCATION_ID)), mContext));

                pictureInfoList.add(pictureInfo);
            }

            cursor.close();
            db.close();

            return pictureInfoList;
        }

        @Override
        protected void onPostExecute(List<PictureInfo> pictureInfoList) {
            if (mContext instanceof Activity) {
                displayPictureInfoList(pictureInfoList, (Activity) mContext);
            }
        }

        private void displayPictureInfoList (final List<PictureInfo> pictureInfoList, final Activity activity) {

            String activityName = activity.getClass().getSimpleName();

            switch (activityName) {
                case "MyPicturesActivity":
                    MyPicturesActivity myPicturesActivity = (MyPicturesActivity) activity;
                    myPicturesActivity.displayPictureInfoList(pictureInfoList);
            }
        }
    }

    //Get All Pictures At Location
    private class GetAllPicturesAtLocationAsyncTask extends AsyncTask<Void, Void, List<PictureInfo>> {

        // TODO if picture not found in folder, delete row

        private int mLocationId;
        private Context mContext;

        private GetAllPicturesAtLocationAsyncTask(int locationId, Context context) {
            mLocationId = locationId;
            mContext = context;
        }

        @Override
        protected List<PictureInfo> doInBackground(Void... params) {
            System.out.println("LocalDatabase: GetAllPicturesAtLocation AsyncTask");

            dbHelper = new LocalDatabaseOpenHelper(mContext);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String projection[] = {
                    LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_FILENAME,
                    LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_INDIVIDUAL_NAME,
                    LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_INDIVIDUAL_SEX,
                    LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_INDIVIDUAL_SPECIES,
                    LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_LATITUDE,
                    LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_LONGITUDE,
                    LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_FACING_DIRECTION,
                    LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_DATETIME,
                    LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_LOCATION_ID
            };

            String selection = LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_LOCATION_ID + " = ?";

            String[] selectionArgs = { String.valueOf(mLocationId) };

            String sortOrder = LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_DATETIME + " DESC";

            Cursor cursor = db.query(LocalDatabaseContract.PictureInfoEntry.TABLE_NAME, projection,
                    selection, selectionArgs, null, null, sortOrder);

            List<PictureInfo> pictureInfoList = new ArrayList<>();

            while (cursor.moveToNext())
            {
                PictureInfo pictureInfo = new PictureInfo();

                pictureInfo.setFileName(cursor.getString(cursor.getColumnIndex
                        (LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_FILENAME)));

                String individualName = cursor.getString(cursor.getColumnIndex
                        (LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_INDIVIDUAL_NAME));
                if(individualName.equals(NOT_AVAILABLE)) { // check if name is not available
                    // set individual name to null
                    pictureInfo.setIndividualName(null);
                }
                else {
                    pictureInfo.setIndividualName(individualName);
                }

                try {
                    String sex = cursor.getString(cursor.getColumnIndex
                            (LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_INDIVIDUAL_SEX));
                    if(sex.equals(NOT_AVAILABLE)) {
                        pictureInfo.setIndividualSex(SexEnum.UNKNOWN);
                    }
                    else {
                        pictureInfo.setIndividualSex(SexEnum.fromString(sex));
                    }

                    String species = cursor.getString(cursor.getColumnIndex
                            (LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_INDIVIDUAL_SPECIES));
                    if(species.equals(NOT_AVAILABLE)) {
                        pictureInfo.setIndividualSpecies(SpeciesEnum.UNKNOWN);
                    }
                    else {
                        pictureInfo.setIndividualSpecies(SpeciesEnum.fromString(species));
                    }

                } catch (InvalidSexException | InvalidSpeciesException e) {
                    pictureInfo.setIndividualSex(SexEnum.UNKNOWN);
                    e.printStackTrace();
                }

                try {
                    double lat = Double.parseDouble(cursor.getString(cursor.getColumnIndex
                            (LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_LATITUDE)));
                    double lon = Double.parseDouble(cursor.getString(cursor.getColumnIndex
                            (LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_LONGITUDE)));
                    float facing = Float.parseFloat(cursor.getString(cursor.getColumnIndex
                            (LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_FACING_DIRECTION)));
                    pictureInfo.setPosition(new Position(lat, lon, facing));
                } catch (NumberFormatException e) {
                    pictureInfo.setPosition(null);
                    e.printStackTrace();
                }

                pictureInfo.setDateTime((GregorianCalendar)DateTimeUtils.stringToCalendar(cursor.getString
                        (cursor.getColumnIndex(LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_DATETIME)), DateTimeUtils.DateFormat.DATABASE));

                pictureInfo.setLocation(getLocation(cursor.getInt(cursor.getColumnIndex
                        (LocalDatabaseContract.PictureInfoEntry.COLUMN_NAME_LOCATION_ID)), mContext));

                pictureInfoList.add(pictureInfo);
            }

            cursor.close();
            db.close();

            return pictureInfoList;
        }


        @Override
        protected void onPostExecute(List<PictureInfo> pictureInfoList) {
            if (mContext instanceof Activity) {
                displayPictureInfoList(pictureInfoList, (Activity) mContext);
            }
        }

        private void displayPictureInfoList (final List<PictureInfo> pictureInfoList, final Activity activity) {

            String activityName = activity.getClass().getSimpleName();

            switch (activityName) {
                case "MyPicturesActivity":
                    MyPicturesActivity myPicturesActivity = (MyPicturesActivity) activity;
                    myPicturesActivity.displayPictureInfoList(pictureInfoList);
            }
        }
    }


    // Get All Locations
    private class GetAllLocationsAsyncTask extends AsyncTask<Void, Void, List<Location>> {

        private Context mContext;

        private GetAllLocationsAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected List<Location> doInBackground(Void... params) {
            System.out.println("LocalDatabase: GetAllLocations AsyncTask");

            dbHelper = new LocalDatabaseOpenHelper(mContext);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String projection[] = {
                    LocalDatabaseContract.LocationEntry.COLUMN_NAME_ID,
                    LocalDatabaseContract.LocationEntry.COLUMN_NAME_LOCATION_NAME,
                    LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LAT,
                    LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LON,
                    LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LAT,
                    LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LON
            };

            String sortOrder = LocalDatabaseContract.LocationEntry.COLUMN_NAME_LOCATION_NAME + " ASC";

            Cursor cursor = db.query(LocalDatabaseContract.LocationEntry.TABLE_NAME, projection,
                    null, null, null, null, sortOrder);

            List<Location> locationList = new ArrayList<>();

            while (cursor.moveToNext()) {
                Location location = new Location();

                location.setId(cursor.getInt(cursor.getColumnIndex
                        (LocalDatabaseContract.LocationEntry.COLUMN_NAME_ID)));

                location.setName(cursor.getString(cursor.getColumnIndex
                        (LocalDatabaseContract.LocationEntry.COLUMN_NAME_LOCATION_NAME)));

                double swBoundLat = cursor.getDouble(cursor.getColumnIndex
                        (LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LAT));
                double swBoundLon = cursor.getDouble(cursor.getColumnIndex
                        (LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LON));
                double neBoundLat = cursor.getDouble(cursor.getColumnIndex
                        (LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LAT));
                double neBoundLon = cursor.getDouble(cursor.getColumnIndex
                        (LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LON));

                location.setBounds(new LocationBounds(new Position(swBoundLat, swBoundLon),
                        new Position(neBoundLat, neBoundLon)));

                locationList.add(location);
            }

            cursor.close();
            db.close();

            return locationList;
        }

        @Override
        protected void onPostExecute(List<Location> locationList) {
            if (mContext instanceof Activity) {
                displayLocationList(locationList, (Activity) mContext);
            }
        }

        private void displayLocationList (final List<Location> locationList, final Activity activity) {

            String activityName = activity.getClass().getSimpleName();

            switch (activityName) {
            }
        }
    }

    // Get Current Location
    private class GetCurrentLocationAsyncTask extends AsyncTask<Void, Void, Location> {

        private Context mContext;
        private double mLatitude;
        private double mLongitude;

        private GetCurrentLocationAsyncTask(double latitude, double longitude, Context context) {
            mContext = context;
            mLatitude = latitude;
            mLongitude = longitude;
        }

        @Override
        protected Location doInBackground(Void... params) {
            System.out.println("LocalDatabase: GetCurrentLocation AsyncTask " +
                    "(lat = " + mLatitude + ", lon = " + mLongitude + ")");

            dbHelper = new LocalDatabaseOpenHelper(mContext);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String projection[] = {
                    LocalDatabaseContract.LocationEntry.COLUMN_NAME_ID,
                    LocalDatabaseContract.LocationEntry.COLUMN_NAME_LOCATION_NAME,
                    LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LAT,
                    LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LON,
                    LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LAT,
                    LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LON
            };

            Cursor cursor = db.query(LocalDatabaseContract.LocationEntry.TABLE_NAME, projection,
                    null, null, null, null, null);

            List<Location> locationList = new ArrayList<>();

            while (cursor.moveToNext()) {
                Location location = new Location();

                location.setId(cursor.getInt(cursor.getColumnIndex
                        (LocalDatabaseContract.LocationEntry.COLUMN_NAME_ID)));

                location.setName(cursor.getString(cursor.getColumnIndex
                        (LocalDatabaseContract.LocationEntry.COLUMN_NAME_LOCATION_NAME)));

                double swBoundLat = cursor.getDouble(cursor.getColumnIndex
                        (LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LAT));
                double swBoundLon = cursor.getDouble(cursor.getColumnIndex
                        (LocalDatabaseContract.LocationEntry.COLUMN_NAME_SW_BOUND_LON));
                double neBoundLat = cursor.getDouble(cursor.getColumnIndex
                        (LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LAT));
                double neBoundLon = cursor.getDouble(cursor.getColumnIndex
                        (LocalDatabaseContract.LocationEntry.COLUMN_NAME_NE_BOUND_LON));

                location.setBounds(new LocationBounds(new Position(swBoundLat, swBoundLon),
                        new Position(neBoundLat, neBoundLon)));

                locationList.add(location);
            }

            Location currentLocation = null;

            for (Location location : locationList) {
                if (LocationUtils.isPositionAtLocation(mLatitude, mLongitude, location)) {
                    currentLocation = location;
                    break;
                }
            }

            cursor.close();
            db.close();

            return currentLocation;
        }

        @Override
        protected void onPostExecute(Location currentLocation) {
            if (mContext instanceof Activity) {
                currentLocationDetected(currentLocation, (Activity) mContext);
            }
        }

        private void currentLocationDetected (final Location currentLocation, final Activity activity) {
            String activityName = activity.getClass().getSimpleName();

            switch (activityName) {
                case "MainActivity":
                    MainActivity mainActivity = (MainActivity) activity;
                    mainActivity.currentLocationDetected(currentLocation);
            }
        }
    }
}


