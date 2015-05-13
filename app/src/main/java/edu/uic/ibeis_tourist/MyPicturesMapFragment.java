package edu.uic.ibeis_tourist;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import edu.uic.ibeis_tourist.model.Location;
import edu.uic.ibeis_tourist.model.LocationBounds;
import edu.uic.ibeis_tourist.model.PictureInfo;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyPicturesMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyPicturesMapFragment extends Fragment {

    private static final String LOCATION = "location";
    private static final String PICTURE_LIST = "pictureList";

    private OnMapFragmentInteractionListener mListener;

    private MapView mMapView;
    private GoogleMap mMap;

    private Location location;
    private ArrayList<PictureInfo> pictureList;

    /**
     * Use this factory method to create a new instance of MyPicturesMapFragment
     *
     * @return A new instance of fragment MyPicturesMapFragment.
     */
    public static MyPicturesMapFragment newInstance(ArrayList<PictureInfo> pictureList, Location location) {
        MyPicturesMapFragment fragment = new MyPicturesMapFragment();
        Bundle args = new Bundle();
        args.putParcelable(LOCATION, location);
        args.putParcelableArrayList(PICTURE_LIST, pictureList);
        fragment.setArguments(args);
        return fragment;
    }

    public MyPicturesMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = this.getArguments();
        if (args != null) {
            location = args.getParcelable(LOCATION);
            pictureList = args.getParcelableArrayList(PICTURE_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_my_pictures_map, container, false);
        mMapView = (MapView) v.findViewById(R.id.my_pictures_map_view);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            // TODO handle exception
            e.printStackTrace();
        }

        mMap = mMapView.getMap();
        if (mMap != null) {
            setUpMap();
        }
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnMapFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMapFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * Add elements to map here
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        displayPicturesOnMap();
    }

    public void displayPicturesOnMap() {
        final LatLngBounds.Builder mapBoundsBuilder = new LatLngBounds.Builder();

        if(pictureList != null && pictureList.size() > 0) {
            if (location != null) {
                LocationBounds bounds = location.getBounds();
                mapBoundsBuilder
                        .include(new LatLng(bounds.getSouthwestBound().getLatitude(),
                                bounds.getSouthwestBound().getLongitude()))
                        .include(new LatLng(bounds.getNortheastBound().getLatitude(),
                                bounds.getNortheastBound().getLongitude()));
            }
            else {
                for (PictureInfo p : pictureList) {
                    mapBoundsBuilder.include(new LatLng(p.getPosition().getLatitude(),
                            p.getPosition().getLongitude()));
                }
            }
            moveCamera(mapBoundsBuilder);
            addMarkers();
        }
        else {
            //TODO handle no pictures
        }
        mListener.onMapReady();
    }

    private void moveCamera(LatLngBounds.Builder mapBoundsBuilder) {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBoundsBuilder.build(),
                metrics.widthPixels, metrics.heightPixels, getResources().getInteger(R.integer.move_camera_padding)));
    }

    private void addMarkers() {
        for (PictureInfo p : pictureList) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(p.getPosition().getLatitude(), p.getPosition().getLongitude()))
                    .rotation(p.getPosition().getFacingDirection())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.maps_marker)) // marker image has to be pointing to north
                    .title(p.getIndividualName())
                    .snippet("Species: " + p.getIndividualSpecies()));
        }
    }

    /**
     * This interface must be implemented by activities that contain this fragment
     */
    public interface OnMapFragmentInteractionListener {
        public void onMapReady();
    }

}
