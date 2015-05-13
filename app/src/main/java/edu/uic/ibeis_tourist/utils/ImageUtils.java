package edu.uic.ibeis_tourist.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.uic.ibeis_tourist.exceptions.ImageLoadingException;

public class ImageUtils {

    public static final String PATH_TO_IMAGE_FILE = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "IBEIS" + File.separator;

    /**
     * Generate a unique file path for a new image file to be stored in the external memory
     * @return Generated file
     */
    public static File generateImageFile(String fileName) {
        File ibeisDir = new File(PATH_TO_IMAGE_FILE);
        if(!ibeisDir.exists()) {
            ibeisDir.mkdir();
        }
        return new File(PATH_TO_IMAGE_FILE + fileName);
    }

    /**
     * Generate a unique name for an image file
     * @return Name of the file
     */
    public static String generateImageName() {
        return "IBEIS_" + new SimpleDateFormat("yyyyMMdd'_'hhmmss").format(new Date()) + ".png";
    }


    /**
     * Returns a rectangular bitmap from an image file contained in the app folder in the external memory
     * @param fileName
     * @param containerHeight height of bitmap container in the layout
     * @param containerWidth width of bitmap container in the layout
     * @return
     * @throws ImageLoadingException
     */
    public static Bitmap getRectangularBitmap(String fileName, int containerHeight, int containerWidth) throws ImageLoadingException {
        try {
            return getBitmap(fileName, containerHeight, containerWidth, Bitmap.Config.ARGB_8888);
        } catch (IOException e) {
            throw new ImageLoadingException();
        }
    }

    /**
     *
     * Returns a circular bitmap from an image file contained in the app folder in the external memory
     * @param fileName
     * @param containerHeight height of bitmap container in the layout
     * @param containerWidth width of bitmap container in the layout
     * @return
     * @throws ImageLoadingException
     */
    public static Bitmap getCircularBitmap(String fileName, int containerHeight, int containerWidth) throws ImageLoadingException {
        try {
            Bitmap rectangularBitmap = getBitmap(fileName, containerHeight, containerWidth, Bitmap.Config.ARGB_8888);
            return cropToCircle(rectangularBitmap);
        } catch (IOException e) {
            throw new ImageLoadingException();
        }
    }

    /**
     * Convert dp to px
     * @param context
     * @param dp
     * @return
     */
    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int)((dp * displayMetrics.density) + 0.5);
    }

    /**
     * Convert px to dp
     * @param context
     * @param px
     * @return
     */
    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) ((px/displayMetrics.density)+0.5);
    }

    /**
     * Returns a bitmap from an image file contained in the app folder in the external memory (the shape of the image is not modified)
     * @param fileName
     * @return image Bitmap
     * @throws IOException
     */
    private static Bitmap getBitmap(String fileName, int requestedHeight, int requestedWidth,
                             Bitmap.Config colorConfig) throws IOException {

        String filePath = PATH_TO_IMAGE_FILE + fileName;
        if (!(new File(filePath)).exists()) {
            throw new FileNotFoundException();
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        final int height = options.outHeight;
        final int width = options.outWidth;

        options.inPreferredConfig = colorConfig;
        int inSampleSize = 1;

        if (height > requestedHeight)
        {
            inSampleSize = Math.round((float)height / (float)requestedHeight);
        }
        int expectedWidth = width / inSampleSize;

        if (expectedWidth > requestedWidth)
        {
            inSampleSize = Math.round((float)width / (float)requestedWidth);
        }

        options.inSampleSize = inSampleSize;

        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

        ExifInterface exif = new ExifInterface(filePath);
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

        Matrix matrix = new Matrix();
        if (orientation == 6) {
            matrix.postRotate(90);
        }
        else if (orientation == 3) {
            matrix.postRotate(180);
        }
        else if (orientation == 8) {
            matrix.postRotate(270);
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    /**
     * Crops a rectangular bitmap to generate a circular bitmap
     * @param rectangularBitmap
     * @return Circular Bitmap
     */
    private static Bitmap cropToCircle(Bitmap rectangularBitmap) {
        Bitmap output;

        if (rectangularBitmap.getWidth() > rectangularBitmap.getHeight()) {
            output = Bitmap.createBitmap(rectangularBitmap.getHeight(), rectangularBitmap.getHeight(), rectangularBitmap.getConfig());
        } else {
            output = Bitmap.createBitmap(rectangularBitmap.getWidth(), rectangularBitmap.getWidth(), rectangularBitmap.getConfig());
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, rectangularBitmap.getWidth(), rectangularBitmap.getHeight());

        float r = 0;

        if (rectangularBitmap.getWidth() > rectangularBitmap.getHeight()) {
            r = rectangularBitmap.getHeight() / 2;
        } else {
            r = rectangularBitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(rectangularBitmap, rect, rect, paint);
        return output;
    }
}
