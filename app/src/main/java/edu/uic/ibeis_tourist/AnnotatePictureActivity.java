package edu.uic.ibeis_tourist;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.GregorianCalendar;

import edu.uic.ibeis_java_api.api.annotation.BoundingBox;
import edu.uic.ibeis_java_api.values.Sex;
import edu.uic.ibeis_java_api.values.Species;
import edu.uic.ibeis_tourist.activity_enums.ActivityEnum;
import edu.uic.ibeis_tourist.exceptions.ImageLoadingException;
import edu.uic.ibeis_tourist.model.Location;
import edu.uic.ibeis_tourist.model.PictureInfo;
import edu.uic.ibeis_tourist.model.Position;
import edu.uic.ibeis_tourist.utils.ImageUtils;
import edu.uic.ibeis_tourist.view_elements.DragRectImageView;


public class AnnotatePictureActivity extends ActionBarActivity {

    private static final String PICTURE_INFO = "pictureInfo";
    private static final String IMAGE_BITMAP = "imageBitmap";

    private Toolbar toolbar;
    private PrimaryDrawerItem homeDrawerItem;
    private SecondaryDrawerItem myPicturesDrawerItem;
    private DragRectImageView annotatePictureImageView;

    private PictureInfo pictureInfo;
    private Bitmap imageBitmap;

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.annotate_picture_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setIcon(R.drawable.ic_logo);
            getSupportActionBar().setTitle(null);
        }
    }

    private void initNavigationDrawer() {
        homeDrawerItem = new PrimaryDrawerItem().withName(R.string.drawer_item_home).withIcon(R.drawable.ic_home_drawer).withSetSelected(false);
        myPicturesDrawerItem = new SecondaryDrawerItem().withName(R.string.drawer_item_my_pictures).withIcon(R.drawable.ic_my_pictures_drawer).withSetSelected(false);

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.drawer_header_background)
                .addProfiles(
                        new ProfileDrawerItem().withName("User").withEmail("user@ibeis.com").withIcon(getResources().getDrawable(R.drawable.ic_default_user))
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();

        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        homeDrawerItem,
                        new DividerDrawerItem(),
                        myPicturesDrawerItem
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if(drawerItem.equals(homeDrawerItem)) {
                            gotoHome();
                        }
                        if(drawerItem.equals(myPicturesDrawerItem)) {
                            gotoMyPictures();
                        }
                        return false;
                    }
                })
                .build();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotate_picture);
        initToolbar();
        initNavigationDrawer();

        annotatePictureImageView = (DragRectImageView)findViewById(R.id.annotate_picture_image_view);

        if(savedInstanceState != null) {
            pictureInfo = savedInstanceState.getParcelable(PICTURE_INFO);
            imageBitmap = savedInstanceState.getParcelable(IMAGE_BITMAP);
            annotatePictureImageView.setImageBitmap(imageBitmap);
        }
        else {
            Intent intent = getIntent();

            GregorianCalendar dateTime = new GregorianCalendar();
            long timeInMillis = intent.getLongExtra("dateTime", 0);
            if(timeInMillis != 0) {
                dateTime.setTimeInMillis(timeInMillis);
            }
            else {
                dateTime = null;
            }

            pictureInfo = new PictureInfo();
            pictureInfo.setFileName(intent.getStringExtra("fileName"));
            pictureInfo.setLocation((Location) intent.getParcelableExtra("location"));
            pictureInfo.setPosition((Position) intent.getParcelableExtra("position"));
            pictureInfo.setDateTime(dateTime);
            pictureInfo.setIndividualName(null);
            pictureInfo.setIndividualSpecies(Species.UNKNOWN);
            pictureInfo.setIndividualSex(Sex.UNKNOWN);
            pictureInfo.setAnnotationBbox(null);

            try {
                annotatePictureImageView.setImageDrawable(null);
                imageBitmap = ImageUtils.getRectangularBitmap(pictureInfo.getFileName(),
                        ImageUtils.dpToPx(this, annotatePictureImageView.getLayoutParams().height),
                        ImageUtils.dpToPx(this, annotatePictureImageView.getLayoutParams().width));
                annotatePictureImageView.setImageBitmap(imageBitmap);
            } catch (ImageLoadingException e) {
                annotatePictureImageView.setImageDrawable(null);
                if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    annotatePictureImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_no_img_available));
                }
                else {
                    annotatePictureImageView.setBackground(getResources().getDrawable(R.drawable.ic_no_img_available));
                }
            }
        }

        if (annotatePictureImageView != null) {
            annotatePictureImageView.setOnUpCallback(new DragRectImageView.OnUpCallback() {
                @Override
                public void onRectFinished(final BoundingBox boundingBox) {
                    pictureInfo.setAnnotationBbox(boundingBox);
                    System.out.println("BOUNDING BOX: " + pictureInfo.getAnnotationBbox());
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        homeDrawerItem.withSetSelected(false);
        myPicturesDrawerItem.withSetSelected(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(PICTURE_INFO, pictureInfo);
        outState.putParcelable(IMAGE_BITMAP, imageBitmap);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_annotate_picture, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_go_ahead) {
            goToPictureDetail();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void goToPictureDetail() {
        Intent pictureDetailIntent = new Intent(this, PictureDetailActivity.class);
        pictureDetailIntent.putExtra("callingActivity", ActivityEnum.AnnotatePictureActivity.getValue());
        pictureDetailIntent.putExtra("pictureInfo", pictureInfo);

        startActivity(pictureDetailIntent);
    }

    private void gotoHome() {
        homeDrawerItem.withSetSelected(false);
        myPicturesDrawerItem.withSetSelected(false);

        startActivity(new Intent(this, MainActivity.class));
    }

    private void gotoMyPictures() {
        startActivity(new Intent(this, MyPicturesActivity.class));
    }
}
