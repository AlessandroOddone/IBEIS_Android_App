package edu.uic.ibeis_tourist;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import edu.uic.ibeis_tourist.layout.DividerItemDecoration;
import edu.uic.ibeis_tourist.layout.MyPicturesRecyclerViewAdapter;
import edu.uic.ibeis_tourist.model.Location;
import edu.uic.ibeis_tourist.model.PictureInfo;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link edu.uic.ibeis_tourist.MyPicturesListFragment.OnListFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyPicturesListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyPicturesListFragment extends Fragment {

    private static final String LOCATION = "location";
    private static final String PICTURE_LIST = "pictureList";

    private OnListFragmentInteractionListener mListener;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Location location;
    private ArrayList<PictureInfo> pictureList;
    /**
     * Factory method to create a new instance of MyPicturesListFragment.
     *
     * @return A new instance of fragment MyPicturesListFragment.
     */
    public static MyPicturesListFragment newInstance(ArrayList<PictureInfo> pictureList, Location location) {
        MyPicturesListFragment fragment = new MyPicturesListFragment();
        Bundle args = new Bundle();
        args.putParcelable(LOCATION, location);
        args.putParcelableArrayList(PICTURE_LIST, pictureList);
        fragment.setArguments(args);
        return fragment;
    }

    public MyPicturesListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.pictureList = getArguments().getParcelableArrayList(PICTURE_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_my_pictures_list, container, false);
        // initialize Recycler View element
        mRecyclerView = (RecyclerView) v.findViewById(R.id.my_pictures_recycler_view);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity().getApplicationContext()));
        mRecyclerView.setHasFixedSize(true);

        // register recycler view for context menu
        registerForContextMenu(mRecyclerView);

        // define a layout manager and assign it to the Recycler View
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        // set initial scroll position TODO read from prefs file
        mRecyclerView.scrollToPosition(0);

        // set adapter for the Recycler View, passing pictureList as parameter
        mAdapter = new MyPicturesRecyclerViewAdapter(pictureList);
        mRecyclerView.setAdapter(mAdapter);
        mListener.onListReady();

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnListFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnListFragmentInteractionListener {
        public void onListReady();
    }

}
