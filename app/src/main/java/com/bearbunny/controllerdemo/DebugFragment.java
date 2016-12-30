package com.bearbunny.controllerdemo;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.hitlabnz.sensor_fusion_demo.representation.Quaternion;
import org.hitlabnz.sensor_fusion_demo.representation.Vector3f;
import org.w3c.dom.Text;

/**
 * Created by Peter on 2016.12.28..
 */

public class DebugFragment extends Fragment {
    private View view;
    private ControllerDataProvider dataProvider;

    ToggleButton orientationLockBtn;
    ToggleButton dummyOrientationBtn;
    SeekBar seekBar1;
    SeekBar seekBar2;
    SeekBar seekBar3;
    TextView seekBar1_value;
    TextView seekBar2_value;
    TextView seekBar3_value;
    TextView quaternionValue;
    TextView normalizedQuaternionValue;
    TextView quaternionEulerValue;

    private float yaw = 0f;
    private float pitch = 0f;
    private float roll = 0f;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.debug_fragment_layout, container, false);

        orientationLockBtn = (ToggleButton) view.findViewById(R.id.toggleLockOrientation);
        orientationLockBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dataProvider.setOrientationLock(isChecked);
            }
        });
        dummyOrientationBtn = (ToggleButton) view.findViewById(R.id.toggleDummyOrientation);
        dummyOrientationBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dataProvider.setDummyOrientation(isChecked);
            }
        });

        seekBar1 = (SeekBar) view.findViewById(R.id.seekBar1);
        seekBar1_value = (TextView) view.findViewById(R.id.slider1Value);
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // X
                seekBar1_value.setText(Integer.toString(progress));
                yaw = progress;
                dataProvider.getHmdCorrection().setEulerAngle(yaw, pitch, roll);
                quaternionValue.setText(dataProvider.getHmdCorrection().toString());
                double[] euler = dataProvider.getHmdCorrection().toEulerAnglesDeg();
                quaternionEulerValue.setText(euler[0] + "; " + euler[1] + "; " + euler[2]);
                Quaternion temp = new Quaternion();
                temp.normalize();
                normalizedQuaternionValue.setText(temp.toString());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBar2 = (SeekBar) view.findViewById(R.id.seekBar2);
        seekBar2_value = (TextView) view.findViewById(R.id.slider2Value);
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Y
                seekBar2_value.setText(Integer.toString(progress));
                pitch = progress;
                dataProvider.getHmdCorrection().setEulerAngle(yaw, pitch, roll);
                quaternionValue.setText(dataProvider.getHmdCorrection().toString());
                double[] euler = dataProvider.getHmdCorrection().toEulerAnglesDeg();
                quaternionEulerValue.setText(euler[0] + "; " + euler[1] + "; " + euler[2]);
                Quaternion temp = new Quaternion();
                temp.normalize();
                normalizedQuaternionValue.setText(temp.toString());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBar3 = (SeekBar) view.findViewById(R.id.seekBar3);
        seekBar3_value = (TextView) view.findViewById(R.id.slider3Value);
        seekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Z
                seekBar3_value.setText(Integer.toString(progress));
                roll = progress;
                dataProvider.getHmdCorrection().setEulerAngle(yaw, pitch, roll);
                quaternionValue.setText(dataProvider.getHmdCorrection().toString());
                double[] euler = dataProvider.getHmdCorrection().toEulerAnglesDeg();
                quaternionEulerValue.setText(euler[0] + "; " + euler[1] + "; " + euler[2]);
                Quaternion temp = new Quaternion();
                temp.normalize();
                normalizedQuaternionValue.setText(temp.toString());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        quaternionValue = (TextView) view.findViewById(R.id.hmd_quaternion_text);
        normalizedQuaternionValue = (TextView) view.findViewById(R.id.hmd_quaternion_norm_text);
        quaternionEulerValue = (TextView) view.findViewById(R.id.hmd_euler_text);

        return view;
    }

    public void SetDataProvider(ControllerDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }
}
