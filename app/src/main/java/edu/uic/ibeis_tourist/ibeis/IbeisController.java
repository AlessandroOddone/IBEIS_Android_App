package edu.uic.ibeis_tourist.ibeis;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.uic.ibeis_java_api.api.Ibeis;
import edu.uic.ibeis_java_api.api.IbeisAnnotation;
import edu.uic.ibeis_java_api.api.IbeisImage;
import edu.uic.ibeis_java_api.api.IbeisIndividual;
import edu.uic.ibeis_java_api.identification_tools.IbeisDbAnnotationInfosWrapper;
import edu.uic.ibeis_java_api.identification_tools.identification_algorithm.IdentificationAlgorithm;
import edu.uic.ibeis_java_api.identification_tools.identification_algorithm.IdentificationAlgorithmType;
import edu.uic.ibeis_java_api.identification_tools.identification_algorithm.result.IdentificationAlgorithmResult;
import edu.uic.ibeis_java_api.values.Species;
import edu.uic.ibeis_tourist.PictureDetailActivity;
import edu.uic.ibeis_tourist.R;
import edu.uic.ibeis_tourist.exceptions.MatchNotFoundException;
import edu.uic.ibeis_tourist.local_database.LocalDatabase;
import edu.uic.ibeis_tourist.model.PictureInfo;
import edu.uic.ibeis_tourist.utils.ImageUtils;


public class IbeisController implements IbeisInterface {

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
                //upload image and annotation
                image = ibeis.uploadImage(new File(ImageUtils.PATH_TO_IMAGE_FILE + mPictureInfo.getFileName()));
                IbeisAnnotation queryAnnotation = ibeis.addAnnotation(image, mPictureInfo.getAnnotationBbox());

                //identification
                IbeisDbAnnotationInfosWrapper ibeisDbAnnotationInfosWrapper = readIbeisDbAnnotationInfosWrapperFromFile();
                if (ibeisDbAnnotationInfosWrapper != null) {
                    IdentificationAlgorithm identificationAlgorithm = new IdentificationAlgorithm(ibeisDbAnnotationInfosWrapper,
                            IdentificationAlgorithmType.THRESHOLDS_ONE_VS_ALL, 0.5, 0.5, 0.5, 1, false);
                    IdentificationAlgorithmResult identificationAlgorithmResult = identificationAlgorithm.execute(queryAnnotation);

                    IbeisIndividual resultIndividual = identificationAlgorithmResult.getIndividual();
                    Species resultSpecies = identificationAlgorithmResult.getSpecies();
                    if (resultSpecies != null) {
                        mPictureInfo.setIndividualSpecies(resultSpecies);
                        if(resultIndividual != null) {
                            mPictureInfo.setIndividualName(resultIndividual.getName());
                            mPictureInfo.setIndividualSex(resultIndividual.getSex());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    ibeis.deleteImage(image);
                } catch (Exception e) {
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
            PictureDetailActivity pictureDetailActivity = (PictureDetailActivity) mContext;
            pictureDetailActivity.displayPictureInfo(mPictureInfo);
        }

        private IbeisDbAnnotationInfosWrapper readIbeisDbAnnotationInfosWrapperFromFile() {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(mContext.getResources().openRawResource(R.raw.one_vs_many_thresholds_annot_infos)));
                return IbeisDbAnnotationInfosWrapper.fromJson(reader.readLine());

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null) reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
