package edu.uic.ibeis_tourist.ibeis;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;

import edu.uic.ibeis_java_api.api.Ibeis;
import edu.uic.ibeis_java_api.api.IbeisAnnotation;
import edu.uic.ibeis_java_api.api.IbeisImage;
import edu.uic.ibeis_java_api.api.IbeisIndividual;
import edu.uic.ibeis_java_api.values.Sex;
import edu.uic.ibeis_tourist.PictureDetailActivity;
import edu.uic.ibeis_tourist.R;
import edu.uic.ibeis_tourist.exceptions.MatchNotFoundException;
import edu.uic.ibeis_tourist.local_database.LocalDatabase;
import edu.uic.ibeis_tourist.model.PictureInfo;
import edu.uic.ibeis_tourist.model.SexEnum;
import edu.uic.ibeis_tourist.model.SpeciesEnum;
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

                QueryAlgorithmResult queryAlgorithmResult = new QueryAlgorithm(
                        mContext.getResources().openRawResource(R.raw.giraffe_db_hash_map),
                        mContext.getResources().openRawResource(R.raw.giraffe_db_ids_list))
                        .query(queryAnnotation);
                IbeisIndividual resultIndividual = queryAlgorithmResult.getIndividual();
                SpeciesEnum species = queryAlgorithmResult.getSpecies();
                if(resultIndividual != null) {
                    mPictureInfo.setIndividualName(resultIndividual.getName());
                    mPictureInfo.setIndividualSpecies(species.asString());
                    Sex individualSex = resultIndividual.getSex();
                    if (individualSex == Sex.MALE) {
                        mPictureInfo.setIndividualSex(SexEnum.MALE);
                    } else if (individualSex == Sex.FEMALE) {
                        mPictureInfo.setIndividualSex(SexEnum.FEMALE);
                    } else {
                        mPictureInfo.setIndividualSex(SexEnum.UNKNOWN);
                    }
                }
            } catch (Exception e) {//TODO handle exceptions
                e.printStackTrace();
            } finally {
                try {
                    ibeis.deleteImage(image);
                } catch (Exception e) {//TODO handle exception
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

    }
}
