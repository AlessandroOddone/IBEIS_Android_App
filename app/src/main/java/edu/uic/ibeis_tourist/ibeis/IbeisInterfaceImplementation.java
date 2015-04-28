package edu.uic.ibeis_tourist.ibeis;

import android.content.Context;
import android.os.AsyncTask;

import java.util.GregorianCalendar;

import edu.uic.ibeis_tourist.exceptions.MatchNotFoundException;
import edu.uic.ibeis_tourist.interfaces.IbeisInterface;
import edu.uic.ibeis_tourist.local_database.LocalDatabase;
import edu.uic.ibeis_tourist.model.Location;
import edu.uic.ibeis_tourist.model.PictureInfo;
import edu.uic.ibeis_tourist.model.Position;
import edu.uic.ibeis_tourist.model.Sex;
import edu.uic.ibeis_tourist.model.Species;

public class IbeisInterfaceImplementation implements IbeisInterface {

    @Override
    public void identifyIndividual(String fileName, Location location, Position position,
                                   GregorianCalendar dateTime, Context context) throws MatchNotFoundException {

        System.out.println("IbeisInterfaceImplementation: identify individual");
        new IdentifyIndividualAsyncTask(fileName, position, dateTime, location, context).execute();

    }

    // AsyncTask classes implementation

    // TODO: this is a dummy implementation
    private class IdentifyIndividualAsyncTask extends AsyncTask<Void, Void, PictureInfo> {

        private String mFileName;
        private Location mLocation;
        private Position mPosition;
        private GregorianCalendar mDateTime;
        private Context mContext;

        private IdentifyIndividualAsyncTask(String fileName, Position position, GregorianCalendar dateTime,
                                            Location location, Context context) {
            mFileName = fileName;
            mLocation = location;
            mPosition = position;
            mDateTime = dateTime;
            mContext = context;
        }

        @Override
        protected PictureInfo doInBackground(Void... params) {
            System.out.println("IbeisInterfaceImplementation: IdentifyIndividual AsyncTask");

            PictureInfo pictureInfo = new PictureInfo();

            pictureInfo.setFileName(mFileName);
            pictureInfo.setPosition(mPosition);
            pictureInfo.setDateTime(mDateTime);
            pictureInfo.setIndividualName(null);
            pictureInfo.setIndividualSex(Sex.UNKNOWN);
            pictureInfo.setIndividualSpecies(Species.GIRAFFE);
            pictureInfo.setLocation(mLocation);

            return pictureInfo;
        }

        @Override
        protected void onPostExecute(PictureInfo pictureInfo) {
            // TODO Implement
            super.onPostExecute(pictureInfo);

            System.out.println("IdentifyIndividualAsyncTask: onPostExecute");
            LocalDatabase localDb = new LocalDatabase();
            localDb.addPicture(pictureInfo, mContext);
        }
    }
}
