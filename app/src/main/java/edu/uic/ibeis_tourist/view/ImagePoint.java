package edu.uic.ibeis_tourist.view;

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
        radius = Math.sqrt(bitmap.getWidth()*bitmap.getWidth() + bitmap.getHeight()*bitmap.getHeight());
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
