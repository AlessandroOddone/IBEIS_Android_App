package edu.uic.ibeis_tourist.interfaces;

import android.content.Context;

import edu.uic.ibeis_tourist.model.PictureInfo;

public interface LocalDatabaseInterface {

    /**
     * Add a PictureInfo object to the local database and updates the view
     * @param pictureInfo
     * @param context
     */
    public void addPicture(PictureInfo pictureInfo, Context context);

    /**
     * Retrieve a PictureInfo object from the database and updates the view
     * @param fileName Name of the image file corresponding to the PictureInfo object
     * @param context
     */
    public void getPicture(String fileName, Context context);

    /**
     * Remove the picture corresponding to the specified filename from the local database
     * @param fileName
     * @param context
     */
    public void removePicture(String fileName, Context context);

    /**
     * Retrieve all PictureInfo objects from the database and updates the view
     * @param context
     */
    public void getAllPictures(Context context);

    /**
     * Retrieve all PictureInfo objects from certain location and updates the view
     * @param locationId
     * @param context
     */
    public void getAllPicturesAtLocation(int locationId, Context context);

    /**
     * Retrieve all the locations from the database and updates the view
     * @param context
     */
    public void getAllLocations(Context context);

    /**
     * Retrieve the location from the database corresponding to the current position, return null
     * if no location is found
     * @param latitude
     * @param longitude
     * @param context
     */
    public void getCurrentLocation(double latitude, double longitude, Context context);
}
