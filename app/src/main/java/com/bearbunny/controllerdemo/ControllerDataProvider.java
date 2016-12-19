package com.bearbunny.controllerdemo;

import android.app.Activity;
import android.hardware.SensorManager;
import android.util.Log;

import org.hitlabnz.sensor_fusion_demo.orientationProvider.ImprovedOrientationSensor1Provider;
import org.hitlabnz.sensor_fusion_demo.orientationProvider.OrientationProvider;

import java.util.Locale;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by Peter on 2016.12.15..
 */

public class ControllerDataProvider {
    // IMU fields
    private OrientationProvider currentOrientationProvider;
    private SensorManager sensorManager;
    private float[] fusedEulerAngles;

    // Buttons
    public enum Buttons {
        TrackpadPress, Trigger, Menu, System, Grip }
    private boolean trackpadPressed = false;
    private boolean triggerPressed = false;
    private boolean menuButtonPressed = false;
    private boolean systemButtonPressed = false;
    private boolean gripButtonPressed = false;
    private boolean trackpadTouched = false;
    private float trackpadX = 0f;
    private float trackpadY = 0f;

    public void SetButtonState(Buttons button, boolean state) {
        switch (button) {
            case TrackpadPress:
                trackpadPressed = state;
                break;
            case Trigger:
                triggerPressed = state;
                break;
            case Menu:
                menuButtonPressed = state;
                break;
            case System:
                systemButtonPressed = state;
                break;
            case Grip:
                gripButtonPressed = state;
                break;
        }
    }

    public boolean GetButtonState(Buttons button) {
        switch (button) {
            case Trigger:
                return triggerPressed;
            case TrackpadPress:
                return trackpadPressed;
            case Menu:
                return menuButtonPressed;
            case System:
                return systemButtonPressed;
            case Grip:
                return gripButtonPressed;
            default:
                return false;
        }
    }

    public void SetTrackpadPosition(boolean touched, float x, float y) {
        trackpadTouched = touched;
        trackpadX = x;
        trackpadY = y;
    }

    public float[] getFusedEulerAngles() {
        RefreshFusedOrientationData();
        return fusedEulerAngles;
    }

    public ControllerDataProvider(Activity activity, int sensorRefreshSpeed) {
        fusedEulerAngles = new float[3];
        sensorManager = (SensorManager) activity.getSystemService(SENSOR_SERVICE);
        currentOrientationProvider = new ImprovedOrientationSensor1Provider(sensorManager, sensorRefreshSpeed);
    }

    public void OnResume()
    {
        currentOrientationProvider.start();
    }

    public void OnPause()
    {
        currentOrientationProvider.stop();
    }

    public OrientationProvider GetOrientationProvider()
    {
        return currentOrientationProvider;
    }

    private void RefreshFusedOrientationData()
    {
        currentOrientationProvider.getEulerAngles(fusedEulerAngles);
        fusedEulerAngles[0] *= 57.2957795f;
        fusedEulerAngles[1] *= 57.2957795f;
        fusedEulerAngles[2] *= 57.2957795f;
    }

    /**
     * Converts the given float array to string.
     * @param values: A float array with 3 elements.
     * @return The float array converted to a string, separated by semicolons.
     */
    public static String Float3ToString(float[] values)
    {
        /**
         * The format of the floats in the string.
         */
        final String format = "%6.2f";
        return String.format(Locale.US, format, values[0]) + "; " +String.format(Locale.US, format, values[1]) + "; " +
                String.format(Locale.US, format, values[2]);
    }
}
