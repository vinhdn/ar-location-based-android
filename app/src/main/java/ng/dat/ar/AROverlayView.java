package ng.dat.ar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ng.dat.ar.helper.LocationHelper;
import ng.dat.ar.model.ARPoint;

/**
 * Created by ntdat on 1/13/17.
 */

public class AROverlayView extends View {

    Context context;
    private float[] rotatedProjectionMatrix = new float[16];
    private Location currentLocation;
    private List<ARPoint> arPoints;
    private int canvasWidth = 0;
    private int canvasHeight = 0;


    public AROverlayView(Context context) {
        super(context);

        this.context = context;

        //Demo points
        arPoints = new ArrayList<ARPoint>() {{
            add(new ARPoint("Sun Wheel", 16.0404856, 108.2262447, 0));
            add(new ARPoint("Linh Ung Pagoda", 16.1072989, 108.2343984, 0));
        }};
    }

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentLocation(Location currentLocation){
        this.currentLocation = currentLocation;
        this.invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            return true;
        }if(event.getAction() == MotionEvent.ACTION_UP){
            Log.d("TouchEven ", "ID = " + checkTouchOn(event));
            return false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentLocation == null) {
            return;
        }

        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();

        final int radius = 30;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(60);

        Paint paintBG = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBG.setStyle(Paint.Style.FILL);
        paintBG.setColor(Color.parseColor("#607D8B"));
        paintBG.setShadowLayer(10.0f, 0.0f, 2.0f, 0xFF000000);
        paintBG.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paintBG.setTextSize(60);

        for (int i = 0; i < arPoints.size(); i ++) {
            float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
            float[] pointInECEF = LocationHelper.WSG84toECEF(arPoints.get(i).getLocation());
            float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

            // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
            // if z > 0, the point will display on the opposite
            if (cameraCoordinateVector[2] < 0) {
                float x  = (0.5f + cameraCoordinateVector[0]/cameraCoordinateVector[3]) * canvas.getWidth();
                float y = (0.5f - cameraCoordinateVector[1]/cameraCoordinateVector[3]) * canvas.getHeight();

                float left = (x - (30 * arPoints.get(i).getName().length() / 2)) - 80;
                float top = y + 80;
                float right = left + 30 * arPoints.get(i).getName().length() + 80 * 2;
                float bottom = y - 80 * 2;
                Rect rect = new Rect((int)left,(int)top,(int)right, (int)bottom);
                canvas.drawRect(rect, paintBG);
                canvas.drawCircle(x, y, radius, paint);
                canvas.drawText(arPoints.get(i).getName(), x - (30 * arPoints.get(i).getName().length() / 2), y - 80, paint);
            }
        }
    }

    private int checkTouchOn(MotionEvent event){
        if(canvasHeight == 0 || canvasWidth == 0) return -1;
        int ey = (int) event.getY();
        int ex = (int) event.getX();
        for (int i = arPoints.size() - 1; i >=0; i --) {
            float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
            float[] pointInECEF = LocationHelper.WSG84toECEF(arPoints.get(i).getLocation());
            float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

            // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
            // if z > 0, the point will display on the opposite
            if (cameraCoordinateVector[2] < 0) {
                float x  = (0.5f + cameraCoordinateVector[0]/cameraCoordinateVector[3]) * canvasWidth;
                float y = (0.5f - cameraCoordinateVector[1]/cameraCoordinateVector[3]) * canvasHeight;

                float left = (x - (30 * arPoints.get(i).getName().length() / 2)) - 80;
                float top = y + 80;
                float right = left + 30 * arPoints.get(i).getName().length() + 80 * 2;
                float bottom = y - 80 * 2;
                Rect rect = new Rect((int)left,(int)top,(int)right, (int)bottom);
                if(ey < top && ey > bottom && ex > left && ex < right){
                    return i;
                }
            }
        }
        return -1;
    }
}
