package edu.uic.ibeis_tourist.ibeis;

import android.content.Context;

import edu.uic.ibeis_tourist.exceptions.MatchNotFoundException;
import edu.uic.ibeis_tourist.model.PictureInfo;

public interface IbeisInterface {

    /**
     * Run an image recognition algorithm to identify the individual in the picture.
     * Display on the view information related to the individual if a match is found.
     * Store the information in a local database.
     * @param pictureInfo PictureInfo object to be updated
     * @param context Context from which the method is called
     * @throws MatchNotFoundException
     */
    void identifyIndividual(PictureInfo pictureInfo, Context context) throws MatchNotFoundException;
}
