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

public class WifiModeFragment extends Fragment implements SensorEventListener {
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
    private ToggleButton volumeUp_btn;
    private ToggleButton volumeDwn_btn;

    // Navigation drawer
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mDrawerItems;

    private enum ConnectionModes {
        UDP, TCP
    }

    private String targetIP = "000.000.000.000";
    private int targetPort = 5555;
    private long sendInterval = 8l;
    private WifiModeFragment.ConnectionModes connectionMode = WifiModeFragment.ConnectionModes.UDP;

    private Timer timer;
    private Boolean sendEnabled = false;
    private SensorManager sensorManager;
    private Sensor linAccelerationSensor;

    private boolean volUpPressed = false;
    private boolean volDwnPressed = false;
    private boolean btn_r2_pressed = false;

    DatagramPacket packet = null;
    DatagramSocket datagramSocket = null;

    private float[] fusedEulerAngles;

    /**
     * Fused sensor data provider
     */
    private OrientationProvider currentOrientationProvider;

    private final int sensorSpeed = SensorManager.SENSOR_DELAY_FASTEST;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.wifi_mode_layout, container, false);

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
        volumeUp_btn = (ToggleButton) view.findViewById(R.id.volUpTgBtn);
        volumeDwn_btn = (ToggleButton) view.findViewById(R.id.volDwnTgBtn);

        fusedEulerAngles = new float[3];

        sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        linAccelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        currentOrientationProvider = new ImprovedOrientationSensor1Provider(sensorManager);

        return view;
    }



    @Override
    public void onPause() {
        super.onPause();
        currentOrientationProvider.stop();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, linAccelerationSensor, sensorSpeed);
        currentOrientationProvider.start();
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        switch (event.getKeyCode())
        {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN)
                {
                    volumeUp_btn.setChecked(true);
                    volUpPressed = true;
                }
                else if (action == KeyEvent.ACTION_UP)
                {
                    volumeUp_btn.setChecked(false);
                    volUpPressed = false;
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN)
                {
                    volumeDwn_btn.setChecked(true);
                    volDwnPressed = true;
                }
                else if (action == KeyEvent.ACTION_UP)
                {
                    volumeDwn_btn.setChecked(false);
                    volDwnPressed = false;
                }
                return true;
            case KeyEvent.KEYCODE_BUTTON_R2:
            {
                if (action == KeyEvent.ACTION_DOWN)
                {
                    btn_r2_pressed = true;
                }
                else if (action == KeyEvent.ACTION_UP)
                {
                    btn_r2_pressed = false;
                }
                return true;
            }
        }
        return false;
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
            RefreshFieldData();
            StartDataSend();
        }
        else
        {
            timer.cancel();
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


    private final String timestamp_TAG = "TMST";
    private final String fusedOrientation_TAG = "FO";
    private final String buttons_TAG = "BTN";
    private final String close_TAG = "END";
    private long packet_timestamp;
    private String packet_message;
    private int buttonState;

    private String GetPacketMessage()
    {
        packet_timestamp = System.nanoTime();
        buttonState = 0;
        if (btn_button0.isPressed())
        {
            buttonState += 1;
        }

        if (btn_button1.isPressed())
        {
            buttonState += 2;
        }

        if (volUpPressed)
        {
            buttonState += 4;
        }

        if (volDwnPressed)
        {
            buttonState += 8;
        }

        if (btn_r2_pressed)
        {
            buttonState += 16;
        }

        return timestamp_TAG + ";" + packet_timestamp + ";" +
                fusedOrientation_TAG + ";" + fusedEulerAngles[1] +";" + fusedEulerAngles[0] +";" + -fusedEulerAngles[2] +";" +
                buttons_TAG + ";" + buttonState + ";" + close_TAG;
    }

    private InetAddress target;
    private void StartDataSend()
    {
        try {
            datagramSocket = new DatagramSocket();
            target = InetAddress.getByName(targetIP);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (datagramSocket != null) {
            timer = new Timer(true);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        packet_message = GetPacketMessage();
                        System.out.println("Packet message: " + packet_message);
                        if (packet == null) {
                            byte[] bytes = packet_message.getBytes();
                            packet = new DatagramPacket(bytes, bytes.length, target, targetPort);
                        }
                        else
                        {
                            packet.setData(packet_message.getBytes());
                        }
                        datagramSocket.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 0l, sendInterval);
        }
    }

    private String GetWifiIP()
    {
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

    @Override
    public void onStop() {
        super.onStop();

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

    @Override
    public void onSensorChanged(SensorEvent event) {
        RefreshFusedOrientationData();
    }

    private void RefreshFusedOrientationData()
    {
        currentOrientationProvider.getEulerAngles(fusedEulerAngles);
        fusedEulerAngles[0] *= 57.2957795f;
        fusedEulerAngles[1] *= 57.2957795f;
        fusedEulerAngles[2] *= 57.2957795f;
        fusedSensorField.setText(Float3ToString(fusedEulerAngles));
    }

    private final String format = "%6.2f";
    private String Float3ToString(float[] values)
    {
        return String.format(Locale.US, format, values[0]) + "; " +String.format(Locale.US, format, values[1]) + "; " +
                String.format(Locale.US, format, values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
