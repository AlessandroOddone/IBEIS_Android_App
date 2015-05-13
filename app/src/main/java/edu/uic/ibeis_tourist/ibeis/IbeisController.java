package edu.uic.ibeis_tourist.ibeis;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
import edu.uic.ibeis_tourist.utils.ImageUtils;


public class IbeisController implements edu.uic.ibeis_tourist.interfaces.IbeisInterface {

    @Override
    public void identifyIndividual(String fileName, Location location, Position position,
                                   GregorianCalendar dateTime, Context context) throws MatchNotFoundException {

        System.out.println("IbeisController: Identify Individual");
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

            System.out.println("IdentifyIndividualAsyncTask");

            ibeis = new Ibeis();

            mFileName = fileName;
            mLocation = location;
            mPosition = position;
            mDateTime = dateTime;
            mContext = context;
        }

        // TODO temporary implementation
        @Override
        protected PictureInfo doInBackground(Void... params) {
            System.out.println("IbeisInterfaceImplementation: IdentifyIndividual AsyncTask");

            PictureInfo pictureInfo = new PictureInfo();
            pictureInfo.setFileName(mFileName);
            pictureInfo.setLocation(mLocation);
            pictureInfo.setPosition(mPosition);
            pictureInfo.setDateTime(mDateTime);
            pictureInfo.setIndividualName(null);
            pictureInfo.setIndividualSpecies(edu.uic.ibeis_tourist.model.Species.UNKNOWN);
            pictureInfo.setIndividualSex(Sex.UNKNOWN);

            IbeisImage uploadedImage = null;
            List<IbeisAnnotation> imageAnnotations = null;

            try {
                uploadedImage = ibeis.uploadImage(new File(ImageUtils.PATH_TO_IMAGE_FILE + mFileName));
                System.out.println("UPLOADED IMAGE: id=" + uploadedImage.getId());
            } catch (UnsupportedImageFileTypeException | IOException | UnsuccessfulHttpRequestException e) {
                //TODO handle exception
                System.out.println("Error uploading image\n");
                e.printStackTrace(System.out);
            }

            try {
                if(uploadedImage != null) {
                    // TODO this is a workaround
                    imageAnnotations = ibeis.runAnimalDetection(Arrays.asList(uploadedImage, uploadedImage), Species.GIRAFFE).get(0);
                    System.out.println("IMAGE ANNOTATIONS: " + imageAnnotations);
                }
            } catch (IOException | UnsuccessfulHttpRequestException e) {
                //TODO handle exception
                System.out.println("Error in animal detection\n");
                e.printStackTrace(System.out);
            }

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
