package com.franktom.horizon;

import com.franktom.horizon.Compass.CompassListener;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.location.Location;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.lang.Math;

class CompassView extends View implements Compass.CompassListener, View.OnClickListener, View.OnTouchListener {

    public interface ZoomListener {
        public void onZoomChange(double zoom);
    }
    private List<ZoomListener> mListeners = new ArrayList<ZoomListener>();
    public void registerZoomListener(ZoomListener listener)
    {
        mListeners.add(listener);
    }

    //average slope of the hill and angle limit for object hiding
    static final double AVERAGE_SLOPE_ANGLE = 30.0;
    static final double OVERLAPPING_ANGLE_LIMIT = 45.0;

    private static float DEFAULT_CAMERA_VIEW_ANGLE_HORIZONTAL = 56; //spise kolem 60
    private static float DEFAULT_CAMERA_VIEW_ANGLE_VERTICAL = 28; // spise kolem 30, mozna i vice
    private static final int DISPLAY_ANGLE_STEP = 10;

    private float mXOrigin;
    private float mYOrigin;

    private double mViewDirection;
    private double mSolarAzimuth;

    private boolean mUpdateCurrentLocation = true;
    private Location mCurrentLocation;
    MyLocation my_location;

    public float mCameraViewAngleHorizontal = DEFAULT_CAMERA_VIEW_ANGLE_HORIZONTAL;
    public float mCameraViewAngleVertical = DEFAULT_CAMERA_VIEW_ANGLE_VERTICAL;
    private int mCurrentZoomRatio;
    static double mMinDistance = 0.0;
    static double mMaxDistance = 30000.0;
    static double mMinElevation = 300.0;
    static double mMaxElevation = 2000.0;
    static double mAverageVAngle = 0.0;

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

    Paint mPaint;
    Paint mPaintScale;
    Paint mPaintBackground;
    Paint mPaintObjects;
    Paint mPaintObjectsLines;
    Paint mPaintLocationData;
    Paint mPaintButtonLabel;
    Paint mPaintSun, mPaintShadow;
    RectF mSunRect;
    Cursor cursor;

    public MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
        @Override
        public void gotLocation(final Location location){

            if (mUpdateCurrentLocation) {
                mCurrentLocation = new Location(location);
                mSolarAzimuth = calculateSolarAzimuth();
                reloadObjects(true);
            }
        }
    };

    void setCurrentLocation(Location loc)
    {
        mUpdateCurrentLocation = false;
        mCurrentLocation = new Location(loc);
        mSolarAzimuth = calculateSolarAzimuth();
        reloadObjects(true);
    }

    Location getCurrentLocation()
    {
        return mCurrentLocation;
    }

    void setMinDistance(double min) {
        mMinDistance = min;
    }

    double getMinDistance() {
        return mMinDistance;
    }

    void setMaxDistance(double max) {
        mMaxDistance = max;
    }

    double getMaxDistance() {
        return mMaxDistance;
    }

    void setMinElevation(double ele) {
        mMinElevation = ele;
    }

    double getMinElevation() {
        return mMinElevation;
    }

    double getMaxElevation() {
        return mMaxElevation;
    }

    void setHorizontalAngle(float angle) {
        angle = Math.max(55, angle);
        angle = Math.min(65, angle);
        DEFAULT_CAMERA_VIEW_ANGLE_HORIZONTAL = angle;

    }

    float getHorizontalAngle() {
        return DEFAULT_CAMERA_VIEW_ANGLE_HORIZONTAL;
    }

    void setVerticalAngle(float angle) {
        angle = Math.max(25, angle);
        angle = Math.min(35, angle);
        DEFAULT_CAMERA_VIEW_ANGLE_VERTICAL = angle;
    }

    float getVerticalAngle() {
        return DEFAULT_CAMERA_VIEW_ANGLE_VERTICAL;
    }


    void updateCurrentLocation()
    {
        mUpdateCurrentLocation = true;
        Location location = MyLocation.getLocation(getContext());
        mCurrentLocation = (location!=null)?location:new Location("");
        mSolarAzimuth = calculateSolarAzimuth();
        reloadObjects(true);
    }

    class Item {
        String name;
        int position;
        float angle;
        float elevation;
        boolean hidden, invisible;
        float distance;
        double objectAngleV;

        Item(String name, int position, float angle, float elevation, float distance, double objectAngleV) {
            this.name = name;
            this.position = position;
            this.angle = angle;
            this.elevation = elevation;
            this.distance = distance;
            this.objectAngleV = objectAngleV;
            this.hidden = false;
            this.invisible = false;
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

    public CompassView(Context context) {
        super(context);
        mItemsToShow = new ArrayList<Item>();

        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setTextAlign(Align.CENTER);

        mPaintButtonLabel = new Paint();
        mPaintButtonLabel.setColor(Color.WHITE);
        mPaintButtonLabel.setTextAlign(Align.CENTER);
        mPaintButtonLabel.setTextSize(30);


        mPaintScale = new Paint();
        mPaintScale.setColor(Color.YELLOW);
        mPaintScale.setTextAlign(Align.CENTER);
        mPaintScale.setStrokeWidth(3);

        mPaintSun = new Paint();
        mPaintSun.setColor(0xFFFF9933);
        mPaintSun.setStrokeWidth(1);
        mPaintShadow = new Paint();
        mPaintShadow.setColor(Color.GRAY);
        mPaintShadow.setStrokeWidth(1);
        mSunRect=new RectF(0, mYOrigin/100*HEADER_HEIGHT, 0, mYOrigin/100*HEADER_HEIGHT+20);


        mPaintBackground = new Paint();
        mPaintBackground.setARGB(128, 0, 0, 0);

        mPaintObjects = new Paint();
        mPaintObjects.setColor(Color.WHITE);
        mPaintObjects.setTextSize(20);
        mPaintObjects.setTextAlign(Align.LEFT);

        mPaintObjectsLines = new Paint();
        mPaintObjectsLines.setColor(Color.GREEN); //WHITE
        mPaintObjectsLines.setTextAlign(Align.LEFT);

        mPaintLocationData = new Paint();
        mPaintLocationData.setColor(Color.GREEN);
        mPaintLocationData.setTextSize(15);
        mPaintLocationData.setTextAlign(Align.CENTER);


        setBackgroundColor(Color.TRANSPARENT);


        mCurrentLocation = new Location("0.0 0.0");

//        mCurrentLocation.setAltitude(150.0);
//        mCurrentLocation.setLongitude(14.0443528);
//        mCurrentLocation.setLatitude(50.5961870);
//        mUpdateCurrentLocation = false;

        my_location = new MyLocation();
        my_location.init(getContext(), locationResult);

        //setOnClickListener(this);
        distCurrent = 1; //Dummy default distance
        dist0 = 1;   //Dummy default distance
        distx0 = 0;
        distx = 0;
        mCurrentZoomRatio = 100;
        setOnTouchListener(this);

        mSolarAzimuth = 0;
    }

    double calculateSolarAzimuth() {
        SolarPosition.posdata pdat = new SolarPosition.posdata();
        long retval;              /* to capture S_solpos return codes */
        SolarPosition.S_init(pdat);
        pdat.longitude = (float)mCurrentLocation.getLongitude();  /* Note that latitude and longitude are  */
        pdat.latitude  =  (float)mCurrentLocation.getLatitude();  /*   in DECIMAL DEGREES, not Deg/Min/Sec */
        pdat.timezone  =  Calendar.getInstance().getTimeZone().getRawOffset()/1000/60/60;

        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();


        pdat.year      = today.year;
        pdat.daynum    =  today.yearDay;
        pdat.hour      =  today.hour;
        pdat.minute    = today.minute;
        pdat.second    = today.second;

        /* Let's assume that the temperature is 27 degrees C and that
        the pressure is 1006 millibars.  The temperature is used for the
        atmospheric refraction correction, and the pressure is used for the
        refraction correction and the pressure-corrected airmass. */

        pdat.temp      =   27.0f;
        pdat.press     = 1006.0f;

        /* Finally, we will assume that you have a flat surface facing southeast,
        tilted at latitude. */

        pdat.tilt      = pdat.latitude;  /* Tilted at latitude */
        pdat.aspect    = 0.0f;       /* 135 deg. = SE */

        retval = SolarPosition.S_solpos (pdat);  /* S_solpos function call */
        SolarPosition.S_decode(retval, pdat);    /* ALWAYS look at the return code! */

        return Compass.convertTo180Dg((double)pdat.azim);
    }

    public double getViewDirection() {
        return mViewDirection;
    }

    public void setViewDirection(double viewDirection) {
        if (Math.abs(viewDirection - this.mViewDirection)>(mCameraViewAngleHorizontal/100)) {
            this.mViewDirection = Compass.convertTo180Dg(viewDirection);
            invalidate();
        }
    }

    public void reloadObjects(boolean queryDatabase)
    {
        Location mTmpLocation = new Location(mCurrentLocation);

        if (cursor==null || queryDatabase)
        {
            Location refLoc = new Location(mCurrentLocation);

            refLoc.setLongitude(mCurrentLocation.getLongitude()+1);
            refLoc.setLatitude(mCurrentLocation.getLatitude());
            double meridianDistance = mCurrentLocation.distanceTo(refLoc);

            refLoc.setLongitude(mCurrentLocation.getLongitude());
            refLoc.setLatitude(mCurrentLocation.getLatitude()+1);
            double parallelDistance = mCurrentLocation.distanceTo(refLoc);


            double meridianDistanceInDg = mMaxDistance/meridianDistance;
            double parallelDistanceInDg = mMaxDistance/parallelDistance;



            //Uri uri = Uri.parse("content://" + LocationsProvider.AUTHORITY + "/" + "visiblelocations");
            String[] projection =
            {
                LocationsProvider.TABLE_NAME + "._id",
                LocationsProvider.TABLE_NAME + ".NAME",
                LocationsProvider.TABLE_NAME + ".LATITUDE",
                LocationsProvider.TABLE_NAME + ".LONGITUDE",
                LocationsProvider.TABLE_NAME + ".ELEVATION"
            };

            //StringBuilder selection;
            String selection =
                    String.format("LATITUDE>%1.2f AND LATITUDE<%1.2f AND LONGITUDE>%1.2f AND LONGITUDE<%1.2f",
                            mCurrentLocation.getLatitude()-parallelDistanceInDg,
                            mCurrentLocation.getLatitude()+parallelDistanceInDg,
                            mCurrentLocation.getLongitude()-meridianDistanceInDg,
                            mCurrentLocation.getLongitude()+meridianDistanceInDg);

            cursor = ((Activity)getContext()).getContentResolver().query(LocationsProvider.CONTENT_ID_URI_VISIBLE_LOCATION, projection, selection.replace(',', '.'), null, null);
        }

        mMaxElevation = 0;
        mItemsToShow.clear();
        cursor.moveToFirst();
        for( int i=0; i<cursor.getCount(); i++)
        {
            float latitude = cursor.getFloat(2);
            float longitude = cursor.getFloat(3);
            float elevation = cursor.getFloat(4);

            mTmpLocation.setLatitude(latitude);
            mTmpLocation.setLongitude(longitude);
            mTmpLocation.setAltitude(elevation);

            float objectDistance= mCurrentLocation.distanceTo(mTmpLocation);
            float objectBearing= mCurrentLocation.bearingTo(mTmpLocation);
            double objectAngleV = Math.atan((mTmpLocation.getAltitude()-mCurrentLocation.getAltitude())/objectDistance) / Math.PI * 180;

            if (elevation>mMaxElevation)
                mMaxElevation = elevation;


            if ( //(objectDistance<mDistanceLimit0 && elevation == 0) ||
                 //(objectDistance<mDistanceLimit1 && elevation>mElevationLimit1) ||
                 //(objectDistance<mDistanceLimit2 && elevation>mElevationLimit2) ||
                 //(objectDistance<mDistanceLimit3 && elevation>mElevationLimit3) ||
                 //(objectDistance<mDistanceLimit3)
                    objectDistance>=mMinDistance && objectDistance<=mMaxDistance && (elevation>=mMinElevation || elevation == 0)
                 ) {

                mItemsToShow.add(new Item(cursor.getString(1), cursor.getPosition(), objectBearing, elevation, objectDistance, objectAngleV));
            }

            cursor.moveToNext();
        }

        Collections.sort(mItemsToShow, new ItemComparator());

        /*for (int i = mItemsToShow.size()-1; i>=0 ; i--) {
            Item item = mItemsToShow.get(i);
            cursor.moveToPosition(item.position);
            String name = cursor.getString(1);
            System.out.println("A (" + name + ") - Pos:" + item.position + " Azimuth:" + item.angle);
        }*/


        //hiding object by camera elevation
        double tanAngleE = Math.tan(AVERAGE_SLOPE_ANGLE*Math.PI/180.0);

        for (int i1 = 0; i1 < mItemsToShow.size(); i1++) {
        //for (int i1 = mItemsToShow.size()-1; i1>=0 ; i1--) {
            Item myItem = mItemsToShow.get(i1);
            myItem.invisible = false;
            for (int i2 = 0; i2 < mItemsToShow.size(); i2++) {
                Item otherItem = mItemsToShow.get(i2);

                double angleG = Compass.convertTo180Dg(myItem.angle-otherItem.angle);
              //do not evaluate visibility
                if (i1!=i2 && ///...with the same object
                    otherItem.distance>100 && //...if the other object is closer than 100m (observer on the top probably)
                    myItem.distance > otherItem.distance && // if the other object is behind my object
                    Math.abs(angleG) < OVERLAPPING_ANGLE_LIMIT // if the angle between object is too large
                    ) {

                    double angleGdist = Math.abs(Math.tan(angleG*Math.PI/180)*otherItem.distance);
                    double elevationDrop = tanAngleE*angleGdist;

                    double relativeElevation = otherItem.elevation-mCurrentLocation.getAltitude();
                    double otherObjectAngleV = Math.atan((relativeElevation-elevationDrop)/otherItem.distance)/Math.PI*180.0;
                    if (otherObjectAngleV > myItem.objectAngleV) {
                        myItem.invisible = true;
                        //mItemsToShow.remove(i1);
                        break;
                    }
                }
            }
        }

        //hiding object by camera angle
        float lastAzimuth = 10000;
        double lastObjectAngleV = -90;
        int lastPosition = -1;
        boolean lastInvisible = true;
        float overlappingLimit = mHideOverlappingLimit * mCameraViewAngleHorizontal / DEFAULT_CAMERA_VIEW_ANGLE_HORIZONTAL;
        mAverageVAngle = 0.0;
        int mAverageVAngleCount = 0;
        for (int i = mItemsToShow.size()-1; i>=0 ; i--) {
            Item item = mItemsToShow.get(i);
            item.hidden = false;

            if(item.elevation>0) {
                mAverageVAngle += item.objectAngleV;
                mAverageVAngleCount++;
            }


            if (lastAzimuth - item.angle < overlappingLimit) {
                if ((lastInvisible==true && item.invisible==false) || item.objectAngleV > lastObjectAngleV ) {
                    if (lastPosition>=0)
                        mItemsToShow.get(lastPosition).hidden = true;
                    lastPosition = i;
                    lastAzimuth = item.angle;
                    lastObjectAngleV = item.objectAngleV;
                    lastInvisible = item.invisible;
                }
                else {
                    item.hidden = true;
                }
            }
            else
            {
                lastPosition = i;
                lastAzimuth = item.angle;
                lastObjectAngleV = item.objectAngleV;
                lastInvisible = item.invisible;
            }
        }
        mAverageVAngle = mAverageVAngle / mAverageVAngleCount;

        /*for (int i = mItemsToShow.size()-1; i>=0 ; i--) {
        Item item = mItemsToShow.get(i);
        cursor.moveToPosition(item.position);
        String name = cursor.getString(1);
        System.out.println("B (" + name + ") - Pos:" + item.position + " Azimuth:" + item.angle);
        }*/

        invalidate();
    }

    private static final int HEADER_HEIGHT = 30; //%
    private static final int HEADER_HEIGHT_TOUCH_AREA = 50;
    private static final int SCALE_HEIGHT = 1; //%
    private static final float MARKER_SIZE = 1.6f; //pts
    private static final int LOCKED_BUTTON_WIDTH = 15; //pts
    private static final int LOCKED_BUTTON_HEIGHT = 10; //pts

    private static float msInPixels = 0;

    @Override
    protected void onDraw(Canvas canvas) {

        final float xc = mXOrigin/2;
        final float yc = mYOrigin/2;


        //background
        mPaintBackground.setARGB(0x7F, 0, 0, 0);
        canvas.drawRect(0, 0, mXOrigin, mYOrigin/100*HEADER_HEIGHT, mPaintBackground);
        mPaintBackground.setARGB(0x8F, 0, 0, 0);
        canvas.drawRect(0, mYOrigin/100*HEADER_HEIGHT, mXOrigin, mYOrigin/100*HEADER_HEIGHT+20, mPaintBackground);

        //center lines
        if (mDrawHelperLines) {
            canvas.drawLine(xc, mYOrigin/100*HEADER_HEIGHT, xc, mYOrigin/100*100, mPaint);
            canvas.drawLine(0, yc, mXOrigin, yc, mPaint);
        }

        //double mTopAngle  = (CAMERA_VIEW_ANGLE_VERTICAL/2);
        //double mBottomAngle  = -(CAMERA_VIEW_ANGLE_VERTICAL/2);

        double mLeftAngle = mViewDirection - (mCameraViewAngleHorizontal/2.0);
        double mRightAngle = mViewDirection + (mCameraViewAngleHorizontal/2.0);
        long start = Math.round(mLeftAngle  / DISPLAY_ANGLE_STEP) * DISPLAY_ANGLE_STEP;
        long end   = Math.round(mRightAngle / DISPLAY_ANGLE_STEP) * DISPLAY_ANGLE_STEP + DISPLAY_ANGLE_STEP;

        for (long i = start; i < end; i += DISPLAY_ANGLE_STEP)
        {
            double xPos = ((i-mLeftAngle)/mCameraViewAngleHorizontal) * mXOrigin;
            long displayedAzimuthValue = i;
            if (displayedAzimuthValue < 0) displayedAzimuthValue = displayedAzimuthValue + 360;
            if (displayedAzimuthValue >= 360) displayedAzimuthValue = displayedAzimuthValue - 360;
            canvas.drawLine((float)(xPos), mYOrigin/100*HEADER_HEIGHT+20, (float)(xPos), mYOrigin/100*HEADER_HEIGHT+13, mPaintScale);
            canvas.drawText(String.format("%03d",displayedAzimuthValue), (float)(xPos), mYOrigin/100*HEADER_HEIGHT+12, mPaintScale);
        }

        canvas.drawText(
                String.format("Az:%03.0f Lat:%3.7f Lon:%3.7f Alt:%.0f MinEle:%.0f Range:%.0f-%.0f Count:%d",
                        (mViewDirection < 0) ? mViewDirection + 360 : mViewDirection,
                        mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude(),
                        mCurrentLocation.getAltitude(),
                        mMinElevation, mMinDistance, mMaxDistance, mItemsToShow.size()),
                        xc, mYOrigin/100*2, mPaintLocationData);

        //draw horizon line (with taking average V angle into account)
        double middleOfTheDisplay = mYOrigin - mYOrigin/100*(100-HEADER_HEIGHT)/2.0;
        double horizontHeight = middleOfTheDisplay - ((-mAverageVAngle)/mCameraViewAngleVertical*mYOrigin);
        canvas.drawLine((float)0.0, (float)horizontHeight, (float)mXOrigin, (float)horizontHeight, mPaintLocationData);

        //Location mTmpLocation = new Location(mCurrentLocation);

        Iterator<Item> it = mItemsToShow.iterator();
        while (it.hasNext())
        {
            Item item = it.next();
            //cursor.moveToPosition(item.position);

            /*String name = cursor.getString(1);
            float latitude = cursor.getFloat(2);
            float longitude = cursor.getFloat(3);*/
            //float elevation = item.elevation;

            //mTmpLocation.setLatitude(latitude);
            //mTmpLocation.setLongitude(longitude);
            //mTmpLocation.setAltitude(elevation);

            //double objectAngleV = item.objectAngleV;//Math.atan((mTmpLocation.getAltitude()-mCurrentLocation.getAltitude())/objectDistance) / Math.PI * 180;

            //if (item.angle < mLeftAngle || item.angle > mRightAngle)
                //continue;
            /*if (Math.abs(item.angle - mViewDirection) > CAMERA_VIEW_ANGLE_HORIZONTAL / 2){
                continue;
            }###*/
            double lineEnd = middleOfTheDisplay - ((item.objectAngleV-mAverageVAngle)/mCameraViewAngleVertical*mYOrigin);
            if (lineEnd < mYOrigin/100*(HEADER_HEIGHT))
                lineEnd = mYOrigin/100*(HEADER_HEIGHT);

            if (item.elevation==0)
                lineEnd = mYOrigin;

            mPaintObjectsLines.setColor(item.invisible?Color.GRAY:Color.GREEN);
            mPaintObjects.setColor(item.invisible?Color.GRAY:Color.WHITE);

            /*float x = (item.angle-mLeftAngle);
            if (x<=360) x+=360;
            if (x>360) x-=360;*/
            float objectXPos = (float)(Compass.convertTo360Dg(item.angle-mLeftAngle)/mCameraViewAngleHorizontal) * mXOrigin;

            if (objectXPos<0 || objectXPos>mXOrigin)
                continue;

            double lineWidthInPercent = (item.distance-mMinDistance)/(mMaxDistance-mMinDistance);


            if (!item.invisible) {
                //FFFF00 yellow, FF8000 orange, FF0000 red
                //mPaintObjectsLines.setColor(0xFFFF0000 | ((int)(0xFF-Math.round(lineWidthInPercent*0xBF))<<8));

                //GREEN, YELLOW, ORANGE, RED
                /*if (lineWidthInPercent > 0.75) {
                    mPaintObjectsLines.setColor(Color.GREEN);
                } else if (lineWidthInPercent > 0.50) {
                    mPaintObjectsLines.setColor(Color.YELLOW);
                } else if (lineWidthInPercent > 0.25) {
                    mPaintObjectsLines.setColor(0xFFF5B800);
                } else {
                    mPaintObjectsLines.setColor(Color.RED);
                }*/


                //mPaintObjectsLines.setColor(0x0000FF00 | ((int)(0xFF-Math.round(lineWidthInPercent*0xDF))<<24));
            }


            //object marker
            if  (item.hidden) {
                canvas.drawLine(objectXPos, mYOrigin/100*HEADER_HEIGHT, objectXPos, mYOrigin/100*HEADER_HEIGHT-msInPixels/2, mPaintObjectsLines);
            }
            else {
                canvas.drawLine(objectXPos, mYOrigin/100*HEADER_HEIGHT, objectXPos-msInPixels, mYOrigin/100*HEADER_HEIGHT-msInPixels, mPaintObjectsLines);
                canvas.drawLine(objectXPos, mYOrigin/100*HEADER_HEIGHT, objectXPos+msInPixels, mYOrigin/100*HEADER_HEIGHT-msInPixels, mPaintObjectsLines);
                canvas.drawLine(objectXPos-msInPixels, mYOrigin/100*HEADER_HEIGHT-msInPixels, objectXPos+msInPixels, mYOrigin/100*HEADER_HEIGHT-msInPixels, mPaintObjectsLines);

                //object line
                //mPaintObjectsLines.setStrokeWidth(6-Math.round(lineWidthInPercent*5));
                mPaintObjectsLines.setColor(0x0000FF00 | ((int)(0xFF-Math.round(lineWidthInPercent*0xDF))<<24));
                mPaintObjectsLines.setStrokeWidth(5);
                canvas.drawLine(objectXPos, mYOrigin/100*HEADER_HEIGHT, objectXPos, (float)lineEnd, mPaintObjectsLines);
                mPaintObjectsLines.setStrokeWidth(1);
                //mPaintObjectsLines.setStrokeWidth(1);

                canvas.save();
                canvas.rotate((float)270, objectXPos, mYOrigin/100*HEADER_HEIGHT-msInPixels);
                canvas.drawText(item.name,
                        objectXPos+3, mYOrigin/100*HEADER_HEIGHT-msInPixels*1.15f, mPaintObjects);
                canvas.drawText(String.format("%1.0fkm/%1.0fm",item.distance/1000, item.elevation),
                        objectXPos+3, mYOrigin/100*HEADER_HEIGHT+msInPixels*0.22f, mPaintObjects);
                canvas.restore();
            }
        }
        if (mFrozen) {
            canvas.drawRect(10, 10, 10+mXOrigin*LOCKED_BUTTON_WIDTH/100 , 10+mYOrigin*LOCKED_BUTTON_HEIGHT/100, mPaintBackground);
            canvas.drawText("Unlock", 10+mXOrigin*LOCKED_BUTTON_WIDTH/2/100, 10+mYOrigin*LOCKED_BUTTON_HEIGHT/100-20, mPaintButtonLabel);

        }

        //paint sun, shadow etc.
        if (mSolarAzimuth!=0) {
            float solarXPos = (float)(Compass.convertTo360Dg(mSolarAzimuth-mLeftAngle)/mCameraViewAngleHorizontal) * mXOrigin;
            float shadowXPos = (float)(Compass.convertTo360Dg(Compass.convertTo360Dg(mSolarAzimuth-180)-mLeftAngle)/mCameraViewAngleHorizontal) * mXOrigin;
            float sunOnLeftXPos = (float)(Compass.convertTo360Dg(Compass.convertTo360Dg(mSolarAzimuth-90)-mLeftAngle)/mCameraViewAngleHorizontal) * mXOrigin;
            float sunOnRightXPos = (float)(Compass.convertTo360Dg(Compass.convertTo360Dg(mSolarAzimuth+90)-mLeftAngle)/mCameraViewAngleHorizontal) * mXOrigin;
            canvas.drawCircle(solarXPos, mYOrigin/100*HEADER_HEIGHT+10, 8, mPaintSun);
            canvas.drawCircle(shadowXPos, mYOrigin/100*HEADER_HEIGHT+10, 8, mPaintShadow);

            mSunRect.top=mYOrigin/100*HEADER_HEIGHT+2;mSunRect.bottom=mYOrigin/100*HEADER_HEIGHT+18;
            canvas.drawCircle(sunOnLeftXPos, mYOrigin/100*HEADER_HEIGHT+10, 8, mPaintSun);
            mSunRect.left=sunOnLeftXPos-8;mSunRect.right=sunOnLeftXPos+8;
            canvas.drawArc(mSunRect, 90, 180, false, mPaintShadow);

            canvas.drawCircle(sunOnRightXPos, mYOrigin/100*HEADER_HEIGHT+10, 8, mPaintShadow);
            mSunRect.left=sunOnRightXPos-8;mSunRect.right=sunOnRightXPos+8;
            canvas.drawArc(mSunRect, 270, 180, false, mPaintSun);
        }
    }

    /*public int getZoomRatio() {
        return mCurrentZoomRatio;
    }*/

    public void setZoomRatio(int currentZoomRatio) {
        mCameraViewAngleHorizontal = DEFAULT_CAMERA_VIEW_ANGLE_HORIZONTAL * (100 / (float)currentZoomRatio);
        mCameraViewAngleVertical = DEFAULT_CAMERA_VIEW_ANGLE_VERTICAL * (100 / (float)currentZoomRatio);
        reloadObjects(false);
    }

    @Override
    public void onAzimutChange(double azimut) {
        if (!mFrozen) {
            setViewDirection(azimut);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // compute the origin of the screen relative to the origin of
        // the bitmap
        mXOrigin = w;
        mYOrigin = 480;//h;
        msInPixels = mXOrigin/100*MARKER_SIZE;

        mPaintButtonLabel.setTextSize(mXOrigin/100*2.5f); //30
        mPaintObjects.setTextSize(mXOrigin/100*2.2f);//20
        mPaintLocationData.setTextSize(mXOrigin/100*1.6f);//15
    }

    @Override
    public void onClick (View v) {
        android.widget.Toast.makeText(getContext(), "aaa", android.widget.Toast.LENGTH_SHORT).show();
    }

    void printSamples(MotionEvent ev) {
        final int historySize = ev.getHistorySize();
        final int pointerCount = ev.getPointerCount();
        for (int h = 0; h < historySize; h++) {
            System.out.printf("At time %d:", ev.getHistoricalEventTime(h));
            for (int p = 0; p < pointerCount; p++) {
                System.out.printf("  pointer %d: (%f,%f)",
                    ev.getPointerId(p), ev.getHistoricalX(p, h), ev.getHistoricalY(p, h));
            }
        }
        System.out.printf("At time %d:", ev.getEventTime());
        for (int p = 0; p < pointerCount; p++) {
            System.out.printf("  pointer %d: (%f,%f)",
                ev.getPointerId(p), ev.getX(p), ev.getY(p));
        }
    }

    /*@Override
    public boolean onTouch(View v, MotionEvent event) {
        printSamples(event);
        android.widget.Toast.makeText(getContext(), "bbb", android.widget.Toast.LENGTH_SHORT).show();
        return true;
    }*/

    int touchState;
    final int IDLE = 0;
    final int TOUCH = 1;
    final int PINCH = 2;
    float dist0, distCurrent, distx0, distx;
    double dist_xOld;


    @Override
    public boolean onTouch(View view, MotionEvent event) {
        // TODO Auto-generated method stub
        if (event.getY(0)>mYOrigin/100*HEADER_HEIGHT_TOUCH_AREA) {
            return false;
        }

        float distx, disty;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (event.getX(0)<10+mXOrigin*LOCKED_BUTTON_WIDTH/100 && event.getY(0)<10+mYOrigin*LOCKED_BUTTON_HEIGHT/100) {
                    mFrozen = false;
                    return true;
                }

                // A pressed gesture has started, the motion contains the
                // initial starting location.
                //myTouchEvent.setText("ACTION_DOWN");
                dist_xOld = getViewDirection();
                distx0 = event.getX(0);
                touchState = TOUCH;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                // A non-primary pointer has gone down.
                //myTouchEvent.setText("ACTION_POINTER_DOWN");
                touchState = PINCH;

                // Get the distance when the second pointer touch
                distx = event.getX(0) - event.getX(1);
                disty = event.getY(0) - event.getY(1);
                dist0 = FloatMath.sqrt(distx * distx + disty * disty);
                System.out.printf("Dist0 = %f.4f", dist0);
                break;
            case MotionEvent.ACTION_MOVE:
                // A change has happened during a press gesture (between
                // ACTION_DOWN and ACTION_UP).
                //myTouchEvent.setText("ACTION_MOVE");

                if (touchState == PINCH) {
                    // Get the current distance
                    distx = event.getX(0) - event.getX(1);
                    disty = event.getY(0) - event.getY(1);
                    distCurrent = FloatMath.sqrt(distx * distx + disty * disty);
                    //zoomChanged((((distCurrent/dist0)-1)/10)+1);
                    zoomChanged(distCurrent/dist0, false);
                } else if (touchState == TOUCH) {
                    if (event.getY(0)<100) {
                        distx = event.getX(0) - distx0;
                        mFrozen = true;
                        setViewDirection(dist_xOld-distx/(600/mCameraViewAngleVertical));
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                // A pressed gesture has finished.
                //myTouchEvent.setText("ACTION_UP");
                //mCurrentZoomRatio = (int)(mCurrentZoomRatio*distCurrent/dist0);
                touchState = IDLE;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                // A non-primary pointer has gone up.
                //myTouchEvent.setText("ACTION_POINTER_UP");
                zoomChanged(distCurrent/dist0, true);
                touchState = IDLE;
                break;
        }

        return true;
    }

    void zoomChanged(double newZoom, boolean done) {
        /*int z = (int)java.lang.Math.floor(getZoomRatio()*newZoom);
        if (z<100) z=100;
        if (z>3000) z=3000;
        setZoomRatio(z);*/
        int z = (int)java.lang.Math.floor(mCurrentZoomRatio*newZoom);
        if (z<100) z=100;
        if (z>3000) z=3000;
        setZoomRatio(z);
        if (done) {
            mCurrentZoomRatio = (int)(z);
            for (ZoomListener listener : mListeners) {
                listener.onZoomChange(mCurrentZoomRatio);
            }
         }



    }

}
