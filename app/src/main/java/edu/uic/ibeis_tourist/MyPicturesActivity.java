package edu.uic.ibeis_tourist;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import edu.uic.ibeis_tourist.interfaces.LocalDatabaseInterface;
import edu.uic.ibeis_tourist.local_database.LocalDatabase;
import edu.uic.ibeis_tourist.model.Location;
import edu.uic.ibeis_tourist.model.PictureInfo;


public class MyPicturesActivity extends ActionBarActivity
        implements MyPicturesListFragment.OnListFragmentInteractionListener, MyPicturesMapFragment.OnMapFragmentInteractionListener {

    private static final String LOCATION = "location";
    private static final String PICTURE_LIST = "pictureList";
    private static final String CURRENT_FRAGMENT = "currentFragment";

    private static final String APP_PREFS = "appPrefsFile";
    private static final String MY_PICTURES_FRAGMENT_PREF = "myPicturesFragmentPref";

    private LocalDatabaseInterface localDb;

    private Location location;
    private ArrayList<PictureInfo> pictureList;

    private enum CurrentFragment {
        LIST("List"), MAP("Map");

        private String value;

        CurrentFragment(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private CurrentFragment currentFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_pictures);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_pictures_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState != null) {
            location = savedInstanceState.getParcelable(LOCATION);
            pictureList = savedInstanceState.getParcelableArrayList(PICTURE_LIST);
            currentFragment = CurrentFragment.values()[savedInstanceState.getInt(CURRENT_FRAGMENT)];
        }
        else {
            Intent intent = getIntent();
            location = intent.getParcelableExtra(LOCATION);
            pictureList = intent.getParcelableArrayListExtra(PICTURE_LIST);
            // Restore preferences
            SharedPreferences appPrefs = getSharedPreferences(APP_PREFS, 0);
            String fragmentPref = appPrefs.getString(MY_PICTURES_FRAGMENT_PREF, null);
            if (fragmentPref != null) {
                if (fragmentPref.equals(CurrentFragment.LIST.getValue())) {
                    currentFragment = CurrentFragment.LIST;
                }
                else if (fragmentPref.equals(CurrentFragment.MAP.getValue())) {
                    currentFragment = CurrentFragment.MAP;
                }
                else {
                    //TODO throw exception
                    System.out.println("Error: invalid fragment pref");
                    return;
                }
            }
            else {
                currentFragment = CurrentFragment.LIST;
            }
        }
        localDb = new LocalDatabase();
        if (location == null) {
            localDb.getAllPictures(this);
        }
        else {
            localDb.getAllPicturesAtLocation(location.getId(), this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Save fragment preferences
        SharedPreferences settings = getSharedPreferences(APP_PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(MY_PICTURES_FRAGMENT_PREF, currentFragment.getValue());
        editor.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(LOCATION, location);
        outState.putParcelableArrayList(PICTURE_LIST, pictureList);
        outState.putInt(CURRENT_FRAGMENT, currentFragment.ordinal());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_pictures, menu);
        if (currentFragment == CurrentFragment.LIST) {
            menu.findItem(R.id.my_pictures_list_menu_item).setVisible(false);
            menu.findItem(R.id.my_pictures_map_menu_item).setVisible(true);
        }
        else if (currentFragment == CurrentFragment.MAP) {
            menu.findItem(R.id.my_pictures_list_menu_item).setVisible(true);
            menu.findItem(R.id.my_pictures_map_menu_item).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.my_pictures_list_menu_item:
                currentFragment = CurrentFragment.LIST;
                displayFragment(currentFragment);
                return true;

            case R.id.my_pictures_map_menu_item:
                currentFragment = CurrentFragment.MAP;
                displayFragment(currentFragment);
                return true;

        }
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

    private void displayFragment(CurrentFragment currentFragment) {
        showProgressBar();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (currentFragment == CurrentFragment.LIST) {
            fragmentTransaction.replace(R.id.my_pictures_container,
                    MyPicturesListFragment.newInstance(pictureList, location));
            currentFragment = CurrentFragment.LIST;
            invalidateOptionsMenu();
        }
        else if (currentFragment == CurrentFragment.MAP) {
            fragmentTransaction.replace(R.id.my_pictures_container,
                    MyPicturesMapFragment.newInstance(pictureList, location));
            currentFragment = CurrentFragment.MAP;
            invalidateOptionsMenu();
        }
        else {
            //TODO throw exception
            System.out.println("Error: invalid fragment code");
            return;
        }

        fragmentTransaction.commit();
    }

    // Asynchronously called by local database when pictures are retrieved
    public void displayPictureInfoList(List<PictureInfo> pictureList) {
        System.out.println("(Activity) pictureInfoList: SIZE = " + pictureList.size());
        this.pictureList = new ArrayList<>(pictureList);
        displayFragment(currentFragment);
    }

    @Override
    public void onListReady() {
        showContainer();
    }

    @Override
    public void onMapReady() {
        showContainer();
    }

    private void showProgressBar() {
        findViewById(R.id.my_pictures_progress_bar).setVisibility(View.VISIBLE);
        findViewById(R.id.my_pictures_container).setVisibility(View.GONE);
    }

    private void showContainer() {
        findViewById(R.id.my_pictures_progress_bar).setVisibility(View.GONE);
        findViewById(R.id.my_pictures_container).setVisibility(View.VISIBLE);
    }

}
