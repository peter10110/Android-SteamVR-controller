package com.bearbunny.controllerdemo;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.hitlabnz.sensor_fusion_demo.OrientationVisualisationFragment;

public class MainActivity extends Activity {
    // Navigation drawer
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mDrawerItems;

    // Fragment controls
    private static int currentFragmentIndex = 0;
    private Fragment currentFragment = null;

    // Sensor and controller data
    private ControllerDataProvider dataProvider;
    private BackgroundProcessManager backgroundProcessManager;

    // Settings
    private final int sensorSpeed = SensorManager.SENSOR_DELAY_FASTEST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.nav_drawer_layout).setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE);


        // Navigation drawer
        mDrawerItems = getResources().getStringArray(R.array.drawer_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_navigation_drawer);
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, mDrawerItems));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        Init();

        // Load the first segment
        FragmentManager fragmentManager = getFragmentManager();
        currentFragment = new ControllerFragment();
        currentFragmentIndex = 1;
        fragmentManager.beginTransaction().replace(R.id.content_frame, currentFragment).commit();
        ((ControllerFragment) currentFragment).SetDataProvider(dataProvider, backgroundProcessManager);
    }

    private void Init()
    {
        dataProvider = new ControllerDataProvider(this, sensorSpeed);
        backgroundProcessManager = new BackgroundProcessManager(this, dataProvider);
    }

    private class DrawerItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SelectItem(position);
        }

        private void SelectItem(int position) {
            switch (position) {
                case 0:
                    currentFragment = new WifiModeFragment();
                    ((WifiModeFragment) currentFragment).SetDataProvider(dataProvider, backgroundProcessManager);
                    break;
                case 1:
                    currentFragment = new ControllerFragment();
                    ((ControllerFragment) currentFragment).SetDataProvider(dataProvider, backgroundProcessManager);

                    break;
                case 2:
                    currentFragment = new OrientationVisualisationFragment();
                    ((OrientationVisualisationFragment) currentFragment).SetDataProvider(dataProvider);
                    break;
            }

            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, currentFragment).commit();
            currentFragmentIndex = position;

            mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (currentFragmentIndex == 0) {
            // The WiFi mode is selected currently
            return ((WifiModeFragment) currentFragment).dispatchKeyEvent(event);
        }
        else if (currentFragmentIndex == 1) {
            // The controller mode is selected currently
            return ((ControllerFragment) currentFragment).dispatchKeyEvent(event);
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dataProvider != null) {
            dataProvider.OnPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (backgroundProcessManager != null) {
            backgroundProcessManager.OnStop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dataProvider != null) {
            dataProvider.OnResume();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }
}
