package com.bearbunny.controllerdemo;

import android.app.Activity;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Peter on 2016.12.19..
 */

public class BackgroundProcessManager {
    private Timer sendDataOverWifiTimer;
    private ControllerDataProvider dataProvider;
    private SendDataThroughWifi wifiModeProvider;
    private Activity activity;
    private boolean sendingOnWifi = false;

    public BackgroundProcessManager(Activity activity, ControllerDataProvider dataProvider) {
        this.activity = activity;
        this.dataProvider = dataProvider;
    }

    public boolean getWifiSendingState() {
        return sendingOnWifi;
    }

    public void StartDataSendOnWifi(long interval, String targetIP)
    {
        if (!sendingOnWifi) {
            Log.d("Bg process manager", "Star send on WiFi");
            sendingOnWifi = true;
            if (sendDataOverWifiTimer == null) {
                wifiModeProvider = new SendDataThroughWifi(activity, dataProvider);
                wifiModeProvider.InitSendThroughWifi(targetIP);
            }

            sendDataOverWifiTimer = new Timer();
            sendDataOverWifiTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    wifiModeProvider.SendData();
                }
            }, 0l, interval);
        }
    }

    public void StopDataSendOnWifi() {
        Log.d("Bg process manager", "Stop send on WiFi");
        sendingOnWifi = false;
        if (sendDataOverWifiTimer != null) {
            sendDataOverWifiTimer.cancel();
        }
    }

    public void ResetDataSenDOnWifi() {
        StopDataSendOnWifi();
        sendDataOverWifiTimer = null;
    }

    public void OnStop() {
        StopDataSendOnWifi();
    }
}
