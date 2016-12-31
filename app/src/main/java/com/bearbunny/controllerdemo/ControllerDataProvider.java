package com.bearbunny.controllerdemo;

import android.app.Activity;
import android.hardware.SensorManager;
import android.util.Log;

import org.hitlabnz.sensor_fusion_demo.orientationProvider.ImprovedOrientationSensor1Provider;
import org.hitlabnz.sensor_fusion_demo.orientationProvider.OrientationProvider;
import org.hitlabnz.sensor_fusion_demo.representation.Quaternion;
import org.hitlabnz.sensor_fusion_demo.representation.Vector3f;

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
    private Quaternion fusedQuaternion;
    private Quaternion zeroQuaternion;
    private Quaternion finalFusedQuaternion;
    private String whichHand = "R";

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
    private boolean resetCenter = false;

    // Debug options
    private boolean lockOrientation = false;
    private boolean dummyOrientation = false;
    private Quaternion hmdCorrectionQuat = new Quaternion();
    public void setOrientationLock(Boolean setTo) {
        lockOrientation = setTo;
    }

    public void setDummyOrientation(Boolean setTo) {
        dummyOrientation = setTo;
    }

    public void setHmdCorrection(float w, float x, float y, float z) {
        hmdCorrectionQuat.setXYZW(x,y,z,w);
    }

    public String getWhichHand() {
        return whichHand;
    }

    public Boolean getOrientationLock() {
        return lockOrientation;
    }

    public Boolean getDummyOrientation() {
        return dummyOrientation;
    }

    public Quaternion getHmdCorrection() {
        return hmdCorrectionQuat;
    }

    public boolean getAndClearResetCenterButton() {
        boolean temp = resetCenter;
        resetCenter = false;
        return temp;
    }

    public boolean getResetCenterButton() {
        return resetCenter;
    }

    public void setButtonState(Buttons button, boolean state) {
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

    public boolean getButtonState(Buttons button) {
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

    public void setHand(String newValue) {
        whichHand = newValue;
    }

    public float getTrackpadX() {
        return trackpadX;
    }

    public float getTrackpadY() {
        return trackpadY;
    }

    public boolean getTracpadTouched() {
        return trackpadTouched;
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

    public Quaternion getFusedQuaternion() {
        RefreshFusedOrientationQuaternionData();
        Log.d("Final fused quaternion", finalFusedQuaternion.toStringSingleLine());
        return finalFusedQuaternion;
    }

    public ControllerDataProvider(Activity activity, int sensorRefreshSpeed) {
        fusedEulerAngles = new float[3];
        fusedQuaternion = new Quaternion();
        fusedQuaternion.loadIdentityQuat();
        zeroQuaternion = new Quaternion();
        zeroQuaternion.loadIdentityQuat();
        finalFusedQuaternion = new Quaternion();
        fusedQuaternion.loadIdentityQuat();
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

    public OrientationProvider getOrientationProvider()
    {
        return currentOrientationProvider;
    }

    private void RefreshFusedOrientationData()
    {
        if (lockOrientation)
            return;

        if (dummyOrientation) {
            fusedEulerAngles[0] = 0f;
            fusedEulerAngles[1] = 0f;
            fusedEulerAngles[2] = 90f;
        }
        else {
            currentOrientationProvider.getEulerAngles(fusedEulerAngles);
            fusedEulerAngles[0] *= 57.2957795f;
            fusedEulerAngles[1] *= 57.2957795f;
            fusedEulerAngles[2] *= 57.2957795f;
        }
    }

    public void setCurrentOrientationAsCenter() {
        /*zeroQuaternion.setW(fusedQuaternion.w());
        zeroQuaternion.setX(fusedQuaternion.x());
        zeroQuaternion.setY(fusedQuaternion.y());
        zeroQuaternion.setZ(fusedQuaternion.z());*/
        zeroQuaternion.set(fusedQuaternion);
        Log.d("Center point set", zeroQuaternion.toStringSingleLine());
        zeroQuaternion.invert();
        zeroQuaternion.setW(-zeroQuaternion.getW());
        Log.d("Center point inverted", zeroQuaternion.toStringSingleLine());
        resetCenter = true;
    }

    float angleCounter = 0f;
    private void RefreshFusedOrientationQuaternionData() {
        if (lockOrientation)
            return;

        if (dummyOrientation) {
            //fusedQuaternion.setXYZW(0f, 0f, 0.707f, 0.707f);
            fusedQuaternion.setAxisAngle(new Vector3f(1f, 0f, 0f), 35f);
        }
        else {
            currentOrientationProvider.getQuaternion(fusedQuaternion);
        }

        fusedQuaternion.multiplyByQuat(zeroQuaternion, finalFusedQuaternion);
        finalFusedQuaternion.normalize();
        Quaternion angleCorrection = new Quaternion();
        angleCorrection.setAxisAngle(new Vector3f(1f,0f,0f),-90f);
        angleCorrection.multiplyByQuat(finalFusedQuaternion, finalFusedQuaternion);
        finalFusedQuaternion.setW(-finalFusedQuaternion.w());
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

    public static String QuaternionToString(Quaternion value)
    {
        /**
         * The format of the floats in the string.
         */
        final String format = "%6.6f";
        return "W:" + (value.w() < 0 ? "" : "+") +  String.format(Locale.US, format, value.w()) +
                ";\nX:" + (value.x() < 0 ? "" : "+") + String.format(Locale.US, format, value.x()) +
                ";\nY:" + (value.y() < 0 ? "" : "+") +  String.format(Locale.US, format, value.y()) +
                ";\nZ:" + (value.z() < 0 ? "" : "+") +  String.format(Locale.US, format, value.z());
    }
}
