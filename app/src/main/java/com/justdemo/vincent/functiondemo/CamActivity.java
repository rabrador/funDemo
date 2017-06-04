package com.justdemo.vincent.functiondemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.util.Arrays;

public class CamActivity extends AppCompatActivity {

    private final int REQUEST_CAMERA = 1;
    private Size previewSize = null;
    private TextureView cameraText = null;
    private CameraDevice camDevice = null;
    private CaptureRequest.Builder capBuilder = null;
    private CameraCaptureSession cameraSeeion = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);

        initView();
        cameraText.setSurfaceTextureListener(mSurfaceTextureListener);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();

        } else {
            //user do reject
        }
    }

    private void initView() {
        cameraText = (TextureView) findViewById(R.id.textureView);
    }

    /* invoke onSurfaceTextureAvailable when TextureView is activity */
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        /* Check Permission, if needed */
            if (ActivityCompat.checkSelfPermission(CamActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CamActivity.this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA);
            } else {
                openCamera();
            }

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private void openCamera() {
        CameraManager camMgr = (CameraManager) getSystemService(CAMERA_SERVICE);

        try {
            /* get Camera ID */
            String camId = camMgr.getCameraIdList()[0];
            CameraCharacteristics camChar = camMgr.getCameraCharacteristics(camId);

            StreamConfigurationMap map = camChar.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            previewSize = map.getOutputSizes(SurfaceTexture.class)[0];

            /* Activity Camera */
            if (ContextCompat.checkSelfPermission(CamActivity.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                camMgr.openCamera(camId, mCameraStateCallback, null);
            }


        } catch (Exception e) {
            Toast.makeText(this, "Open camera Error", Toast.LENGTH_LONG).show();
            //Error
        }

    }

    private CameraDevice.StateCallback mCameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            camDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {

        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    };

    private CameraCaptureSession.StateCallback mCameraCaptureSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            closeAllCameraSession();

            cameraSeeion = session;
            capBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            HandlerThread backThread = new HandlerThread("CameraPreview");
            backThread.start();
            Handler backHandler = new Handler(backThread.getLooper());

            try {
                cameraSeeion.setRepeatingRequest(capBuilder.build(), null, backHandler);
            } catch (Exception e) {

            }


        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };

    private void startPreview() {
        SurfaceTexture surfaceText = cameraText.getSurfaceTexture();
        surfaceText.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

        Surface surface = new Surface(surfaceText);

        try {
            capBuilder = camDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (Exception e) {
            //Error
        }

        capBuilder.addTarget(surface);

        try {
            camDevice.createCaptureSession(Arrays.asList(surface), mCameraCaptureSessionCallback, null);
        } catch (Exception e) {
            //Error
        }
    }

    private void closeAllCameraSession() {
        if (cameraSeeion != null) {
            cameraSeeion.close();
            cameraSeeion = null;
        }
    }
}
