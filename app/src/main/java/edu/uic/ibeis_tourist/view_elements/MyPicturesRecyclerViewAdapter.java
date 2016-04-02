package edu.uic.ibeis_tourist.view_elements;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.util.List;

import edu.uic.ibeis_tourist.PictureDetailActivity;
import edu.uic.ibeis_tourist.R;
import edu.uic.ibeis_tourist.activity_enums.ActivityEnum;
import edu.uic.ibeis_tourist.model.PictureInfo;
import edu.uic.ibeis_tourist.utils.DateTimeUtils;
import edu.uic.ibeis_tourist.utils.ImageUtils;

public class MyPicturesRecyclerViewAdapter extends RecyclerView.Adapter<MyPicturesRecyclerViewAdapter.ViewHolder> {

    public static int PICTURE_LAYOUT_HEIGHT;
    public static int PICTURE_LAYOUT_WIDTH;

    private List<PictureInfo> mPictureInfoList;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public Resources resources;

        public View view;
        public ImageView pictureImageView;
        public TextView speciesTextView;
        public TextView nameTextView;
        public TextView locationTextView;
        public TextView dateTextView;
        public ImageButton favoriteButton;

        public ViewHolder(View view) {
            super(view);
            this.view = view;

            resources = view.getResources();

            pictureImageView = (ImageView) view.findViewById(R.id.item_picture);
            MyPicturesRecyclerViewAdapter.PICTURE_LAYOUT_HEIGHT = ImageUtils.dpToPx(view.getContext(), pictureImageView.getLayoutParams().height);
            MyPicturesRecyclerViewAdapter.PICTURE_LAYOUT_WIDTH = ImageUtils.dpToPx(view.getContext(), pictureImageView.getLayoutParams().width);

            speciesTextView = (TextView) view.findViewById(R.id.item_individual_species);
            nameTextView = (TextView) view.findViewById(R.id.item_individual_name);
            locationTextView = (TextView) view.findViewById(R.id.item_location);
            dateTextView = (TextView) view.findViewById(R.id.item_datetime);
            favoriteButton = (ImageButton) view.findViewById(R.id.item_star_btn);
        }
    }

    public MyPicturesRecyclerViewAdapter(List<PictureInfo> pictureInfoList, Context context) {
        mPictureInfoList = pictureInfoList;
        mContext = context;
    }

    @Override
    public MyPicturesRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.my_pictures_list_item, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyPicturesRecyclerViewAdapter.ViewHolder viewHolder, final int position) {

        Picasso.with(mContext)
                .load(new File((ImageUtils.getImagesMainFolder() + mPictureInfoList.get(position).getFileName())))
                .transform(new CircleTransform())
                .error(R.drawable.ic_no_img_available)
                .fit()
                .into(viewHolder.pictureImageView);

        String species = mPictureInfoList.get(position).getIndividualSpecies().toString().toUpperCase();
        String name = mPictureInfoList.get(position).getIndividualName();
        String location = mPictureInfoList.get(position).getLocation().getName();
        String date = DateTimeUtils.calendarToString
                (mPictureInfoList.get(position).getDateTime(), DateTimeUtils.DateFormat.DATE_ONLY);

        if(species == null) {
            viewHolder.speciesTextView.setVisibility(View.GONE);
        }
        else {
            viewHolder.speciesTextView.setVisibility(View.VISIBLE);
            viewHolder.speciesTextView.setText(species);
        }
        if(name == null) {
            viewHolder.nameTextView.setVisibility(View.GONE);
        }
        else {
            viewHolder.nameTextView.setVisibility(View.VISIBLE);
            viewHolder.nameTextView.setText(name);
        }
        if(location == null) {
            viewHolder.locationTextView.setVisibility(View.GONE);
        }
        else {
            viewHolder.locationTextView.setVisibility(View.VISIBLE);
            viewHolder.locationTextView.setText("@" + location);
        }
        viewHolder.dateTextView.setText(date);

        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pictureDetailIntent = new Intent(v.getContext(), PictureDetailActivity.class);
                pictureDetailIntent.putExtra("callingActivity", ActivityEnum.MyPicturesActivity.getValue());
                pictureDetailIntent.putExtra("pictureInfo", mPictureInfoList.get(position));
                v.getContext().startActivity(pictureDetailIntent);
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

    public class CircleTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size/2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }
}
