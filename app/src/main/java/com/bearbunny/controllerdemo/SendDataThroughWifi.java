package com.bearbunny.controllerdemo;

import android.app.Activity;

import org.hitlabnz.sensor_fusion_demo.representation.Quaternion;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Peter on 2016.12.19..
 */

public class SendDataThroughWifi {
    private Activity activity;
    private ControllerDataProvider dataProvider;

    private final String timestamp_TAG = "TMST";
    private final String fusedOrientation_TAG = "FO";
    private final String fusedQuaternion_TAG = "FQ";
    private final String hmdCorrection_TAG = "HMDC";
    private final String setCenter_TAG = "SCT";
    private final String buttons_TAG = "BTN";
    private final String trackpad_TAG = "TRK";
    private final String close_TAG = "END";
    private long packet_timestamp;
    private String packet_message;
    private int buttonState;
    private DatagramPacket packet = null;
    private DatagramSocket datagramSocket = null;
    private InetAddress target;
    private String targetIP = "000.000.000.000";
    private int targetPort = 5555;
    private long sendInterval = 8l;

    public SendDataThroughWifi(Activity activity, ControllerDataProvider dataProvider) {
        this.activity = activity;
        this.dataProvider = dataProvider;
    }

    public Boolean InitSendThroughWifi(String targetIP)
    {
        try {
            this.targetIP = targetIP;
            datagramSocket = new DatagramSocket();
            target = InetAddress.getByName(targetIP);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void SendData() {
        try {
            packet_message = GetPacketMessage();
            System.out.println("Packet message: " + packet_message);
            if (packet == null) {
                byte[] bytes = packet_message.getBytes();
                packet = new DatagramPacket(bytes, bytes.length, target, targetPort);
            }
            else {
                packet.setData(packet_message.getBytes());
            }
            datagramSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String GetPacketMessage()
    {
        packet_timestamp = System.nanoTime();
        buttonState = 0;

        if (dataProvider.getButtonState(ControllerDataProvider.Buttons.TrackpadPress.Trigger)) {
            buttonState += 16;
        }

        if (dataProvider.getButtonState(ControllerDataProvider.Buttons.TrackpadPress.TrackpadPress)) {
            buttonState += 8;
        }

        if (dataProvider.getButtonState(ControllerDataProvider.Buttons.TrackpadPress.Menu)) {
            buttonState += 4;
        }

        if (dataProvider.getButtonState(ControllerDataProvider.Buttons.TrackpadPress.System)) {
            buttonState += 2;
        }

        if (dataProvider.getButtonState(ControllerDataProvider.Buttons.TrackpadPress.Grip)) {
            buttonState += 1;
        }

        float[] fusedData = dataProvider.getFusedEulerAngles();
        Quaternion fusedQuaternion = dataProvider.getFusedQuaternion();

        return dataProvider.getWhichHand() + "#"
                + timestamp_TAG + ";" + packet_timestamp + ";"
                + fusedOrientation_TAG + ";"
                + fusedData[1] + ";" + fusedData[0] + ";" + -fusedData[2] +";"
                + fusedQuaternion_TAG + ";"
                + (fusedQuaternion.getW()) + ";" + (fusedQuaternion.getX()) + ";" + (fusedQuaternion.getY()) + ";" + (fusedQuaternion.getZ()) + ";"
                + setCenter_TAG + ";" + (dataProvider.getAndClearResetCenterButton() ? 1 : 0) + ";"
                + buttons_TAG + ";" + buttonState + ";"
                + trackpad_TAG + ";"
                + (dataProvider.getTracpadTouched() ? 1 : 0) + ";" + dataProvider.getTrackpadX() + ";" + dataProvider.getTrackpadY() + ";"
                + close_TAG;
    }
}
