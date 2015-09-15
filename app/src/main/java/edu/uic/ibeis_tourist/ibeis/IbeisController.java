package edu.uic.ibeis_tourist.ibeis;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.uic.ibeis_java_api.api.Ibeis;
import edu.uic.ibeis_java_api.api.IbeisAnnotation;
import edu.uic.ibeis_java_api.api.IbeisImage;
import edu.uic.ibeis_java_api.api.IbeisIndividual;
import edu.uic.ibeis_java_api.api.IbeisQueryResult;
import edu.uic.ibeis_java_api.api.IbeisQueryScore;
import edu.uic.ibeis_java_api.values.Sex;
import edu.uic.ibeis_tourist.exceptions.MatchNotFoundException;
import edu.uic.ibeis_tourist.local_database.LocalDatabase;
import edu.uic.ibeis_tourist.model.PictureInfo;
import edu.uic.ibeis_tourist.model.SexEnum;
import edu.uic.ibeis_tourist.model.SpeciesEnum;
import edu.uic.ibeis_tourist.utils.ImageUtils;


public class IbeisController implements edu.uic.ibeis_tourist.interfaces.IbeisInterface {

    private static final int BROOKFIELD_GIRAFFES_ENCOUNTER_ID = 38;
    private static final double QUERY_RECOGNITION_THRESHOLD = 0.1;

    @Override
    public void identifyIndividual(PictureInfo pictureInfo, Context context) throws MatchNotFoundException {
        //System.out.println("IbeisController: Identify Individual");
        new IdentifyIndividualAsyncTask(pictureInfo, context).execute();
    }

    // AsyncTask classes implementation

    private class IdentifyIndividualAsyncTask extends AsyncTask<Void, Void, PictureInfo> {

        private Ibeis ibeis;

        private PictureInfo mPictureInfo;
        private Context mContext;

        private IdentifyIndividualAsyncTask(PictureInfo pictureInfo, Context context) {
            //System.out.println("IdentifyIndividualAsyncTask");
            ibeis = new Ibeis();
            mPictureInfo = pictureInfo;
            mContext = context;
        }

        @Override
        protected PictureInfo doInBackground(Void... params) {
            //System.out.println("IbeisInterfaceImplementation: IdentifyIndividual AsyncTask");
            IbeisImage image = null;

            try {
                //upload image and add annotation
                image = ibeis.uploadImage(new File(ImageUtils.PATH_TO_IMAGE_FILE + mPictureInfo.getFileName()));
                //System.out.println("UPLOADED IMAGE: " + image.getId());

                //System.out.println("BOUNDING BOX: " + mPictureInfo.getAnnotationBbox());
                IbeisAnnotation queryAnnotation = ibeis.addAnnotation(image, mPictureInfo.getAnnotationBbox());
                //System.out.println("QUERY ANNOTATION: " + queryAnnotation.getId());

                List<IbeisAnnotation> dbAnnotations = new ArrayList<>();
                for(IbeisImage i : ibeis.getEncounterById(BROOKFIELD_GIRAFFES_ENCOUNTER_ID).getImages()) {
                    dbAnnotations.addAll(i.getAnnotations());
                }

                IbeisQueryResult queryResult = ibeis.query(queryAnnotation, dbAnnotations);
                List<IbeisQueryScore> queryScores = queryResult.getScores();
                //System.out.println("QUERY RESULT: " + queryResult);

                //sort query scores from the highest to the lowest
                Collections.sort(queryScores, Collections.reverseOrder());
                //get the highest score
                IbeisQueryScore highestScore = queryScores.get(0);
                //System.out.println("HIGHEST SCORE: " + highestScore);

                if(highestScore.getScore() >= QUERY_RECOGNITION_THRESHOLD) {
                    IbeisIndividual individual = highestScore.getDbAnnotation().getIndividual();
                    mPictureInfo.setIndividualName(individual.getName());
                    mPictureInfo.setIndividualSpecies(SpeciesEnum.GIRAFFE);
                    Sex individualSex = individual.getSex();
                    System.out.println("SEX: " + individualSex);

                    if (individualSex == Sex.MALE) {
                        mPictureInfo.setIndividualSex(SexEnum.MALE);
                    } else if (individualSex == Sex.FEMALE) {
                       mPictureInfo.setIndividualSex(SexEnum.FEMALE);
                    } else {
                        mPictureInfo.setIndividualSex(SexEnum.UNKNOWN);
                    }
                }
            } catch (Exception e) {//TODO handle exceptions
                //System.out.println("EXCEPTION BRANCH 1");
                e.printStackTrace();
            } finally {
                try {
                    //System.out.println("DELETE IMAGE");
                    ibeis.deleteImage(image);
                } catch (Exception e) {//TODO handle exception
                    //System.out.println("EXCEPTION BRANCH 2");
                    e.printStackTrace();
                }
            }
            return mPictureInfo;
        }

        @Override
        protected void onPostExecute(PictureInfo pictureInfo) {
            //System.out.println("IdentifyIndividualAsyncTask: onPostExecute");
            super.onPostExecute(pictureInfo);
            LocalDatabase localDb = new LocalDatabase();
            localDb.addPicture(pictureInfo, mContext);
        }
    }
}
