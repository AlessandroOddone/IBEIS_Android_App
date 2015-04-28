package edu.uic.ibeis_tourist.interfaces;

import android.content.Context;

import java.util.GregorianCalendar;

import edu.uic.ibeis_tourist.exceptions.MatchNotFoundException;
import edu.uic.ibeis_tourist.model.Location;
import edu.uic.ibeis_tourist.model.Position;

public interface IbeisInterface {

    /**
     * Runs an image recognition algorithm to identify the individual in the picture
     * Displays on the view information related to the individual if a match is found
     * Stores the information in a local database
     * @param fileName Name of the image file
     * @param location Location at which the picture was taken
     * @param position Position at which the picture was taken
     * @param dateTime Datetime at which the picture was taken
     * @param context Context from which the method is called
     * @throws MatchNotFoundException
     */
    public void identifyIndividual(String fileName, Location location, Position position,
                                   GregorianCalendar dateTime, Context context) throws MatchNotFoundException;
}
