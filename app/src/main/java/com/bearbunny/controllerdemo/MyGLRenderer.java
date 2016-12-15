package com.bearbunny.controllerdemo;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Peter on 2016.12.15..
 */

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private OrientationIndicator model;
    private float aspect = 1f;

    public MyGLRenderer(Context context) {

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(0f, 0f, 0f, 1f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        aspect = (float) width / height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 90f, aspect, 0.1f, 20f);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glLoadIdentity();
        gl.glTranslatef(0f, 0f, -6f);
        model.draw(gl);
    }

    private class OrientationIndicator {
        private FloatBuffer vertexBuffer;
        private FloatBuffer texBUffer;

        public void draw(GL10 gl) {
            gl.glFrontFace(GL10.GL_CCW);
        }
    }
}
