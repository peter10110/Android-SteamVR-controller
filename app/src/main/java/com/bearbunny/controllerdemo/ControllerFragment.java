package com.bearbunny.controllerdemo;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by Peter on 2016.12.15..
 */

public class ControllerFragment extends Fragment {
    private View view;
    private ControllerDataProvider dataProvider;
    private BackgroundProcessManager processManager;

    private TextView menuButton;
    private TextView systemButton;
    private TextView gripButton;
    private TextView trackpadPress;
    private TextView triggerPress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.controller_layout, container, false);


        menuButton = (TextView) view.findViewById(R.id.menu_btn);
        menuButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    OnButtonTouchEvent(menuButton, true);
                    dataProvider.setButtonState(ControllerDataProvider.Buttons.Menu, true);
                }
                else if (action == MotionEvent.ACTION_UP) {
                    OnButtonTouchEvent(menuButton, false);
                    dataProvider.setButtonState(ControllerDataProvider.Buttons.Menu, false);
                }

                return true;
            }
        });

        systemButton = (TextView) view.findViewById(R.id.system_btn);
        systemButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    OnButtonTouchEvent(systemButton, true);
                    dataProvider.setButtonState(ControllerDataProvider.Buttons.System, true);
                }
                else if (action == MotionEvent.ACTION_UP) {
                    OnButtonTouchEvent(systemButton, false);
                    dataProvider.setButtonState(ControllerDataProvider.Buttons.System, false);
                }

                return true;
            }
        });

        gripButton = (TextView) view.findViewById(R.id.grip_btn);
        gripButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    OnButtonTouchEvent(gripButton, true);
                    dataProvider.setButtonState(ControllerDataProvider.Buttons.Grip, true);
                }
                else if (action == MotionEvent.ACTION_UP) {
                    OnButtonTouchEvent(gripButton, false);
                    dataProvider.setButtonState(ControllerDataProvider.Buttons.Grip, false);
                }

                return true;
            }
        });

        triggerPress = (TextView) view.findViewById(R.id.trigger_button);
        trackpadPress = (TextView) view.findViewById(R.id.trackpad_press);

        return view;
    }

    private void OnButtonTouchEvent(TextView button, boolean touchDown) {
        if (touchDown) {
            button.setBackgroundResource(R.drawable.circle_highlighted);
        }
        else {
            button.setBackgroundResource(R.drawable.circle);
        }
    }

    private void OnControlActivatedEvent(TextView label, boolean down) {
        if (down) {
            label.setTextColor(getResources().getColor(R.color.text_color_highlighted));
        }
        else {
            label.setTextColor(getResources().getColor(R.color.text_color));
        }
    }


    public void SetDataProvider(ControllerDataProvider dataProvider, BackgroundProcessManager backgroundProcessManager) {
        this.dataProvider = dataProvider;
        this.processManager = backgroundProcessManager;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        switch (event.getKeyCode())
        {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN)
                {
                    OnControlActivatedEvent(trackpadPress, true);
                    dataProvider.setButtonState(ControllerDataProvider.Buttons.TrackpadPress, true);
                }
                else if (action == KeyEvent.ACTION_UP)
                {
                    OnControlActivatedEvent(trackpadPress, false);
                    dataProvider.setButtonState(ControllerDataProvider.Buttons.TrackpadPress, false);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN)
                {
                    OnControlActivatedEvent(triggerPress, true);
                    dataProvider.setButtonState(ControllerDataProvider.Buttons.Trigger, true);
                }
                else if (action == KeyEvent.ACTION_UP)
                {
                    OnControlActivatedEvent(triggerPress, false);
                    dataProvider.setButtonState(ControllerDataProvider.Buttons.Trigger, false);
                }
                return true;
        }
        return false;
    }
}
