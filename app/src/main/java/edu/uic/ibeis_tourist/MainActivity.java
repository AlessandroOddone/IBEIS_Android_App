package edu.uic.ibeis_tourist;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.util.GregorianCalendar;

import edu.uic.ibeis_tourist.interfaces.LocalDatabaseInterface;
import edu.uic.ibeis_tourist.local_database.LocalDatabase;
import edu.uic.ibeis_tourist.model.Location;
import edu.uic.ibeis_tourist.model.Position;
import edu.uic.ibeis_tourist.services.PositionService;
import edu.uic.ibeis_tourist.utils.ImageUtils;
import edu.uic.ibeis_tourist.values.ActivityForResultRequest;
import edu.uic.ibeis_tourist.values.PositionEvent;
import io.fabric.sdk.android.Fabric;


public class MainActivity extends ActionBarActivity {
    private Button takePictureButton;
    private Button myPicturesButton;

    private static final String CUR_LAT = "currentLatitude";
    private static final String CUR_LON = "currentLongitude";
    private static final String CUR_FACING_DIRECTION = "currentFacingDirection";
    private static final String IMG_FILE_NAME = "imageFileName";
    private static final String LOCATION_DETECTED = "locationDetected";
    private static final String LOCATION = "location";

    private Intent positionServiceIntent;

    private Location location;

    private Double currentLatitude;
    private Double currentLongitude;
    private boolean gpsEnabled;

    private Float currentFacingDirection;

    private boolean takingPicture;
    private boolean locationDetected;

    private String imageFileName;

    private LocalDatabaseInterface localDb;

    /**
     * Broadcast receiver for position events
     */
    private BroadcastReceiver positionEventsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int gpsEvent = bundle.getInt("positionEvent");

                if (gpsEvent == PositionEvent.GPS_ENABLED.getValue()) {
                    gpsEnabled();
                }
                else if (gpsEvent == PositionEvent.GPS_DISABLED.getValue()) {
                    gpsDisabled();
                }
                else if (gpsEvent == PositionEvent.LOCATION_CHANGED.getValue()) {
                    locationChanged(bundle.getDouble("lat"), bundle.getDouble("lon"));
                }
                else if (gpsEvent == PositionEvent.SENSOR_CHANGED.getValue()) {
                    sensorChanged(bundle.getFloat("azimuth"));
                }
            }
        }
    };

    private void setMenuButtonText(Button button, String text) {
        int i = text.indexOf('\n');
        Spannable spannableText = new SpannableString(text);
        spannableText.setSpan(new TextAppearanceSpan(this, R.style.MainButtonTitleTextAppearance), 0, i,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new TextAppearanceSpan(this, R.style.MainButtonDescriptionTextAppearance), i, text.length(),
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        button.setText(spannableText);
    }

    private void initMenuButtons() {
        takePictureButton = (Button) findViewById(R.id.take_picture);
        takePictureButton.setTransformationMethod(null);
        setMenuButtonText(takePictureButton, getString(R.string.take_picture_button_text));

        myPicturesButton = (Button) findViewById(R.id.my_pictures);
        myPicturesButton.setTransformationMethod(null);
        setMenuButtonText(myPicturesButton, getString(R.string.my_pictures_button_text));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //System.out.println("MainActivity: onCreate");
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setIcon(R.drawable.ic_logo);
            getSupportActionBar().setTitle(null);
        }
        initMenuButtons();

        if (savedInstanceState != null && takingPicture) {
            String lat = savedInstanceState.getString(CUR_LAT);
            String lon = savedInstanceState.getString(CUR_LON);
            String facing = savedInstanceState.getString(CUR_FACING_DIRECTION);

            if(lat != null) {
                currentLatitude = Double.parseDouble(lat);
            }
            if(lon != null) {
                currentLongitude = Double.parseDouble(lon);
            }
            if (facing != null) {
                currentFacingDirection = Float.parseFloat(facing);
            }
            imageFileName = savedInstanceState.getString(IMG_FILE_NAME);
            locationDetected = savedInstanceState.getBoolean(LOCATION_DETECTED);
            location = savedInstanceState.getParcelable(LOCATION);
        }
    }

    @Override
    protected  void onStart() {
        //System.out.println("MainActivity: onStart");
        super.onStart();

        if (!takingPicture) {
            // Register GPS events receiver
            registerReceiver(positionEventsReceiver, new IntentFilter("edu.uic.ibeis_tourist.broadcast_position_event"));
            // Start GPS Service
            positionServiceIntent = new Intent(this, PositionService.class);
            startService(positionServiceIntent);
        }
        takingPicture = false;
    }

    @Override
    protected void onResume() {
        //System.out.println("MainActivity: onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        //System.out.println("MainActivity: onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        //System.out.println("MainActivity: onStop");
        if (!takingPicture) { // Check not interacting with camera app

            // Stop GPS Service
            stopService(positionServiceIntent);
            // Unregister GPS events receiver
            try {
                unregisterReceiver(positionEventsReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //System.out.println("HomeActivity: onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //System.out.println("HomeActivity: onSaveInstanceState");
        outState.putString(CUR_LAT, currentLatitude!=null ? currentLatitude.toString() : null);
        outState.putString(CUR_LON, currentLongitude!=null ? currentLongitude.toString() : null);
        outState.putString(CUR_FACING_DIRECTION, currentFacingDirection!=null ? currentFacingDirection.toString() : null);
        outState.putString(IMG_FILE_NAME, imageFileName);
        outState.putBoolean(LOCATION_DETECTED, locationDetected);
        outState.putParcelable(LOCATION, location);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int keycode = event.getKeyCode();
        final int action = event.getAction();
        if (keycode == KeyEvent.KEYCODE_MENU && action == KeyEvent.ACTION_UP) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public void showGpsAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.gps_alert_title);
        alertDialog.setMessage(R.string.gps_alert_message);

        alertDialog.setNegativeButton(R.string.gps_alert_neg, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.setPositiveButton(R.string.gps_alert_pos, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        alertDialog.show();
    }

    public void showCurrentLocationNotAvailableAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.obtaining_location_alert_title);
        alertDialog.setMessage(R.string.obtaining_location_alert_message);

        alertDialog.setNeutralButton(R.string.obtaining_location_alert_btn, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }


    public void showInvalidLocationAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.no_location_alert_title);
        alertDialog.setMessage(R.string.no_location_alert_message);

        alertDialog.setNegativeButton(R.string.no_location_alert_btn, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    public void takePicture(View v) {
        if (!gpsEnabled) {
            showGpsAlertDialog();
            return;
        }
        if (!locationDetected) {
            showCurrentLocationNotAvailableAlertDialog();
            return;
        }
        if (location == null) {
            showInvalidLocationAlertDialog();
            return;
        }

        takingPicture = true;

        imageFileName = ImageUtils.generateImageName();

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(ImageUtils.generateImageFile(imageFileName)));

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, ActivityForResultRequest.PICTURE_REQUEST.getValue());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //System.out.println("MainActivity: onActivityResult");
        if (requestCode == ActivityForResultRequest.PICTURE_REQUEST.getValue() && resultCode == RESULT_OK) {
            gotToAnnotatePicture(imageFileName);
        }
    }

    private void gotToAnnotatePicture(String pictureFileName) {
        //System.out.println("MainActivity: gotToAnnotatePictureActivity");
        // Stop GPS Service
        stopService(positionServiceIntent);
        // Unregister GPS events receiver
        try {
            unregisterReceiver(positionEventsReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        Intent annotatePictureIntent = new Intent(this, AnnotatePictureActivity.class);
        annotatePictureIntent.putExtra("fileName", pictureFileName);
        annotatePictureIntent.putExtra("location", location);
        annotatePictureIntent.putExtra("position",
                (currentLatitude != null && currentLongitude != null && currentFacingDirection != null ?
                        new Position(currentLatitude, currentLongitude, currentFacingDirection) : null));
        annotatePictureIntent.putExtra("dateTime", new GregorianCalendar().getTimeInMillis());

        startActivity(annotatePictureIntent);
    }

    public void gotoMyPictures(View v) {
        //System.out.println("MainActivity: gotToMyPictures");
        // Stop GPS Service
        stopService(positionServiceIntent);
        // Unregister GPS events receiver

        Intent myPicturesIntent = new Intent(this, MyPicturesActivity.class);
        //myPicturesActivityIntent.putExtra("location", location);
        startActivity(myPicturesIntent);
    }

    private void gpsEnabled() {
        //System.out.println("MainActivity: gpsEnabled");
        gpsEnabled = true;

        findViewById(edu.uic.ibeis_tourist.R.id.position_progress_bar).setVisibility(View.VISIBLE);
        findViewById(edu.uic.ibeis_tourist.R.id.gps_info_text).setVisibility(View.GONE);
    }

    private void gpsDisabled() {
        //System.out.println("MainActivity: gpsDisabled");
        gpsEnabled = false;
        currentLatitude = null;
        currentLongitude = null;

        findViewById(edu.uic.ibeis_tourist.R.id.position_progress_bar).setVisibility(View.GONE);
        TextView gpsInfoText = (TextView) findViewById(R.id.gps_info_text);
        gpsInfoText.setText(getResources().getText(R.string.gps_not_enabled));
        gpsInfoText.setVisibility(View.VISIBLE);
        findViewById(R.id.detected_location_text).setVisibility(View.GONE);
        locationDetected = false;
    }

    private void locationChanged(double lat, double lon) {
        //System.out.println("MainActivity: locationChanged");
        currentLatitude = lat;
        currentLongitude = lon;

        if (!locationDetected) {
            localDb = new LocalDatabase();
            localDb.getCurrentLocation(lat, lon, this);
        }
        else {
            if (currentFacingDirection != null) {
                findViewById(R.id.position_progress_bar).setVisibility(View.GONE);
                TextView gpsInfoText = (TextView) findViewById(R.id.gps_info_text);
                gpsInfoText.setText(getResources().getText(R.string.gps_available));
                gpsInfoText.setVisibility(View.VISIBLE);
            }
        }
    }

    private void sensorChanged(float facing) {
        //System.out.println("MainActivity: sensorChanged");
        currentFacingDirection = facing;

        if (locationDetected) {
            findViewById(R.id.position_progress_bar).setVisibility(View.GONE);
            TextView gpsInfoText = (TextView) findViewById(R.id.gps_info_text);
            gpsInfoText.setText(getResources().getText(R.string.gps_available));
            gpsInfoText.setVisibility(View.VISIBLE);
        }
    }

    public void currentLocationDetected(Location currentLocation) {
        locationDetected = true;
        location = currentLocation;

        findViewById(R.id.position_progress_bar).setVisibility(View.GONE);
        TextView gpsInfoText = (TextView) findViewById(R.id.gps_info_text);
        gpsInfoText.setText(getResources().getText(R.string.gps_available));
        gpsInfoText.setVisibility(View.VISIBLE);

        TextView detectedLocationText = (TextView) findViewById(R.id.detected_location_text);
        if (location != null) {
            detectedLocationText.setText(currentLocation.getName());
            detectedLocationText.setVisibility(View.VISIBLE);
        }
        else {
            detectedLocationText.setVisibility(View.GONE);
        }
    }
}
