package org.hitlabnz.sensor_fusion_demo;

import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;

import com.bearbunny.controllerdemo.ControllerDataProvider;
import com.bearbunny.controllerdemo.R;

import org.hitlabnz.sensor_fusion_demo.orientationProvider.AccelerometerCompassProvider;
import org.hitlabnz.sensor_fusion_demo.orientationProvider.CalibratedGyroscopeProvider;
import org.hitlabnz.sensor_fusion_demo.orientationProvider.GravityCompassProvider;
import org.hitlabnz.sensor_fusion_demo.orientationProvider.ImprovedOrientationSensor1Provider;
import org.hitlabnz.sensor_fusion_demo.orientationProvider.ImprovedOrientationSensor2Provider;
import org.hitlabnz.sensor_fusion_demo.orientationProvider.OrientationProvider;
import org.hitlabnz.sensor_fusion_demo.orientationProvider.RotationVectorProvider;

import static android.content.Context.SENSOR_SERVICE;

/**
 * A fragment that contains the same visualisation for different orientation providers
 */
public class OrientationVisualisationFragment extends android.app.Fragment {
    /**
     * The surface that will be drawn upon
     */
    private GLSurfaceView mGLSurfaceView;
    /**
     * The class that renders the cube
     */
    private CubeRenderer mRenderer;
    /**
     * The current orientation provider that delivers device orientation.
     */
    private OrientationProvider currentOrientationProvider;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    public static final String ARG_SECTION_NUMBER = "section_number";

    private View view;

    @Override
    public void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        if (currentOrientationProvider != null) {
            currentOrientationProvider.start();
        }
        mGLSurfaceView.onResume();
    }

    @Override
    public void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        currentOrientationProvider.stop();
        mGLSurfaceView.onPause();
    }

    public void SetDataProvider(ControllerDataProvider dataProvider) {
        this.currentOrientationProvider = dataProvider.GetOrientationProvider();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_orientation_display, container, false);
        // Initialise the orientationProvider
//        switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
//        case 1:
//            currentOrientationProvider = new ImprovedOrientationSensor1Provider((SensorManager) getActivity()
//                    .getSystemService(SensorSelectionActivity.SENSOR_SERVICE));
//            break;
//        case 2:
//            currentOrientationProvider = new ImprovedOrientationSensor2Provider((SensorManager) getActivity()
//                    .getSystemService(SensorSelectionActivity.SENSOR_SERVICE));
//            break;
//        case 3:
//            currentOrientationProvider = new RotationVectorProvider((SensorManager) getActivity().getSystemService(
//                    SensorSelectionActivity.SENSOR_SERVICE));
//            break;
//        case 4:
//            currentOrientationProvider = new CalibratedGyroscopeProvider((SensorManager) getActivity()
//                    .getSystemService(SensorSelectionActivity.SENSOR_SERVICE));
//            break;
//        case 5:
//            currentOrientationProvider = new GravityCompassProvider((SensorManager) getActivity().getSystemService(
//                    SensorSelectionActivity.SENSOR_SERVICE));
//            break;
//        case 6:
//            currentOrientationProvider = new AccelerometerCompassProvider((SensorManager) getActivity()
//                    .getSystemService(SensorSelectionActivity.SENSOR_SERVICE));
//            break;
//        default:
//            break;
//        }

        // Create our Preview view and set it as the content of our Activity
        mRenderer = new CubeRenderer();
        mRenderer.setOrientationProvider(currentOrientationProvider);
        mGLSurfaceView = new GLSurfaceView(getActivity());
        mGLSurfaceView = (GLSurfaceView) view.findViewById(R.id.orientetionSurfaceView);
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLSurfaceView.setRenderer(mRenderer);

        mGLSurfaceView.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                mRenderer.toggleShowCubeInsideOut();
                return true;
            }
        });

        //return mGLSurfaceView;
        return view;
    }
}