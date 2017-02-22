package com.bearbunny.controllerdemo;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Peter on 2016.12.15..
 */

public class ControllerFragment extends Fragment {
    private View view;
    private ControllerDataProvider dataProvider;
    private BackgroundProcessManager processManager;

    private PowerManager.WakeLock mWakeLock;

    private Button resetButton;
    private TextView menuButton;
    private TextView systemButton;
    private TextView gripButton;
    private TextView trackpadPress;
    private TextView triggerPress;
    private LinearLayout trackpad;

    private SurfaceView trackpadTouchPoint;
    private SurfaceHolder holder;
    private Paint paint;
    private float currentTouchpointX = 0f;
    private float currentTouchpointY = 0f;
    private float density;
    private static final float trackpadDiameter = 250f;

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

        InitTouchPoint();
        trackpad = (LinearLayout) view.findViewById(R.id.trackpad);
        trackpad.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    OnTrackpadTouchEvent(true);
                    currentTouchpointX = event.getX();
                    currentTouchpointY = event.getY();
                    dataProvider.SetTrackpadPosition(true, ClampTrackpadAxis(currentTouchpointX),
                            ClampTrackpadAxis(currentTouchpointY));
                }
                else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_OUTSIDE) {
                    OnTrackpadTouchEvent(false);
                    currentTouchpointX = 0f;
                    currentTouchpointY = 0f;
                    dataProvider.SetTrackpadPosition(false, 0f, 0f);
                }
                else if (action == MotionEvent.ACTION_MOVE) {
                    currentTouchpointX = event.getX();
                    currentTouchpointY = event.getY();

                    if (CheckTouchpadBounds(currentTouchpointX, currentTouchpointY)) {
                        DrawTouchpoint(currentTouchpointX, currentTouchpointY);
                        dataProvider.SetTrackpadPosition(true, ClampTrackpadAxis(currentTouchpointX),
                                ClampTrackpadAxis(currentTouchpointY));
                    }
                }
                return true;
            }
        });

        resetButton = (Button) view.findViewById(R.id.controllerResetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataProvider.setCurrentOrientationAsCenter();
            }
        });

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        density = displayMetrics.density;

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        return view;
    }

    private float ClampTrackpadAxis(float value) {
        float clamped = ((value / density) - 125f) / 125f;
        if (clamped > 1f)
            return 1f;
        else
            return clamped;
    }

    private void InitTouchPoint() {
        trackpadTouchPoint = (SurfaceView) view.findViewById(R.id.trackpad_surfaceView);
        trackpadTouchPoint.setZOrderOnTop(true);
        holder = trackpadTouchPoint.getHolder();
        holder.setFormat(PixelFormat.TRANSPARENT);
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.touchpoint));
    }

    private boolean CheckTouchpadBounds(float pointX, float pointY) {
        final float center = trackpadDiameter / 2f;
        float x = center - pointX / density;
        float y = center - pointY / density;
        float distance = (float) Math.sqrt(x*x + y*y);
        return distance < center;
    }

    private void OnTrackpadTouchEvent(boolean touched) {
        if (touched == false) {
            ClearTouchpoint();
        }
    }

    private void DrawTouchpoint(float x, float y) {
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawCircle(x, y, 15f * density, paint);
        holder.unlockCanvasAndPost(canvas);
    }

    private void DrawTrackpadBounds(Canvas canvas) {
        Paint boundsPaint = new Paint();
        boundsPaint.setColor(Color.RED);
        Log.d("Canvas debug", "Width: " + canvas.getWidth() + ", height: " + canvas.getHeight());
        canvas.drawCircle(canvas.getWidth() / 2f, canvas.getHeight() / 2f, (trackpadDiameter / 2f) * density, boundsPaint);
    }

    private void ClearTouchpoint() {
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        holder.unlockCanvasAndPost(canvas);
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
