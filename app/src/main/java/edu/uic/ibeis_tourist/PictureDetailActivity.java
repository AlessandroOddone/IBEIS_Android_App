package edu.uic.ibeis_tourist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Calendar;

import edu.uic.ibeis_tourist.activity_enums.ActivityEnum;
import edu.uic.ibeis_tourist.exceptions.MatchNotFoundException;
import edu.uic.ibeis_tourist.ibeis.IbeisController;
import edu.uic.ibeis_tourist.ibeis.IbeisInterface;
import edu.uic.ibeis_tourist.model.Location;
import edu.uic.ibeis_tourist.model.PictureInfo;
import edu.uic.ibeis_tourist.model.Position;
import edu.uic.ibeis_tourist.model.SexEnum;
import edu.uic.ibeis_tourist.model.SpeciesEnum;
import edu.uic.ibeis_tourist.utils.DateTimeUtils;
import edu.uic.ibeis_tourist.utils.ImageUtils;


public class PictureDetailActivity extends ActionBarActivity {

    private static final String NOT_AVAILABLE = "N/A";

    private PictureInfo pictureInfo;

    private Toolbar toolbar;
    private PrimaryDrawerItem homeDrawerItem;
    private SecondaryDrawerItem myPicturesDrawerItem;

    private LinearLayout detailLayout;
    private ProgressBar detailProgressBar;

    private ImageView detailImageView;
    private TextView speciesText;
    private TextView nameText;
    private TextView sexText;
    private TextView locationText;
    private TextView coordinatesText;
    private TextView datetimeText;

    private IbeisInterface ibeis;

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.picture_detail_toolbar);
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
        System.out.println("MyPictureDetailActivity: onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_detail);
        initToolbar();
        initNavigationDrawer();

        detailLayout = (LinearLayout) findViewById(R.id.detail_layout);
        detailProgressBar = (ProgressBar) findViewById(R.id.detail_progress_bar);

        detailImageView = (ImageView) findViewById(R.id.detail_image_view);
        speciesText = (TextView) findViewById(R.id.detail_individual_species);
        nameText = (TextView) findViewById(R.id.detail_individual_name);
        sexText = (TextView) findViewById(R.id.detail_individual_sex);
        locationText = (TextView) findViewById(R.id.detail_encounter_location);
        coordinatesText = (TextView) findViewById(R.id.detail_encounter_coordinates);
        datetimeText = (TextView) findViewById(R.id.detail_encounter_date);

        detailLayout.setVisibility(View.GONE);
        detailProgressBar.setVisibility(View.VISIBLE);

        if(savedInstanceState != null) {
            //System.out.println("PictureDetailActivity: savedInstanceState != null");
            displayPictureInfo((PictureInfo)savedInstanceState.getParcelable("pictureInfo"));
        }
        else {
            //System.out.println("PictureDetailActivity: savedInstanceState == null");
            Intent intent = getIntent();

            int callingActivity = intent.getIntExtra("callingActivity", 0);

            if(callingActivity == ActivityEnum.AnnotatePictureActivity.getValue()) {
                //System.out.println("PictureDetailActivity: calling activity: MainActivity");
                pictureInfo = intent.getParcelableExtra("pictureInfo");

                try {
                    ibeis = new IbeisController();
                    ibeis.identifyIndividual(pictureInfo, this);
                } catch (MatchNotFoundException e) {
                    // TODO handle match not found
                    e.printStackTrace();
                }
            }
            else if(callingActivity == ActivityEnum.MyPicturesActivity.getValue()) {
                //System.out.println("PictureDetailActivity: calling activity: MyPicturesActivity");
                displayPictureInfo((PictureInfo) intent.getParcelableExtra("pictureInfo"));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        homeDrawerItem.withSetSelected(false);
        myPicturesDrawerItem.withSetSelected(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_picture_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("pictureInfo", pictureInfo);
        super.onSaveInstanceState(outState);
    }

    public void displayPictureInfo(PictureInfo pictureInfo) {
        System.out.println("PictureDetailActivity: displayPictureInfo");
        this.pictureInfo = pictureInfo;
        if(pictureInfo == null) {
            // TODO handle null pictureInfo
            return;
        }
        else {
            detailLayout.setVisibility(View.VISIBLE);
            detailProgressBar.setVisibility(View.GONE);

            Picasso.with(this)
                    .load(new File((ImageUtils.getImagesMainFolder() + pictureInfo.getFileName())))
                    .error(R.drawable.ic_no_img_available)
                    .fit()
                    .centerInside()
                    .into(detailImageView);
            /*
            try {
                detailImageView.setImageDrawable(null);
                detailImageView.setImageBitmap (
                        ImageUtils.getRectangularBitmap (
                        pictureInfo.getFileName(),
                        ImageUtils.dpToPx(this, detailImageView.getLayoutParams().height),
                        ImageUtils.dpToPx(this, detailImageView.getLayoutParams().width)
                        )
                );
            } catch (ImageLoadingException e) {
                detailImageView.setImageDrawable(null);
                if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    detailImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_no_img_available));
                }
                else {
                    detailImageView.setBackground(getResources().getDrawable(R.drawable.ic_no_img_available));
                }
            }
            */

            SpeciesEnum species = pictureInfo.getIndividualSpecies();
            String name = pictureInfo.getIndividualName();
            SexEnum sex = pictureInfo.getIndividualSex();
            Location location = pictureInfo.getLocation();
            Position position = pictureInfo.getPosition();
            Calendar datetime = pictureInfo.getDateTime();

            setDetailAttributeText(speciesText, "Species: " + (species != null ? species.asString() : NOT_AVAILABLE));
            setDetailAttributeText(nameText, "Name: " + (name != null ? name : NOT_AVAILABLE));
            setDetailAttributeText(sexText, "Sex: " + (sex != null ? sex.asString() : NOT_AVAILABLE));
            setDetailAttributeText(locationText, "Location: " + (location != null ? location.getName() : NOT_AVAILABLE));
            setDetailAttributeText(coordinatesText, "Coordinates: " + (position != null ? "("
                    + new DecimalFormat("0.000").format(position.getLatitude()) + ", " +
                    new DecimalFormat("0.000").format(position.getLongitude()) + ")" : NOT_AVAILABLE));
            setDetailAttributeText(datetimeText, "Time: " + (datetime != null ?
                    DateTimeUtils.calendarToString(datetime, DateTimeUtils.DateFormat.DATETIME) : NOT_AVAILABLE));
        }
    }

    private void setDetailAttributeText(TextView textView, String text) {
        int i = text.indexOf(':');
        Spannable spannableText = new SpannableString(text);
        spannableText.setSpan(new TextAppearanceSpan(this, R.style.PictureDetailAttributeTitle), 0, i+1,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableText.setSpan(new TextAppearanceSpan(this, R.style.PictureDetailAttributeValue), i + 1, text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableText);
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