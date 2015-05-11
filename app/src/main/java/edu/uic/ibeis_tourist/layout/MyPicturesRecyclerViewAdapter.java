package edu.uic.ibeis_tourist.layout;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.uic.ibeis_tourist.MyPictureDetailActivity;
import edu.uic.ibeis_tourist.R;
import edu.uic.ibeis_tourist.other.HackedTouchDelegate;
import edu.uic.ibeis_tourist.exceptions.ImageLoadingException;
import edu.uic.ibeis_tourist.model.PictureInfo;
import edu.uic.ibeis_tourist.utils.DateTimeUtils;
import edu.uic.ibeis_tourist.utils.ImageUtils;
import edu.uic.ibeis_tourist.values.ActivityEnum;

public class MyPicturesRecyclerViewAdapter extends RecyclerView.Adapter<MyPicturesRecyclerViewAdapter.ViewHolder> {

    public static int PICTURE_LAYOUT_HEIGHT;
    public static int PICTURE_LAYOUT_WIDTH;

    private List<PictureInfo> mPictureInfoList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public Resources resources;

        public View view;
        public ImageView picture;
        public TextView speciesText;
        public TextView nameText;
        public TextView locationText;
        public TextView dateText;
        public ImageButton favoriteButton;

        public ViewHolder(View view) {
            super(view);
            this.view = view;

            resources = view.getResources();

            picture = (ImageView) view.findViewById(R.id.item_picture);
            MyPicturesRecyclerViewAdapter.PICTURE_LAYOUT_HEIGHT = ImageUtils.dpToPx(view.getContext(), picture.getLayoutParams().height);
            MyPicturesRecyclerViewAdapter.PICTURE_LAYOUT_WIDTH = ImageUtils.dpToPx(view.getContext(), picture.getLayoutParams().width);

            speciesText = (TextView) view.findViewById(R.id.item_individual_species);
            nameText = (TextView) view.findViewById(R.id.item_individual_name);
            locationText = (TextView) view.findViewById(R.id.item_location);
            dateText = (TextView) view.findViewById(R.id.item_datetime);
            favoriteButton = (ImageButton) view.findViewById(R.id.item_star_btn);
        }
    }

    public MyPicturesRecyclerViewAdapter(List<PictureInfo> pictureInfoList) {
        mPictureInfoList = pictureInfoList;
    }

    @Override
    public MyPicturesRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.my_pictures_list_item, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyPicturesRecyclerViewAdapter.ViewHolder viewHolder, final int position) {

        try {
            viewHolder.picture.setImageDrawable(null);
            viewHolder.picture.setImageBitmap(
                    ImageUtils.getCircularBitmap(mPictureInfoList.get(position).getFileName(),
                            PICTURE_LAYOUT_HEIGHT, PICTURE_LAYOUT_WIDTH));
        } catch (ImageLoadingException e) {
            viewHolder.picture.setImageDrawable(null);
            if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                viewHolder.picture.setBackgroundDrawable(viewHolder.resources.getDrawable(R.drawable.ic_no_img_available));
            }
            else {
                viewHolder.picture.setBackground(viewHolder.resources.getDrawable(R.drawable.ic_no_img_available));
            }
            e.printStackTrace();
        }
        String species = mPictureInfoList.get(position).getIndividualSpecies().asString().toUpperCase();
        String name = mPictureInfoList.get(position).getIndividualName();
        String location = mPictureInfoList.get(position).getLocation().getName();
        String date = DateTimeUtils.calendarToString
                (mPictureInfoList.get(position).getDateTime(), DateTimeUtils.DateFormat.DATE_ONLY);

        if(species == null) {
            viewHolder.speciesText.setVisibility(View.GONE);
        }
        else {
            viewHolder.speciesText.setVisibility(View.VISIBLE);
            viewHolder.speciesText.setText(species);
        }
        if(name == null) {
            viewHolder.nameText.setVisibility(View.GONE);
        }
        else {
            viewHolder.nameText.setVisibility(View.VISIBLE);
            viewHolder.nameText.setText(name);
        }
        if(location == null) {
            viewHolder.locationText.setVisibility(View.GONE);
        }
        else {
            viewHolder.locationText.setVisibility(View.VISIBLE);
            viewHolder.locationText.setText("@" + location);
        }
        viewHolder.dateText.setText(date);

        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myPictureDetailIntent = new Intent(v.getContext(), MyPictureDetailActivity.class);
                myPictureDetailIntent.putExtra("pictureInfo", mPictureInfoList.get(position));
                myPictureDetailIntent.putExtra("callingActivity", ActivityEnum.MyPicturesActivity.getValue());
                v.getContext().startActivity(myPictureDetailIntent);
            }
        });


        // increase clickable area of favorite button
        final View parent = (View) viewHolder.favoriteButton.getParent();
        parent.post(new Runnable() {
            @Override
            public void run() {
                Rect rect = new Rect();
                ImageView delegate = viewHolder.favoriteButton;
                delegate.getHitRect(rect);
                int extraPadding = 65;
                rect.top -= extraPadding;
                rect.bottom += extraPadding;
                rect.left -= extraPadding;
                rect.right += extraPadding;
                parent.setTouchDelegate(new HackedTouchDelegate(rect, delegate));
            }
        });

        viewHolder.favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
                if(v.isSelected()) {
                    v.setSelected(false);
                }
                else {
                    v.setSelected(true);
                }
                return;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPictureInfoList.size();
    }
}
