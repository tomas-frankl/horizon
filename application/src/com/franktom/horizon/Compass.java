package com.franktom.horizon;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

public class Compass implements SensorEventListener {

    public interface CompassListener {
        public void onAzimutChange(double azimut);
    }


    private SensorManager mSensorManager;
    private List<CompassListener> mListeners = new ArrayList<CompassListener>();

    private static final float COMPASS_DAMPING = 0.005f;

    private float m_lastAccels[];
    private float m_lastMagFields[];
    private float m_rotationMatrix[];
    private float m_orientation[];

    private double mViewDirection;

    public Compass(Context context) {
        mSensorManager = (SensorManager)((Activity)context).getSystemService(Context.SENSOR_SERVICE);

        m_lastAccels = new float[16];
        m_lastMagFields = new float[16];
        m_rotationMatrix = new float[16];
        m_orientation = new float[3];

        mViewDirection = 0;
    }

    public void registerAzimutListener(CompassListener listener)
    {
        mListeners.add(listener);
    }

    public void start() {
        Sensor magnetField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, magnetField, SensorManager.SENSOR_DELAY_UI);
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    public void stop() {
        mSensorManager.unregisterListener(this);
    }

    protected double lowPass( double newValue, double oldValue ) {
        newValue = newValue + (float)90.0;
        double delta = newValue - oldValue;
        delta = convertTo180Dg(delta);
        double newOutput = oldValue + COMPASS_DAMPING * (delta);
        return convertTo180Dg(newOutput);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE){
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, m_lastAccels, 0, 3);
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, m_lastMagFields, 0, 3);
        }

        if (SensorManager.getRotationMatrix(m_rotationMatrix, null, m_lastAccels, m_lastMagFields)) {
            SensorManager.getOrientation(m_rotationMatrix, m_orientation);

            mViewDirection = lowPass( Math.toDegrees(m_orientation[0]), mViewDirection );

            for (CompassListener listener : mListeners) {
                listener.onAzimutChange(mViewDirection);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public static double convertTo360Dg(double value)
    {
        if (value<=0) return value+360;
        if (value>360) return value-360;
        return value;
    }

    public static double convertTo180Dg(double value)
    {
        if (value<=-180) return value+360;
        if (value>180) return value-360;
        return value;
    }
}

