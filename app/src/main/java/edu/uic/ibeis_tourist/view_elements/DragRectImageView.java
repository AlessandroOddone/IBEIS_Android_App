package edu.uic.ibeis_tourist.view_elements;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.uic.ibeis_java_api.api.annotation.BoundingBox;
import edu.uic.ibeis_tourist.R;

public class DragRectImageView extends ImageView {

    private boolean init = true;

    private Paint paint;

    private ImagePoint lowerLeftPoint = new ImagePoint(getContext(), R.drawable.point_image);
    private ImagePoint lowerRightPoint = new ImagePoint(getContext(), R.drawable.point_image);
    private ImagePoint upperLeftPoint = new ImagePoint(getContext(), R.drawable.point_image);
    private ImagePoint upperRightPoint = new ImagePoint(getContext(), R.drawable.point_image);

    private List<ImagePoint> imagePoints;
    private static final int LOWER_LEFT_INDEX = 0;
    private static final int LOWER_RIGHT_INDEX = 1;
    private static final int UPPER_LEFT_INDEX = 2;
    private static final int UPPER_RIGHT_INDEX = 3;

    public enum TouchSelection {LOWER_LEFT_POINT, LOWER_RIGHT_POINT, UPPER_LEFT_POINT, UPPER_RIGHT_POINT, AREA, NONE}
    private TouchSelection touchSelection = TouchSelection.NONE;

    private int horizontalBorder;
    private int verticalBorder;

    private int imageOriginalWidth;
    private int imageOriginalHeight;
    private int imageActualWidth;
    private int imageActualHeight;

    private BoundingBox boundingBox;

    private int oldX;
    private int oldY;

    private static final int MIN_RECT_EDGE_LENGTH = 150;
    private static final int POINT_RADIUS_EXTENSION = 85;

    private OnUpCallback mCallback = null;

    public interface OnUpCallback {
        void onRectFinished(BoundingBox boundingBox);
    }

    public DragRectImageView(final Context context) {
        super(context);
        init();
    }

    public DragRectImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragRectImageView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Sets callback for up
     *
     * @param callback {@link OnUpCallback}
     */
    public void setOnUpCallback(OnUpCallback callback) {
        mCallback = callback;
    }

    private void init() {
        //System.out.println("INIT");
        this.setWillNotDraw(false);

        paint = new Paint();
        paint.setColor(getContext().getResources().getColor(R.color.accent));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(12);

        imagePoints = new ArrayList<>();
        imagePoints.add(lowerLeftPoint);
        imagePoints.add(lowerRightPoint);
        imagePoints.add(upperLeftPoint);
        imagePoints.add(upperRightPoint);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //System.out.println("ACTION_DOWN");
                boolean found = false;

                for(int i=0; i<imagePoints.size(); i++) {
                    // calculate the distance of the point of touch from the center of the ball
                    ImagePoint imagePoint = imagePoints.get(i);
                    double distance = Math.sqrt((x - imagePoint.x)*(x - imagePoint.x) + (y - imagePoint.y)*(y - imagePoint.y));

                    if (distance <= imagePoint.getRadius() + POINT_RADIUS_EXTENSION) {
                        switch(i) {
                            case LOWER_LEFT_INDEX:
                                System.out.println("LOWER_LEFT");
                                touchSelection = TouchSelection.LOWER_LEFT_POINT;
                                found = true;
                                break;

                            case LOWER_RIGHT_INDEX:
                                System.out.println("LOWER_RIGHT");
                                touchSelection = TouchSelection.LOWER_RIGHT_POINT;
                                found = true;
                                break;

                            case UPPER_LEFT_INDEX:
                                System.out.println("UPPER_LEFT");
                                touchSelection = TouchSelection.UPPER_LEFT_POINT;
                                found = true;
                                break;

                            case UPPER_RIGHT_INDEX:
                                System.out.println("UPPER_RIGHT");
                                touchSelection = TouchSelection.UPPER_RIGHT_POINT;
                                found = true;
                                break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
                if (touchSelection == TouchSelection.NONE &&
                        x>upperLeftPoint.x && x<upperRightPoint.x &&
                        y>upperLeftPoint.y && y<lowerRightPoint.y) {
                    System.out.println("AREA");
                    oldX = x;
                    oldY = y;
                    System.out.println("oldX = " + oldX);
                    System.out.println("oldY = " + oldY);
                    touchSelection = TouchSelection.AREA;
                }

                break;

            case MotionEvent.ACTION_MOVE:
                //System.out.println("ACTION_MOVE");
                switch (touchSelection) {
                    case LOWER_LEFT_POINT:
                        //System.out.println("LOWER_LEFT");
                        if (x >= horizontalBorder && x <= lowerRightPoint.x - MIN_RECT_EDGE_LENGTH) {
                            lowerLeftPoint.x = x;
                            upperLeftPoint.x = x;
                        }
                        if (y >= upperLeftPoint.y + MIN_RECT_EDGE_LENGTH && y <= verticalBorder + imageActualHeight) {
                            lowerLeftPoint.y = y;
                            lowerRightPoint.y = y;
                        }
                        init = false;
                        invalidate();
                        break;

                    case LOWER_RIGHT_POINT:
                        //System.out.println("LOWER_RIGHT");
                        if (x >= lowerLeftPoint.x + MIN_RECT_EDGE_LENGTH && x <= horizontalBorder + imageActualWidth) {
                            lowerRightPoint.x = x;
                            upperRightPoint.x = x;
                        }
                        if (y >= upperRightPoint.y + MIN_RECT_EDGE_LENGTH && y <= verticalBorder + imageActualHeight) {
                            lowerRightPoint.y = y;
                            lowerLeftPoint.y = y;
                        }
                        init = false;
                        invalidate();
                        break;

                    case UPPER_LEFT_POINT:
                        //System.out.println("UPPER_LEFT");
                        if (x >= horizontalBorder && x <= upperRightPoint.x - MIN_RECT_EDGE_LENGTH) {
                            upperLeftPoint.x = x;
                            lowerLeftPoint.x = x;
                        }
                        if (y >= verticalBorder && y <= lowerLeftPoint.y - MIN_RECT_EDGE_LENGTH) {
                            upperLeftPoint.y = y;
                            upperRightPoint.y = y;
                        }
                        init = false;
                        invalidate();
                        break;

                    case UPPER_RIGHT_POINT:
                        //System.out.println("UPPER_RIGHT");
                        if (x >= upperLeftPoint.x + MIN_RECT_EDGE_LENGTH && x <= horizontalBorder + imageActualWidth) {
                            upperRightPoint.x = x;
                            lowerRightPoint.x = x;
                        }
                        if (y >= verticalBorder && y <= lowerRightPoint.y - MIN_RECT_EDGE_LENGTH) {
                            upperRightPoint.y = y;
                            upperLeftPoint.y = y;
                        }
                        init = false;
                        invalidate();
                        break;

                    case AREA:
                        //System.out.println("AREA");
                        int lowerLeftX = lowerLeftPoint.x + (x - oldX);
                        int lowerLeftY = lowerLeftPoint.y + (y - oldY);
                        int lowerRightX = lowerRightPoint.x + (x - oldX);
                        int lowerRightY = lowerRightPoint.y + (y - oldY);
                        int upperLeftX = upperLeftPoint.x + (x - oldX);
                        int upperLeftY = upperLeftPoint.y + (y - oldY);
                        int upperRightX = upperRightPoint.x + (x - oldX);
                        int upperRightY = upperRightPoint.y + (y - oldY);

                        System.out.println("lowerLeftX = " + lowerLeftX);
                        System.out.println("lowerLeftY = " + lowerLeftY);
                        System.out.println("lowerRightX = " + lowerRightX);
                        System.out.println("lowerRightY = " + lowerRightY);
                        System.out.println("upperLeftX = " + upperLeftX);
                        System.out.println("upperLeftY = " + upperLeftY);
                        System.out.println("upperRightX = " + upperRightX);
                        System.out.println("upperRightY = " + upperRightY);

                        if (lowerLeftX >= horizontalBorder && lowerRightX <= horizontalBorder + imageActualWidth) {
                            System.out.println("IF (1)");
                            lowerLeftPoint.x = lowerLeftX;
                            lowerRightPoint.x = lowerRightX;
                            upperLeftPoint.x = upperLeftX;
                            upperRightPoint.x = upperRightX;
                            oldX = x;
                        }
                        if (upperLeftY >= verticalBorder && lowerLeftY <= verticalBorder + imageActualHeight) {
                            System.out.println("IF (2)");
                            lowerLeftPoint.y = lowerLeftY;
                            lowerRightPoint.y = lowerRightY;
                            upperLeftPoint.y = upperLeftY;
                            upperRightPoint.y = upperRightY;
                            oldY = y;
                        }
                        init = false;
                        invalidate();
                        break;
                }
                break;

            case MotionEvent.ACTION_UP:
                touchSelection = TouchSelection.NONE;
                if (mCallback != null) {
                    if(imageActualWidth != 0 && imageActualHeight != 0) {
                        boundingBox.setX((upperLeftPoint.x - horizontalBorder) * imageOriginalWidth / imageActualWidth);
                        boundingBox.setY((upperLeftPoint.y - verticalBorder) * imageOriginalHeight / imageActualHeight);
                        boundingBox.setW((upperRightPoint.x - upperLeftPoint.x) * imageOriginalWidth / imageActualWidth);
                        boundingBox.setH((lowerLeftPoint.y - upperLeftPoint.y) * imageOriginalHeight / imageActualHeight);
                        mCallback.onRectFinished(boundingBox);
                    }
                }
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        //System.out.println("ON_DRAW");
        super.onDraw(canvas);

        // Calculate horizontal and vertical border
        horizontalBorder = (this.getWidth() - imageActualWidth)/2;
        verticalBorder = (this.getHeight() - imageActualHeight)/2;

        //System.out.println("width = " + this.getWidth());
        //System.out.println("height = " + this.getHeight());
        //System.out.println("horizontalBorder = " + horizontalBorder);
        //System.out.println("verticalBorder = " + verticalBorder);

        if(init) {
            lowerLeftPoint.set(horizontalBorder, imageActualHeight + verticalBorder);
            lowerRightPoint.set(imageActualWidth + horizontalBorder,
                    imageActualHeight + verticalBorder);
            upperLeftPoint.set(horizontalBorder, verticalBorder);
            upperRightPoint.set(imageActualWidth + horizontalBorder, verticalBorder);

            boundingBox = new BoundingBox();
            boundingBox.setX((upperLeftPoint.x - horizontalBorder) * imageOriginalWidth / imageActualWidth);
            boundingBox.setY((upperLeftPoint.y - verticalBorder) * imageOriginalHeight / imageActualHeight);
            boundingBox.setW((upperRightPoint.x - upperLeftPoint.x) * imageOriginalWidth / imageActualWidth);
            boundingBox.setH((lowerLeftPoint.y - upperLeftPoint.y) * imageOriginalHeight / imageActualHeight);
            mCallback.onRectFinished(boundingBox);
        }

        //System.out.println("lowerLeftPoint.x = " + lowerLeftPoint.x);
        //System.out.println("upperLeftPoint.y = " + upperLeftPoint.y);
        //System.out.println("lowerRightPoint.x = " + lowerRightPoint.x);
        //System.out.println("lowerLeftPoint.y = " + lowerLeftPoint.y);

        canvas.drawRect(lowerLeftPoint.x, upperLeftPoint.y, lowerRightPoint.x, lowerLeftPoint.y, paint);

        for(ImagePoint imagePoint : Arrays.asList(lowerLeftPoint,lowerRightPoint,upperLeftPoint,upperRightPoint)) {
            imagePoint.drawPoint(canvas, paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //System.out.println("ON_MEASURE");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Get image matrix values and place them in an array
        float[] f = new float[9];
        getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = getDrawable();
        imageOriginalWidth = d.getIntrinsicWidth();
        imageOriginalHeight = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        imageActualWidth = Math.round(imageOriginalWidth * scaleX);
        imageActualHeight = Math.round(imageOriginalHeight * scaleY);

        invalidate();

        //System.out.println("imageOriginalWidth = " + imageOriginalWidth);
        //System.out.println("imageOriginalHeight = " + imageOriginalHeight);
        //System.out.println("imageActualWidth = " + imageActualWidth);
        //System.out.println("imageActualHeight = " + imageActualHeight);
    }
}
