package edu.uic.ibeis_tourist.ibeis;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;

import edu.uic.ibeis_java_api.api.Ibeis;
import edu.uic.ibeis_java_api.api.IbeisAnnotation;
import edu.uic.ibeis_java_api.api.IbeisImage;
import edu.uic.ibeis_java_api.exceptions.UnsuccessfulHttpRequestException;
import edu.uic.ibeis_java_api.exceptions.UnsupportedImageFileTypeException;
import edu.uic.ibeis_java_api.values.Species;
import edu.uic.ibeis_tourist.exceptions.MatchNotFoundException;
import edu.uic.ibeis_tourist.local_database.LocalDatabase;
import edu.uic.ibeis_tourist.model.Location;
import edu.uic.ibeis_tourist.model.PictureInfo;
import edu.uic.ibeis_tourist.model.Position;
import edu.uic.ibeis_tourist.model.Sex;


public class IbeisController implements edu.uic.ibeis_tourist.interfaces.IbeisInterface {

    @Override
    public void identifyIndividual(String fileName, Location location, Position position,
                                   GregorianCalendar dateTime, Context context) throws MatchNotFoundException {

        System.out.println("IbeisInterfaceImplementation: identify individual");
        new IdentifyIndividualAsyncTask(fileName, position, dateTime, location, context).execute();
    }

    // AsyncTask classes implementation

    private class IdentifyIndividualAsyncTask extends AsyncTask<Void, Void, PictureInfo> {

        private Ibeis ibeis;

        private String mFileName;
        private Location mLocation;
        private Position mPosition;
        private GregorianCalendar mDateTime;
        private Context mContext;

        private IdentifyIndividualAsyncTask(String fileName, Position position, GregorianCalendar dateTime,
                                            Location location, Context context) {

            ibeis = new Ibeis();

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

            IbeisImage uploadedImage;
            List<IbeisAnnotation> imageAnnotations = null;

            try {
                uploadedImage = ibeis.uploadImage(new File(mFileName));
                imageAnnotations = ibeis.runAnimalDetection(uploadedImage, Species.GIRAFFE);
            } catch (UnsupportedImageFileTypeException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UnsuccessfulHttpRequestException e) {
                e.printStackTrace();
            }

            pictureInfo.setFileName(mFileName);
            pictureInfo.setLocation(mLocation);
            pictureInfo.setPosition(mPosition);
            pictureInfo.setDateTime(mDateTime);
            pictureInfo.setIndividualName(null);
            pictureInfo.setIndividualSpecies(edu.uic.ibeis_tourist.model.Species.UNKNOWN);
            pictureInfo.setIndividualSex(Sex.UNKNOWN);


            if(imageAnnotations  != null && imageAnnotations.size() > 0) {
                pictureInfo.setIndividualSpecies(edu.uic.ibeis_tourist.model.Species.GIRAFFE);
            }

            return pictureInfo;
        }

        @Override
        protected void onPostExecute(PictureInfo pictureInfo) {
            super.onPostExecute(pictureInfo);

            System.out.println("IdentifyIndividualAsyncTask: onPostExecute");
            LocalDatabase localDb = new LocalDatabase();
            localDb.addPicture(pictureInfo, mContext);
        }
    }
}
