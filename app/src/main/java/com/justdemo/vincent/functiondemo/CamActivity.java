package com.justdemo.vincent.functiondemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class CamActivity extends AppCompatActivity implements LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationManager locationMgr;
    private final int REQUEST_CAMERA = 1;
    private final int REQUEST_LOCATION = 2;
    private Size previewSize = null;
    private TextureView cameraText = null;
    private CameraDevice camDevice = null;
    private CaptureRequest.Builder capBuilder = null;
    private CameraCaptureSession cameraSeeion = null;
    private FrameLayout frameAR;
    private String accelData = "Accelerometer Data";
    private String compassData = "Compass Data";
    private double x, y = 0;
    private int w, h = 0;
    private Rect areaRect;
    ArrayList<Touris> dbTouris = new ArrayList<>();
    private String data;
    private String[] LongitudeArr;
    private String[] LatitudeArr;
    private String[] namesArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);

        initView();
        cameraText.setSurfaceTextureListener(mSurfaceTextureListener);

        OverlayView arContent = new OverlayView(getApplicationContext());
        frameAR.addView(arContent);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .build();

        }

        locationMgr = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        /* Check Permission, if needed */
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            String provider = this.locationMgr.getBestProvider(new Criteria(), true);
            Location location = locationMgr.getLastKnownLocation(provider);
            //Toast.makeText(CamActivity.this, String.valueOf(location.getLatitude()) + ", "+ String.valueOf(location.getLongitude()), Toast.LENGTH_LONG).show();
            Log.d("GPS", String.valueOf(location.getLatitude()) + ", " + String.valueOf(location.getLongitude()));
        }

        // Load Raw file and covert to String
        covRawToString(loadRawFile());

        // Parser json data
        parserJson(data);

        // insert to ListView
        LongitudeArr = new String[dbTouris.size()];
        LatitudeArr = new String[dbTouris.size()];
        namesArr = new String[dbTouris.size()];

        for (int i = 0; i < dbTouris.size(); i++) {
            LongitudeArr[i] = dbTouris.get(i).getLongitude();
            LatitudeArr[i] = dbTouris.get(i).getLatitude();
            namesArr[i] = dbTouris.get(i).getName();
            Log.d("Coordinate", "Longitude: " + LongitudeArr[i].toString() + ", Latitude: " + LatitudeArr[i].toString());
        }
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
        frameAR = (FrameLayout) findViewById(R.id.framelayout);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Toast.makeText(CamActivity.this, String.valueOf(location.getLongitude()), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(CamActivity.this, "GPS NULL", Toast.LENGTH_LONG).show();
        }
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

    public class OverlayView extends View implements SensorEventListener {
        public OverlayView(Context context) {
            super(context);

            SensorManager sensors = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            Sensor accelSensor = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Sensor compassSensor = sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            Sensor gyroSensor = sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

            boolean isAccelAvailable = sensors.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
            boolean isCompassAvailable = sensors.registerListener(this, compassSensor, SensorManager.SENSOR_DELAY_NORMAL);
            boolean isGyroAvailable = sensors.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            createNewObj(canvas, Math.random() * 100, Math.random() * 50, 0);
            createNewObj(canvas, Math.random() * 600, Math.random() * 300, 1);

//            Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//            contentPaint.setTextAlign(Paint.Align.CENTER);
//            contentPaint.setTextSize(40);
//            contentPaint.setColor(Color.RED);
//            canvas.drawText(accelData, canvas.getWidth() / 2, canvas.getHeight() / 4, contentPaint);
//
//            Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//            contentPaint.setColor(Color.GREEN);
//            contentPaint.setTextSize(50);
//
//            x = Math.random() * 23;
//            y = Math.random() * 51;
//            w = ((int) x) + 440;
//            h = ((int) y) + 160;
//            areaRect = new Rect(((int) x) - 30, ((int) y), w, h);
//            canvas.drawRect(areaRect, contentPaint);
//
//            RectF rectf = new RectF(areaRect);
//            // measure text width
//            //rectf.right = contentPaint.measureText(accelData, 0, accelData.length());
//            // measure text height
//            //rectf.bottom = contentPaint.descent() - contentPaint.ascent();
//
//            rectf.left += (areaRect.width() ) / 4.0f;
//            rectf.top += (areaRect.height() ) / 4.0f;
//
//            contentPaint.setColor(Color.WHITE);
//            canvas.drawText(namesArr[0], rectf.left, rectf.top - contentPaint.ascent(), contentPaint);
//
//            canvas.drawText(namesArr[1], rectf.left, rectf.top - contentPaint.ascent(), contentPaint);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            StringBuilder msg = new StringBuilder(event.sensor.getName()).append(" ");
            for (float value : event.values) {
                msg.append("[").append(value).append("]");
            }

            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    accelData = msg.toString();
                    break;
//                case Sensor.TYPE_GYROSCOPE:
//                    gyroData = msg.toString();
//                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    compassData = msg.toString();
                    break;
            }

            this.invalidate(); //well be call OnDraw() always
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    public InputStream loadRawFile() {
        return getResources().openRawResource(R.raw.data);
    }

    public String covRawToString(InputStream inputStream) {

        try {
            int size = inputStream.available();

            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            data = new String(buffer, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    public void parserJson(String sJson) {
        try {
            JSONObject obj = new JSONObject(sJson);
            JSONArray array = obj.getJSONArray("data");

            for (int i = 0; i < array.length(); i++) {
                JSONObject subObj = array.getJSONObject(i);

                Touris touris = new Touris(subObj.getString("Name"), subObj.getString("Title"), subObj.getString("Introduction"),
                        subObj.getString("Nlat"), subObj.getString("Elong"));
                dbTouris.add(touris);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createNewObj(Canvas canvas, Double x, Double y, int index) {
        Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        contentPaint.setColor(Color.GREEN);
        contentPaint.setTextSize(50);

        w = x.intValue() + 440;
        h = y.intValue() + 160;
        areaRect = new Rect(x.intValue() - 30, y.intValue(), w, h);
        canvas.drawRect(areaRect, contentPaint);

        RectF rectf = new RectF(areaRect);
        rectf.left += (areaRect.width()) / 4.0f;
        rectf.top += (areaRect.height()) / 4.0f;

        contentPaint.setColor(Color.WHITE);
        canvas.drawText(namesArr[index], rectf.left, rectf.top - contentPaint.ascent(), contentPaint);
    }
}
