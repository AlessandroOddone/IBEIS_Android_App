package edu.uic.ibeis_tourist;

import android.content.Intent;
import android.os.Build;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;

import edu.uic.ibeis_tourist.exceptions.ImageLoadingException;
import edu.uic.ibeis_tourist.exceptions.MatchNotFoundException;
import edu.uic.ibeis_tourist.exceptions.UnexpectedCallingActivityException;
import edu.uic.ibeis_tourist.ibeis.IbeisInterfaceImplementation;
import edu.uic.ibeis_tourist.interfaces.IbeisInterface;
import edu.uic.ibeis_tourist.model.Location;
import edu.uic.ibeis_tourist.model.PictureInfo;
import edu.uic.ibeis_tourist.model.Position;
import edu.uic.ibeis_tourist.model.Sex;
import edu.uic.ibeis_tourist.model.Species;
import edu.uic.ibeis_tourist.utils.DateTimeUtils;
import edu.uic.ibeis_tourist.utils.ImageUtils;
import edu.uic.ibeis_tourist.values.ActivityEnum;


public class MyPictureDetailActivity extends ActionBarActivity {

    private static final String NOT_AVAILABLE = "N/A";

    private PictureInfo pictureInfo;

    private RelativeLayout detailLayout;
    private ProgressBar detailProgressBar;

    private ImageView detailImageView;
    private TextView speciesText;
    private TextView nameText;
    private TextView sexText;
    private TextView locationText;
    private TextView coordinatesText;
    private TextView datetimeText;

    private IbeisInterface ibeis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("MyPictureDetailActivity: onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_picture_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_picture_detail_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        detailLayout = (RelativeLayout) findViewById(R.id.detail_layout);
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
            System.out.println("savedInstanceState != null");

            displayPictureInfo((PictureInfo)savedInstanceState.getParcelable("pictureInfo"));
        }
        else {
            System.out.println("savedInstanceState == null");

            Intent intent = getIntent();

            int callingActivity = intent.getIntExtra("callingActivity", 0);

            if(callingActivity == ActivityEnum.MainActivity.getValue()) {
                System.out.println("calling activity: MainActivity");

                Location location = intent.getParcelableExtra("location");
                String fileName = intent.getStringExtra("fileName");

                GregorianCalendar dateTime = new GregorianCalendar();
                long timeInMillis = intent.getLongExtra("dateTime", 0);
                if(timeInMillis != 0) {
                    dateTime.setTimeInMillis(timeInMillis);
                }
                else {
                    dateTime = null;
                }

                Position position = intent.getParcelableExtra("position");

                try {
                    ibeis = new IbeisInterfaceImplementation();
                    ibeis.identifyIndividual(fileName, location, position, dateTime, this);
                } catch (MatchNotFoundException e) {
                    // TODO handle match not found
                    e.printStackTrace();
                }
            }
            else if(callingActivity == ActivityEnum.MyPicturesActivity.getValue()) {
                System.out.println("calling activity: MyPicturesActivity");

                displayPictureInfo((PictureInfo) intent.getParcelableExtra("pictureInfo"));
            }
            else {
                System.out.println("error: invalid calling activity");

                new UnexpectedCallingActivityException().printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_picture_detail, menu);
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
        System.out.println("displayPictureInfo");

        this.pictureInfo = pictureInfo;

        if(pictureInfo == null) {
            System.out.println("pictureInfo == null");

            // TODO handle null pictureInfo
            return;
        }
        else {
            System.out.println("pictureInfo != null");

            detailLayout.setVisibility(View.VISIBLE);
            detailProgressBar.setVisibility(View.GONE);

            try {
                detailImageView.setImageDrawable(null);
                detailImageView.setImageBitmap(ImageUtils.getRectangularBitmap(pictureInfo.getFileName(),
                        ImageUtils.dpToPx(this, detailImageView.getLayoutParams().height),
                        ImageUtils.dpToPx(this, detailImageView.getLayoutParams().width)));
            } catch (ImageLoadingException e) {
                detailImageView.setImageDrawable(null);
                if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    detailImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_no_img_available));
                }
                else {
                    detailImageView.setBackground(getResources().getDrawable(R.drawable.ic_no_img_available));
                }
            }

            Species species = pictureInfo.getIndividualSpecies();
            String name = pictureInfo.getIndividualName();
            Sex sex = pictureInfo.getIndividualSex();
            Location location = pictureInfo.getLocation();
            Position position = pictureInfo.getPosition();
            Calendar datetime = pictureInfo.getDateTime();

            setDetailAttributeText(speciesText, "Species: " + (species != null ? species.asString() : NOT_AVAILABLE));
            setDetailAttributeText(nameText, "Name: " + (name != null ? name : NOT_AVAILABLE));
            setDetailAttributeText(sexText, "Sex: " + (sex != null ? sex.asString() : NOT_AVAILABLE));
            setDetailAttributeText(locationText, "Location: " + (location != null ? location.getName() : NOT_AVAILABLE));
            setDetailAttributeText(coordinatesText, "Coordinates: " + (position != null ? "("
                    + position.getLatitude() + ", " + position.getLongitude() + ")" : NOT_AVAILABLE));
            setDetailAttributeText(datetimeText, "Time: " + (datetime != null ?
                    DateTimeUtils.calendarToString(datetime, DateTimeUtils.DateFormat.DATETIME) : NOT_AVAILABLE));
        }
    }

    private void setDetailAttributeText(TextView textView, String text) {
        int i = text.indexOf(':');
        Spannable spannableText = new SpannableString(text);
        spannableText.setSpan(new TextAppearanceSpan(this, R.style.PictureDetailAttributeTitle), 0, i+1,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableText.setSpan(new TextAppearanceSpan(this, R.style.PictureDetailAttributeValue), i+1, text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableText);
    }
}