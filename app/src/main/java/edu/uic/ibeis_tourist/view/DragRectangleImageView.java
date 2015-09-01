package edu.uic.ibeis_tourist.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.uic.ibeis_tourist.R;

public class DragRectangleImageView extends ImageView {

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

    public enum SelectedPoint {LOWER_LEFT, LOWER_RIGHT, UPPER_LEFT, UPPER_RIGHT, NONE}
    private SelectedPoint selectedPoint = SelectedPoint.NONE;

    private int horizontalBorder;
    private int verticalBorder;

    private int imageOriginalWidth;
    private int imageOriginalHeight;
    private int imageActualWidth;
    private int imageActualHeight;

    private static final int MIN_RECT_EDGE_LENGTH = 125;
    private static final int POINT_RADIUS_EXTENSION = 20;

    private OnUpCallback mCallback = null;

    public interface OnUpCallback {
        void onRectFinished(Rect rect);
    }

    public DragRectangleImageView(final Context context) {
        super(context);
        init();
    }

    public DragRectangleImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragRectangleImageView(final Context context, final AttributeSet attrs, final int defStyle) {
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
        System.out.println("INIT");
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
        System.out.println();

        int x = (int)event.getX();
        int y = (int)event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                System.out.println("ACTION_DOWN");
                for(int i=0; i<imagePoints.size(); i++) {
                    // calculate the distance of the point of touch from the center of the ball
                    ImagePoint imagePoint = imagePoints.get(i);
                    double distance = Math.sqrt((x - imagePoint.x)*(x - imagePoint.x) + (y - imagePoint.y)*(y - imagePoint.y));
                    if(distance <= imagePoint.getRadius() + POINT_RADIUS_EXTENSION) {
                        switch(i) {
                            case LOWER_LEFT_INDEX:
                                System.out.println("LOWER_LEFT");
                                selectedPoint = SelectedPoint.LOWER_LEFT;
                                break;

                            case LOWER_RIGHT_INDEX:
                                System.out.println("LOWER_RIGHT");
                                selectedPoint = SelectedPoint.LOWER_RIGHT;
                                break;

                            case UPPER_LEFT_INDEX:
                                System.out.println("UPPER_LEFT");
                                selectedPoint = SelectedPoint.UPPER_LEFT;
                                break;

                            case UPPER_RIGHT_INDEX:
                                System.out.println("UPPER_RIGHT");
                                selectedPoint = SelectedPoint.UPPER_RIGHT;
                                break;
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                System.out.println("ACTION_MOVE");
                switch (selectedPoint) {
                    case LOWER_LEFT:
                        System.out.println("LOWER_LEFT");
                        if (x >= horizontalBorder && x <= lowerRightPoint.x - MIN_RECT_EDGE_LENGTH &&
                                y >= upperLeftPoint.y + MIN_RECT_EDGE_LENGTH && y <= verticalBorder + imageActualHeight) {
                            lowerLeftPoint.set(x,y);
                            lowerRightPoint.y = y;
                            upperLeftPoint.x = x;
                            init = false;
                            invalidate();
                        }
                        break;

                    case LOWER_RIGHT:
                        System.out.println("LOWER_RIGHT");
                        if (x >= lowerLeftPoint.x + MIN_RECT_EDGE_LENGTH && x <= horizontalBorder + imageActualWidth &&
                                y >= upperRightPoint.y + MIN_RECT_EDGE_LENGTH && y <= verticalBorder + imageActualHeight) {
                            lowerRightPoint.set(x, y);
                            lowerLeftPoint.y = y;
                            upperRightPoint.x = x;
                            init = false;
                            invalidate();
                        }
                        break;

                    case UPPER_LEFT:
                        if (x >= horizontalBorder && x <= upperRightPoint.x - MIN_RECT_EDGE_LENGTH &&
                                y >= verticalBorder && y <= lowerLeftPoint.y - MIN_RECT_EDGE_LENGTH) {
                            upperLeftPoint.set(x, y);
                            upperRightPoint.y = y;
                            lowerLeftPoint.x = x;
                            init = false;
                            invalidate();
                        }
                        break;

                    case UPPER_RIGHT:
                        System.out.println("UPPER_RIGHT");
                        if (x >= upperLeftPoint.x + MIN_RECT_EDGE_LENGTH && x <= horizontalBorder + imageActualWidth &&
                                y >= verticalBorder && y <= lowerRightPoint.y - MIN_RECT_EDGE_LENGTH) {
                            upperRightPoint.set(x, y);
                            upperLeftPoint.y = y;
                            lowerRightPoint.x = x;
                            init = false;
                            invalidate();
                        }
                        break;
                }
                break;

            case MotionEvent.ACTION_UP:
                selectedPoint = SelectedPoint.NONE;
                /*
                if (mCallback != null) {
                    mCallback.onRectFinished(new Rect(Math.min(startX, endX), Math.min(startY, endY),
                            Math.max(endX, startX), Math.max(endY, startX)));
                }
                invalidate();
                break;
                */

            default:
                break;
        }

        return true;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        System.out.println("ON_DRAW");
        super.onDraw(canvas);

        // Calculate horizontal and vertical border
        horizontalBorder = (this.getWidth() - imageActualWidth)/2;
        verticalBorder = (this.getHeight() - imageActualHeight)/2;

        //System.out.println("horizontalBorder = " + horizontalBorder);
        //System.out.println("verticalBorder = " + verticalBorder);

        if(init) {
            lowerLeftPoint.set(horizontalBorder, imageActualHeight + verticalBorder);
            lowerRightPoint.set(imageActualWidth + horizontalBorder,
                    imageActualHeight + verticalBorder);
            upperLeftPoint.set(horizontalBorder, verticalBorder);
            upperRightPoint.set(imageActualWidth + horizontalBorder, verticalBorder);
        }

        System.out.println("lowerLeftPoint.x = " + lowerLeftPoint.x);
        System.out.println("upperLeftPoint.y = " + upperLeftPoint.y);
        System.out.println("lowerRightPoint.x = " + lowerRightPoint.x);
        System.out.println("lowerLeftPoint.y = " + lowerLeftPoint.y);

        canvas.drawRect(lowerLeftPoint.x, upperLeftPoint.y, lowerRightPoint.x, lowerLeftPoint.y, paint);

        for(ImagePoint imagePoint : Arrays.asList(lowerLeftPoint,lowerRightPoint,upperLeftPoint,upperRightPoint)) {
            imagePoint.drawPoint(canvas, paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        System.out.println("ON_MEASURE");
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
