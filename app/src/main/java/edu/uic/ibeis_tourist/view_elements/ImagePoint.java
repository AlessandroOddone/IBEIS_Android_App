package edu.uic.ibeis_tourist.view_elements;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

public class ImagePoint extends Point {

    private Bitmap bitmap;
    private double radius;

    public ImagePoint(Context context, int res, int x, int y) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        bitmap = BitmapFactory.decodeResource(context.getResources(), res);
        radius = Math.sqrt((bitmap.getWidth()/2)*(bitmap.getWidth()/2) + (bitmap.getHeight()/2)*(bitmap.getHeight()/2));
        this.set(x, y);
    }

    public ImagePoint(Context context, int res) {
        this(context, res, 0, 0);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public double getRadius() {
        return radius;
    }

    public void drawPoint(Canvas canvas, Paint paint) {
        canvas.drawBitmap(bitmap, this.x - bitmap.getWidth()/2,
                this.y - bitmap.getHeight()/2, paint);
    }
}
