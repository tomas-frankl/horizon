package com.franktom.horizon;

//import com.franktom.horizon.CompassActivity.CompassView.Item;
//import com.franktom.horizon.CompassActivity.CompassView.ItemComparator;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
//import android.location.Address;
//import android.location.Geocoder;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
//import android.hardware.SensorManager;
//import android.location.LocationManager;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.location.Address;
//import android.location.Geocoder;
//import android.location.Location;
//import android.location.LocationListener;

//import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.lang.Math;
//import java.util.List;
//import java.util.Locale;

class CompassView extends View implements SensorEventListener/*###, LocationListener*/ {

    private float mXOrigin;
    private float mYOrigin;

    private float mViewDirection;

    private boolean mUpdateCurrentLocation = true;
    private Location mCurrentLocation;
    MyLocation my_location;

    private float m_lastAccels[];
    private float m_lastMagFields[];
    private float m_rotationMatrix[];
    private float m_orientation[];

    static final float ALPHA = 0.005f;

    static final float mDistanceLimit0 = (float)15000.0;
    static final float mDistanceLimit1 = (float)10000.0;
    static final float mElevationLimit1 = (float)0.0;
    static final float mDistanceLimit2 = (float)20000.0;
    static final float mElevationLimit2 = (float)600.0;
    static final float mDistanceLimit3 = (float)40000.0;
    static final float mElevationLimit3 = (float)1000.0;
    static final float mHideOverlappingLimit = (float)2.0;
    static final boolean mDrawHelperLines = false;

    boolean mFrozen = false;

    protected float accelVal;

    Paint mPaint;
    Paint mPaintScale;
    Paint mPaintBackground;
    Paint mPaintObjects;
    Paint mPaintObjectsLines;
    Paint mPaintLocationData;
    Cursor cursor;
    private SensorManager mSensorManager;
    //###private LocationManager mLocationManager;

    public MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
        @Override
        public void gotLocation(final Location location){
            if (mUpdateCurrentLocation) {
                mCurrentLocation = location;
            reloadObjects(true);
            }
        }
    };

    void setCurrentLocation(Location loc)
    {
        mUpdateCurrentLocation = false;
        mCurrentLocation = new Location(loc);
        reloadObjects(true);
    }

    Location getCurrentLocation()
    {
        return mCurrentLocation;
    }

    void updateCurrentLocation()
    {
        mUpdateCurrentLocation = true;
        mCurrentLocation = MyLocation.getLocation(getContext());
        reloadObjects(true);
    }

    class Item {
        int position;
        float angle;
        float elevation;
        boolean hidden;

        Item(int position, float angle, float elevation) {
            this.position = position;
            this.angle = angle;
            this.elevation = elevation;
            this.hidden = false;
        }

    };

    class ItemComparator implements Comparator<Item>
    {
        public int compare(Item i1, Item i2) {
            if (i1.angle < i2.angle)
                return -1;
            if (i1.angle > i2.angle)
                return 1;
            return 0;
        }

    }

    ArrayList<Item> mItemsToShow;

    public float DEFAULT_CAMERA_VIEW_ANGLE_HORIZONTAL = 55;
    public float CAMERA_VIEW_ANGLE_HORIZONTAL = DEFAULT_CAMERA_VIEW_ANGLE_HORIZONTAL;
    public float CAMERA_VIEW_ANGLE_VERTICAL = 10;
    private static final int DISPLAY_ANGLE_STEP = 10;

    public CompassView(Context context) {
        super(context);
//        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager = (SensorManager)((Activity)context).getSystemService(Context.SENSOR_SERVICE);
        //###mLocationManager = (LocationManager)((Activity)context).getSystemService(Context.LOCATION_SERVICE);
        mItemsToShow = new ArrayList<Item>();

        m_lastAccels = new float[16];
        m_lastMagFields = new float[16];
        m_rotationMatrix = new float[16];
        m_orientation = new float[3];


        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(metrics);


        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setTextAlign(Align.CENTER);

        mPaintScale = new Paint();
        mPaintScale.setColor(Color.YELLOW);
        mPaintScale.setTextAlign(Align.CENTER);

        mPaintBackground = new Paint();
        //mPaintBackground.setColor(Color.WHITE);
        mPaintBackground.setARGB(128, 0, 0, 0);

        mPaintObjects = new Paint();
        mPaintObjects.setColor(Color.WHITE);
        mPaintObjects.setTextSize(20);
        mPaintObjects.setTextAlign(Align.LEFT);

        mPaintObjectsLines = new Paint();
        mPaintObjectsLines.setColor(Color.GREEN); //WHITE
        mPaintObjectsLines.setTextSize(20);
        mPaintObjectsLines.setTextAlign(Align.LEFT);

        mPaintLocationData = new Paint();
        mPaintLocationData.setColor(Color.GREEN);
        mPaintLocationData.setTextSize(15);
        mPaintLocationData.setTextAlign(Align.CENTER);


        setBackgroundColor(Color.TRANSPARENT);

        mCurrentLocation = new Location("0.0 0.0");
        my_location = new MyLocation();
        my_location.init(getContext(), locationResult);


        /*String[] mProjection =
        {
            "_id",
            "NAME",
            "LATITUDE",
            "LONGITUDE",
            "ELEVATION"
        };
        Uri uri = Uri.parse("content://" + LocationsProvider.AUTHORITY + "/" + "locations");
        cursor = ((Activity)context).getContentResolver().query(uri, mProjection, null, null, null);
        */
    }

    public float getViewDirection() {
        return mViewDirection;
    }

    public void setViewDirection(float mViewDirection) {
        this.mViewDirection = mViewDirection;
        if (this.mViewDirection>=180)
            this.mViewDirection = this.mViewDirection - 360;
        if (this.mViewDirection<-180)
            this.mViewDirection = this.mViewDirection + 360;
        invalidate();
    }

    public void startSimulation() {
        Sensor magnetField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, magnetField, SensorManager.SENSOR_DELAY_NORMAL);
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        /*###
        Location locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationPassive = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        System.out.println("NETWORK_PROVIDER: " + locationNet.toString());
        System.out.println("GPS_PROVIDER: " + locationGPS.toString());
        System.out.println("PASSIVE_PROVIDER: " + locationPassive.toString());*/

        /*if (locationGPS.getTime() > locationNet.getTime() && locationGPS.hasAccuracy())
            onLocationChanged(locationGPS);
        else
            onLocationChanged(locationNet);*/

        //###onLocationChanged(locationGPS.getAccuracy() == Criteria.ACCURACY_FINE ? locationGPS : locationNet);

        reloadObjects(true);

        //###mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 1, this);
        //###mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 1, this);
    }

    public void stopSimulation() {
        mSensorManager.unregisterListener(this);
//###        mLocationManager.removeUpdates(this);
    }

    float convertTo360Dg(float value)
    {
        if (value<=0) return value+360;
        if (value>360) return value-360;
        return value;
    }

    protected float lowPass( float newValue, float oldValue ) {
        newValue = newValue + (float)90.0;
        //return output + ALPHA * (input - output);
        float delta = newValue - oldValue;

        if (delta > 180)
            delta = delta - 360;
        if (delta < -180)
            delta = delta + 360;

        float newOutput = oldValue + ALPHA * (delta);

        if (newOutput < -180)
            newOutput = newOutput + 360;
        if (newOutput >= 180)
            newOutput = newOutput - 360;
        return newOutput;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // compute the origin of the screen relative to the origin of
        // the bitmap
        mXOrigin = w;
        mYOrigin = h;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mFrozen) {
            return;
        }

        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE){
            return;
        }

        /*if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            mViewDirection = lowPass( event.values[0], mViewDirection );
            invalidate();
        }*/

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, m_lastAccels, 0, 3);
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, m_lastMagFields, 0, 3);
        }

        if (SensorManager.getRotationMatrix(m_rotationMatrix, null, m_lastAccels, m_lastMagFields)) {
            SensorManager.getOrientation(m_rotationMatrix, m_orientation);

            mViewDirection = lowPass( (float)Math.toDegrees(m_orientation[0]), mViewDirection );

            invalidate();
        }

        /*
         * record the accelerometer data, the event's timestamp as well as
         * the current time. The latter is needed so we can calculate the
         * "present" time during rendering. In this application, we need to
         * take into account how the screen is rotated with respect to the
         * sensors (which always return data in a coordinate space aligned
         * to with the screen in its native orientation).
         */
/*
        switch (mDisplay.getRotation()) {
            case Surface.ROTATION_0:
                mSensorX = event.values[0];
                mSensorY = event.values[1];
                break;
            case Surface.ROTATION_90:
                mSensorX = -event.values[1];
                mSensorY = event.values[0];
                break;
            case Surface.ROTATION_180:
                mSensorX = -event.values[0];
                mSensorY = -event.values[1];
                break;
            case Surface.ROTATION_270:
                mSensorX = event.values[1];
                mSensorY = -event.values[0];
                break;
        }

        mSensorTimeStamp = event.timestamp;
        mCpuTimeStamp = System.nanoTime();
*/
    }

    /*@Override
    public void onLocationChanged(Location loc) {
        mCurrentLocation = new Location(loc);
        mCurrentLocation.setLatitude(49.830);
        mCurrentLocation.setLongitude(18.278);
        mCurrentLocation.setAltitude(300.0);

        mTmpLocation = loc;
    }*/

    public void reloadObjects(boolean queryDatabase)
    {
        Location mTmpLocation = new Location(mCurrentLocation);

        if (cursor==null || queryDatabase)
        {
            mItemsToShow.clear();


            double meridianDistance = 3.14/180.0*6378.0*Math.cos(mCurrentLocation.getLatitude());
            double parallelDistance = Math.sin(3.14/180.0)*6378.0;

            double range = 50;//km
            double meridianDistanceInDg = range/meridianDistance;
            double parallelDistanceInDg = range/parallelDistance;



            Uri uri = Uri.parse("content://" + LocationsProvider.AUTHORITY + "/" + "locations");
            String[] projection =
            {
                "_id",
                "NAME",
                "LATITUDE",
                "LONGITUDE",
                "ELEVATION"
            };

            //StringBuilder selection;
            String selection =
                    String.format("LATITUDE>%1.2f AND LATITUDE<%1.2f AND LONGITUDE>%1.2f AND LONGITUDE<%1.2f",
                            mCurrentLocation.getLatitude()-parallelDistanceInDg,
                            mCurrentLocation.getLatitude()+parallelDistanceInDg,
                            mCurrentLocation.getLongitude()-meridianDistanceInDg,
                            mCurrentLocation.getLongitude()+meridianDistanceInDg);

            cursor = ((Activity)getContext()).getContentResolver().query(uri, projection, selection.replace(',', '.'), null, null);

            cursor.moveToFirst();
            for( int i=0; i<cursor.getCount(); i++)
            {
                float latitude = cursor.getFloat(2);
                float longitude = cursor.getFloat(3);
                float elevation = cursor.getFloat(4);

                mTmpLocation.setLatitude(latitude);
                mTmpLocation.setLongitude(longitude);

                float objectDistance= mCurrentLocation.distanceTo(mTmpLocation);
                float objectBearing= mCurrentLocation.bearingTo(mTmpLocation);

                if ( (objectDistance<mDistanceLimit0 && elevation == 0) ||
                     (objectDistance<mDistanceLimit1 && elevation>mElevationLimit1) ||
                     (objectDistance<mDistanceLimit2 && elevation>mElevationLimit2) ||
                     (objectDistance<mDistanceLimit3 && elevation>mElevationLimit3)) {
                    mItemsToShow.add(new Item(cursor.getPosition(), objectBearing, elevation));
                }

                cursor.moveToNext();
            }

            Collections.sort(mItemsToShow, new ItemComparator());
        }

        float lastAzimuth = 10000;
        float lastElevation = 10000;
        int lastPosition = -1;

        /*for (int i = mItemsToShow.size()-1; i>=0 ; i--) {
            Item item = mItemsToShow.get(i);
            cursor.moveToPosition(item.position);
            String name = cursor.getString(1);
            System.out.println("A (" + name + ") - Pos:" + item.position + " Azimuth:" + item.angle);
        }*/

        float overlappingLimit = mHideOverlappingLimit * CAMERA_VIEW_ANGLE_HORIZONTAL / DEFAULT_CAMERA_VIEW_ANGLE_HORIZONTAL;
        for (int i = mItemsToShow.size()-1; i>=0 ; i--) {
            Item item = mItemsToShow.get(i);
            mItemsToShow.get(i).hidden = false;

            if (lastAzimuth - item.angle < overlappingLimit) {
                if (item.elevation > lastElevation ) {
                    mItemsToShow.get(lastPosition).hidden = true;
                    lastPosition = i;
                    lastAzimuth = mItemsToShow.get(i).angle;
                    lastElevation = mItemsToShow.get(i).elevation;
                    //mItemsToShow.remove(lastPosition);
                }
                else {
                    mItemsToShow.get(i).hidden = true;
                    //lastPosition = i;
                    //lastAzimuth = mItemsToShow.get(i).angle;
                    //lastElevation = mItemsToShow.get(i).elevation;
                    //mItemsToShow.remove(i);
                }
            }
            else
            {
                lastPosition = i;
                lastAzimuth = mItemsToShow.get(i).angle;
                lastElevation = mItemsToShow.get(i).elevation;
            }
        }

        /*for (int i = mItemsToShow.size()-1; i>=0 ; i--) {
            Item item = mItemsToShow.get(i);
            cursor.moveToPosition(item.position);
            String name = cursor.getString(1);
            System.out.println("B (" + name + ") - Pos:" + item.position + " Azimuth:" + item.angle);
        }*/

        invalidate();
    }

    /*###
    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}*/

    private static final int HEADER_HEIGHT = 30; //%
    private static final int SCALE_HEIGHT = 1; //%
    private static final int MARKER_SIZE = 15; //pts
    private static final int SMALL_MARKER_SIZE = 10; //pts

    @Override
    protected void onDraw(Canvas canvas) {

        final float xc = mXOrigin/2;
        final float yc = mYOrigin/2;


        //background
        canvas.drawRect(0, 0, mXOrigin, mYOrigin/100*HEADER_HEIGHT, mPaintBackground);

        //center lines
        if (mDrawHelperLines) {
            canvas.drawLine(xc, mYOrigin/100*HEADER_HEIGHT, xc, mYOrigin/100*100, mPaint);
            canvas.drawLine(0, yc, mXOrigin, yc, mPaint);
        }

        double mTopAngle  = (CAMERA_VIEW_ANGLE_VERTICAL/2);
        double mBottomAngle  = -(CAMERA_VIEW_ANGLE_VERTICAL/2);

        float mLeftAngle = mViewDirection - (CAMERA_VIEW_ANGLE_HORIZONTAL/2);
        float mRightAngle = mViewDirection + (CAMERA_VIEW_ANGLE_HORIZONTAL/2);
        int start = Math.round(mLeftAngle  / DISPLAY_ANGLE_STEP) * DISPLAY_ANGLE_STEP;
        int end   = Math.round(mRightAngle / DISPLAY_ANGLE_STEP) * DISPLAY_ANGLE_STEP + DISPLAY_ANGLE_STEP;

        for (int i = start; i < end; i += DISPLAY_ANGLE_STEP)
        {
            float xPos = ((i-mLeftAngle)/CAMERA_VIEW_ANGLE_HORIZONTAL) * mXOrigin;
            int displayedAzimuthValue = i;
            if (displayedAzimuthValue < 0) displayedAzimuthValue = displayedAzimuthValue + 360;
            if (displayedAzimuthValue >= 360) displayedAzimuthValue = displayedAzimuthValue - 360;
            canvas.drawLine(xPos, mYOrigin/100*HEADER_HEIGHT, xPos, mYOrigin/100*(HEADER_HEIGHT-SCALE_HEIGHT), mPaintScale);
            canvas.drawText(String.format("%03d",displayedAzimuthValue), xPos, mYOrigin/100*(HEADER_HEIGHT-SCALE_HEIGHT), mPaintScale);
        }

        canvas.drawText(
                String.format("Az:%03.1f Lat:%3.7f Lon%3.7f",
                        (mViewDirection < 0) ? mViewDirection + 360 : mViewDirection,
                        mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude())
                , xc, mYOrigin/100*2, mPaintLocationData);


        Location mTmpLocation = new Location(mCurrentLocation);

        Iterator<Item> it = mItemsToShow.iterator();
        while (it.hasNext())
        {
            Item item = it.next();
            cursor.moveToPosition(item.position);

            String name = cursor.getString(1);
            float latitude = cursor.getFloat(2);
            float longitude = cursor.getFloat(3);
            float elevation = cursor.getFloat(4);
            boolean hidden = item.hidden;


            mTmpLocation.setLatitude(latitude);
            mTmpLocation.setLongitude(longitude);
            mTmpLocation.setAltitude(elevation);

            float objectDistance = mCurrentLocation.distanceTo(mTmpLocation);
            float objectAngleH = mCurrentLocation.bearingTo(mTmpLocation);
            double objectAngleV = Math.atan((mTmpLocation.getAltitude()-mCurrentLocation.getAltitude())/objectDistance) / Math.PI * 180;

            //if (objectAngleH < mLeftAngle || objectAngleH > mRightAngle)
                //continue;
            /*if (Math.abs(objectAngleH - mViewDirection) > CAMERA_VIEW_ANGLE_HORIZONTAL / 2){
                continue;
            }###*/

            double lineEnd = yc - (objectAngleV/CAMERA_VIEW_ANGLE_VERTICAL*mYOrigin);
            if (lineEnd < mYOrigin/100*(HEADER_HEIGHT+10))
                lineEnd = mYOrigin/100*(HEADER_HEIGHT+10);

            if (mTmpLocation.getAltitude()==0)
                lineEnd = mYOrigin;

            /*float x = (objectAngleH-mLeftAngle);
            if (x<=360) x+=360;
            if (x>360) x-=360;*/
            float objectXPos = (convertTo360Dg(objectAngleH-mLeftAngle)/CAMERA_VIEW_ANGLE_HORIZONTAL) * mXOrigin;
            //object marker
            if  (item.hidden) {
                canvas.drawLine(objectXPos, mYOrigin/100*HEADER_HEIGHT, objectXPos, mYOrigin/100*HEADER_HEIGHT-SMALL_MARKER_SIZE, mPaintObjectsLines);
            }
            else {
                canvas.drawLine(objectXPos, mYOrigin/100*HEADER_HEIGHT, objectXPos-MARKER_SIZE, mYOrigin/100*HEADER_HEIGHT-MARKER_SIZE, mPaintObjectsLines);
                canvas.drawLine(objectXPos, mYOrigin/100*HEADER_HEIGHT, objectXPos+MARKER_SIZE, mYOrigin/100*HEADER_HEIGHT-MARKER_SIZE, mPaintObjectsLines);
                canvas.drawLine(objectXPos-MARKER_SIZE, mYOrigin/100*HEADER_HEIGHT-MARKER_SIZE, objectXPos+MARKER_SIZE, mYOrigin/100*HEADER_HEIGHT-MARKER_SIZE, mPaintObjectsLines);

                //object line
                canvas.drawLine(objectXPos, mYOrigin/100*HEADER_HEIGHT, objectXPos, (float)lineEnd, mPaintObjectsLines);

                canvas.save();
                canvas.rotate((float)270, objectXPos, mYOrigin/100*HEADER_HEIGHT-MARKER_SIZE);
                canvas.drawText(name,
                        objectXPos+3, mYOrigin/100*HEADER_HEIGHT-MARKER_SIZE-2, mPaintObjects);
                canvas.drawText(String.format("%1.0fkm/%1.0fm",objectDistance/1000, elevation),
                        objectXPos+3, mYOrigin/100*HEADER_HEIGHT-MARKER_SIZE+16, mPaintObjects);
                canvas.restore();
            }

        }
        //invalidate();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void setZoomRatio(int currentZoomRatio) {
        CAMERA_VIEW_ANGLE_HORIZONTAL = DEFAULT_CAMERA_VIEW_ANGLE_HORIZONTAL * (100 / (float)currentZoomRatio);
        reloadObjects(false);
        //invalidate();
    }
}
