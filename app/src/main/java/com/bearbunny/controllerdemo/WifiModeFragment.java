package com.bearbunny.controllerdemo;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;

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
    private TextView fusedQuaternionField;
    private Button btn_zeroPoint;

    private enum ConnectionModes {
        UDP, TCP
    }

    private String targetIP = "192.168.0.12";
    private int targetPort = 5555;
    private long sendInterval = 16l;
    private WifiModeFragment.ConnectionModes connectionMode = WifiModeFragment.ConnectionModes.UDP;

    private Timer timer;
    private Handler handler;
    private Boolean sendEnabled = false;

    private ControllerDataProvider dataProvider;
    private BackgroundProcessManager backgroundProcessManager;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.wifi_mode_layout, container, false);

        this.view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        // Restore preferences
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        targetIP = settings.getString(TARGET_IP_KEY, targetIP);
        targetPort = settings.getInt(TARGET_PORT_KEY, targetPort);
        sendInterval = settings.getLong(SEND_INTERVAL_KEY, 8l);
        connectionMode = WifiModeFragment.ConnectionModes.values()[settings.getInt(CONNECTION_MODE_KEY, 0)];

        ownIPField = (TextView) view.findViewById(R.id.ipTextView);
        ownIPField.setText("IP: " + GetWifiIP());
        ownIPField.clearFocus();

        ipField = (EditText) view.findViewById(R.id.targetIPField);
        ipField.setText(targetIP);
        ipField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    sendIntervalField.clearFocus();
                    sendIntervalField.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
                return false;
            }
        });

        portField = (EditText) view.findViewById(R.id.targetPortField);
        portField.setText(String.valueOf(targetPort));
        ipField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    sendIntervalField.clearFocus();
                    sendIntervalField.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
                return false;
            }
        });

        sendIntervalField = (EditText) view.findViewById(R.id.intervalField);
        sendIntervalField.setText(String.valueOf(sendInterval));
        sendIntervalField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    sendIntervalField.clearFocus();
                    sendIntervalField.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
                return false;
            }
        });

        connectionModeToggleGroup = (RadioGroup) view.findViewById(R.id.modeRadio);
        connectionModeToggleGroup.check(connectionModeToggleGroup.getChildAt(connectionMode.ordinal()).getId());

        sendDataModeToggle = (ToggleButton) view.findViewById(R.id.sendDataToggle);
        sendDataModeToggle.setChecked(backgroundProcessManager.getWifiSendingState());
        sendDataModeToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendButtonPressed(sendDataModeToggle.isChecked());
            }
        });

        fusedSensorField = (TextView) view.findViewById(R.id.fusedValue);
        fusedQuaternionField = (TextView) view.findViewById(R.id.fusedQuatValue);

        btn_button0 = (Button) view.findViewById(R.id.button0);
        btn_button1 = (Button) view.findViewById(R.id.button1);
        btn_button2 = (Button) view.findViewById(R.id.button2);
        volumeUp_btn = (ToggleButton) view.findViewById(R.id.volUpTgBtn);
        volumeDwn_btn = (ToggleButton) view.findViewById(R.id.volDwnTgBtn);
        btn_zeroPoint = (Button) view.findViewById(R.id.zeroPointButton);
        btn_zeroPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataProvider.setCurrentOrientationAsCenter();
            }
        });

        btn_button0.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_UP) {
                    dataProvider.setButtonState(ControllerDataProvider.Buttons.System, false);
                }
                else if (action == MotionEvent.ACTION_DOWN) {
                    dataProvider.setButtonState(ControllerDataProvider.Buttons.System, true);
                }
                return false;
            }
        });

        btn_button1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_UP) {
                    dataProvider.setButtonState(ControllerDataProvider.Buttons.Menu, false);
                }
                else if (action == MotionEvent.ACTION_DOWN) {
                    dataProvider.setButtonState(ControllerDataProvider.Buttons.Menu, true);
                }
                return false;
            }
        });

        btn_button2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_UP) {
                    dataProvider.setButtonState(ControllerDataProvider.Buttons.Grip, false);
                }
                else if (action == MotionEvent.ACTION_DOWN) {
                    dataProvider.setButtonState(ControllerDataProvider.Buttons.Grip, true);
                }
                return false;
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        StartDataRefreshThread();
    }

    public void SetDataProvider(ControllerDataProvider dataProvider, BackgroundProcessManager backgroundProcessManager) {
        this.dataProvider = dataProvider;
        this.backgroundProcessManager = backgroundProcessManager;
    }

    private String fusedString;
    private void RefreshFusedDataField() {
        fusedString = ControllerDataProvider.Float3ToString(dataProvider.getFusedEulerAngles());
        fusedSensorField.setText(fusedString);
        fusedQuaternionField.setText(ControllerDataProvider.QuaternionToString(dataProvider.getFusedQuaternion()));
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
                    dataProvider.setButtonState(ControllerDataProvider.Buttons.TrackpadPress, true);
                }
                else if (action == KeyEvent.ACTION_UP)
                {
                    volumeUp_btn.setChecked(false);
                    dataProvider.setButtonState(ControllerDataProvider.Buttons.TrackpadPress, false);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN)
                {
                    volumeDwn_btn.setChecked(true);
                    dataProvider.setButtonState(ControllerDataProvider.Buttons.Trigger, true);
                }
                else if (action == KeyEvent.ACTION_UP)
                {
                    volumeDwn_btn.setChecked(false);
                    dataProvider.setButtonState(ControllerDataProvider.Buttons.Trigger, false);
                }
                return true;
        }
        return false;
    }

    public void StartDataRefreshThread() {
        handler = new Handler();

        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        RefreshFusedDataField();
                    }
                });
            }
        }, 0l, sendInterval);
    }

    private void SendButtonPressed(boolean enabled)
    {
        RefreshFieldData();
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
