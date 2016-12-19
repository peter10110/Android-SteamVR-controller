package com.bearbunny.controllerdemo;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.hitlabnz.sensor_fusion_demo.orientationProvider.ImprovedOrientationSensor1Provider;
import org.hitlabnz.sensor_fusion_demo.orientationProvider.OrientationProvider;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.SENSOR_SERVICE;
import static android.content.Context.WIFI_SERVICE;

/**
 * Created by Peter on 2016.12.11..
 */

public class WifiModeFragment extends Fragment {
    // Setting keys
    private static final String PREFS_NAME = "ControllerDemoPreferences";
    private static final String TARGET_IP_KEY = "targetIP";
    private static final String TARGET_PORT_KEY = "targetPort";
    private static final String CONNECTION_MODE_KEY = "connectionMode";
    private static final String SEND_INTERVAL_KEY = "sendInterval";

    // Layout elements
    private View view;
    private EditText ipField;
    private EditText portField;
    private EditText sendIntervalField;
    private RadioGroup connectionModeToggleGroup;
    private ToggleButton sendDataModeToggle;
    private TextView ownIPField;
    private TextView fusedSensorField;
    private Button btn_button0;
    private Button btn_button1;
    private Button btn_button2;
    private ToggleButton volumeUp_btn;
    private ToggleButton volumeDwn_btn;

    private enum ConnectionModes {
        UDP, TCP
    }

    private String targetIP = "000.000.000.000";
    private int targetPort = 5555;
    private long sendInterval = 8l;
    private WifiModeFragment.ConnectionModes connectionMode = WifiModeFragment.ConnectionModes.UDP;

    private Timer timer;
    private Boolean sendEnabled = false;

    private ControllerDataProvider dataProvider;
    private BackgroundProcessManager backgroundProcessManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.wifi_mode_layout, container, false);

        this.view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        // Restore preferences
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        targetIP = settings.getString(TARGET_IP_KEY, "000.000.000.000");
        targetPort = settings.getInt(TARGET_PORT_KEY, 5555);
        sendInterval = settings.getLong(SEND_INTERVAL_KEY, 8l);
        connectionMode = WifiModeFragment.ConnectionModes.values()[settings.getInt(CONNECTION_MODE_KEY, 0)];

        ownIPField = (TextView) view.findViewById(R.id.ipTextView);
        ownIPField.setText("IP: " + GetWifiIP());

        ipField = (EditText) view.findViewById(R.id.targetIPField);
        ipField.setText(targetIP);

        portField = (EditText) view.findViewById(R.id.targetPortField);
        portField.setText(String.valueOf(targetPort));

        sendIntervalField = (EditText) view.findViewById(R.id.intervalField);
        sendIntervalField.setText(String.valueOf(sendInterval));

        connectionModeToggleGroup = (RadioGroup) view.findViewById(R.id.modeRadio);
        connectionModeToggleGroup.check(connectionModeToggleGroup.getChildAt(connectionMode.ordinal()).getId());

        sendDataModeToggle = (ToggleButton) view.findViewById(R.id.sendDataToggle);
        sendDataModeToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendButtonPressed(sendDataModeToggle.isChecked());
            }
        });

        fusedSensorField = (TextView) view.findViewById(R.id.fusedValue);

        btn_button0 = (Button) view.findViewById(R.id.button0);
        btn_button1 = (Button) view.findViewById(R.id.button1);
        btn_button2 = (Button) view.findViewById(R.id.button2);
        volumeUp_btn = (ToggleButton) view.findViewById(R.id.volUpTgBtn);
        volumeDwn_btn = (ToggleButton) view.findViewById(R.id.volDwnTgBtn);

        btn_button0.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_UP) {
                    dataProvider.SetButtonState(ControllerDataProvider.Buttons.System, false);
                }
                else if (action == MotionEvent.ACTION_DOWN) {
                    dataProvider.SetButtonState(ControllerDataProvider.Buttons.System, true);
                }
                return false;
            }
        });

        btn_button1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_UP) {
                    dataProvider.SetButtonState(ControllerDataProvider.Buttons.Menu, false);
                }
                else if (action == MotionEvent.ACTION_DOWN) {
                    dataProvider.SetButtonState(ControllerDataProvider.Buttons.Menu, true);
                }
                return false;
            }
        });

        btn_button2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_UP) {
                    dataProvider.SetButtonState(ControllerDataProvider.Buttons.Grip, false);
                }
                else if (action == MotionEvent.ACTION_DOWN) {
                    dataProvider.SetButtonState(ControllerDataProvider.Buttons.Grip, true);
                }
                return false;
            }
        });

        StartDataRefreshThread();
        return view;
    }

    public void SetDataProvider(ControllerDataProvider dataProvider, BackgroundProcessManager backgroundProcessManager) {
        this.dataProvider = dataProvider;
        this.backgroundProcessManager = backgroundProcessManager;
    }

    private String fusedString;
    private void RefreshFusedDataField() {
        fusedString = ControllerDataProvider.Float3ToString(dataProvider.getFusedEulerAngles());
        fusedSensorField.setText(fusedString);
    }

    public String GetWifiIP() {
        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e("WIFI IP", "Unable to get host address.");
            ipAddressString = null;
        }

        return ipAddressString;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        switch (event.getKeyCode())
        {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN)
                {
                    volumeUp_btn.setChecked(true);
                    dataProvider.SetButtonState(ControllerDataProvider.Buttons.TrackpadPress, true);
                }
                else if (action == KeyEvent.ACTION_UP)
                {
                    volumeUp_btn.setChecked(false);
                    dataProvider.SetButtonState(ControllerDataProvider.Buttons.TrackpadPress, false);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN)
                {
                    volumeDwn_btn.setChecked(true);
                    dataProvider.SetButtonState(ControllerDataProvider.Buttons.Trigger, true);
                }
                else if (action == KeyEvent.ACTION_UP)
                {
                    volumeDwn_btn.setChecked(false);
                    dataProvider.SetButtonState(ControllerDataProvider.Buttons.Trigger, false);
                }
                return true;
        }
        return false;
    }

    public void StartDataRefreshThread() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                RefreshFusedDataField();
            }
        }, 0l, sendInterval);
    }

    private void SendButtonPressed(boolean enabled)
    {
        ipField.setEnabled(!enabled);
        portField.setEnabled(!enabled);
        sendIntervalField.setEnabled(!enabled);
        connectionModeToggleGroup.setEnabled(!enabled);
        sendEnabled = enabled;
        if (enabled)
        {
            backgroundProcessManager.StartDataSendOnWifi(sendInterval, targetIP);
        }
        else
        {
            backgroundProcessManager.StopDataSendOnWifi();
        }
    }

    private void RefreshFieldData()
    {
        targetIP = ipField.getText().toString();
        targetPort = Integer.parseInt(portField.getText().toString());
        sendInterval = Long.parseLong(sendIntervalField.getText().toString());
        RadioButton selectedButton = (RadioButton) view.findViewById(connectionModeToggleGroup.getCheckedRadioButtonId());
        connectionMode = WifiModeFragment.ConnectionModes.values()[connectionModeToggleGroup.indexOfChild(selectedButton)];
    }


    @Override
    public void onStop() {
        super.onStop();

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        targetIP = ipField.getText().toString();
        targetPort = Integer.parseInt(portField.getText().toString());
        RadioButton selectedButton = (RadioButton) view.findViewById(connectionModeToggleGroup.getCheckedRadioButtonId());
        connectionMode = WifiModeFragment.ConnectionModes.values()[connectionModeToggleGroup.indexOfChild(selectedButton)];
        sendInterval = Integer.parseInt(sendIntervalField.getText().toString());

        // Save preferences
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(TARGET_IP_KEY, targetIP);
        editor.putInt(TARGET_PORT_KEY, targetPort);
        editor.putInt(CONNECTION_MODE_KEY, connectionMode.ordinal());
        editor.putLong(SEND_INTERVAL_KEY, sendInterval);
        editor.commit();
    }
}
