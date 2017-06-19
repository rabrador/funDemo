package com.justdemo.vincent.functiondemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CamActivity extends AppCompatActivity implements LocationListener {
    /*********************
     * DEFINE VARIABLE
     ********************/
    private final boolean DEBUG_MESSAGE = true;
    private final boolean TEST_TRACE_CODE = true;
    private final int REQUEST_CAMERA = 1;
    private final int REQUEST_LOCATION = 2;
    private final int REQUEST_SCREEN_SHOT = 3;
    private final int AR_OBJECT_WIDTH = 400;
    private final int MAXIMUM_NUM_DISPLAY_AR = 2;
    private final int MAXIMUM_DISTANCE = 5000; //meter
    private final int MINIMUM_DISTANCE_TO_SHOW = 5000;
    /***********************************************************/

    /******************** LOCAL VARIABLE ********************/
    private GoogleApiClient mGoogleApiClient;
    private LocationManager locationMgr;
    private boolean isLocatOK = false;
    private Size previewSize = null;
    private TextureView cameraText = null;
    private CameraDevice camDevice = null;
    private CaptureRequest.Builder capBuilder = null;
    private CameraCaptureSession cameraSeeion = null;
    private FrameLayout frameAR;
    private String accelData = "Accelerometer Data";
    private String compassData = "Compass Data";
    private String gyroData = "";
    private double x, y = 0;
    private int w, h = 0;
    private Rect areaRect;
    ArrayList<Touris> dbTouris = new ArrayList<>();
    private String data;
    private String[] LongitudeArr;
    private String[] LatitudeArr;
    private String[] namesArr;
    private double xCoordinate[];
    private double yCoordinate[];
    private double sampleXCoord[] = {600, 300, 700};
    private double sampleYCoord[] = {400, 1200, 1400};
    private int arOri[];
    private Location myLocat;
    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];
    private int myOri;
    private Bitmap arNotFound;
    private Bitmap arLocatMark;
    private ImageButton btnScreen;
    private Canvas screenShotCanvas;
    private int screenHeight;
    private int screenWidth;
    private ArChar dbAR = new ArChar();
    private int dispCount = 0;
    /***********************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Full screen */
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_cam);

        initView();

        /* Get Screen height and width */
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
        dbAR.setWindowHeight(screenHeight);
        dbAR.setWindowWidth(screenWidth);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.CAMERA}, REQUEST_LOCATION);
        } else {
            getMyLocation();
            isLocatOK = true;
        }

        arNotFound = BitmapFactory.decodeResource(getResources(), R.drawable.locat_not_found);
        arLocatMark = BitmapFactory.decodeResource(getResources(), R.drawable.ar_locat_mark);
        cameraText.setSurfaceTextureListener(mSurfaceTextureListener);

//        OverlayView arContent = new OverlayView(getApplicationContext());
//        frameAR.addView(arContent);

        // Load Raw file and covert to String
        data = useAPI.covRawToString(getResources().openRawResource(R.raw.data));

        // Parser json data
        dbTouris = useAPI.parserJsonFromTouris(data);

        /*************************  For Debug Beg ************************* */
//        dbAR.setData("新竹關東橋郵局", ((float) 24.782646), ((float) 121.018707), 0, 0, 0, false, 0);
//        dbAR.setData("竹北火車站", ((float) 24.839656), ((float) 121.009613), 0, 0, 0, false, 0);
//        dbAR.setData("十八尖山停車場", ((float) 24.795013), ((float) 120.986764), 0, 0, 0, false, 9);
        /*************************  For Debug End ************************* */

        // insert and init to ListView
//        LongitudeArr = new String[dbTouris.size()];
//        LatitudeArr = new String[dbTouris.size()];
//        namesArr = new String[dbTouris.size()];
//        xCoordinate = new double[dbTouris.size()];
//        yCoordinate = new double[dbTouris.size()];
//        arOri = new int[dbTouris.size()];
//
//        for (int i = 0; i < dbTouris.size(); i++) {
//            LongitudeArr[i] = dbTouris.get(i).getLongitude();
//            LatitudeArr[i] = dbTouris.get(i).getLatitude();
//            namesArr[i] = dbTouris.get(i).getName();
//            Log.d("Coordinate", "Longitude: " + LongitudeArr[i].toString() + ", Latitude: " + LatitudeArr[i].toString());
//        }

        for (int i = 0; i < dbTouris.size(); i++) {
            dbAR.setData(dbTouris.get(i).getName(), Float.parseFloat(dbTouris.get(i).getLatitude()), Float.parseFloat(dbTouris.get(i).getLongitude()), 0, 0, 0, false, 0);
            Log.d("dbAR.getLatitude", String.valueOf(dbAR.getLatitude(i)));
        }

        btnScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //screenShot(getWindow().getDecorView().getRootView());
                captureScreen(REQUEST_SCREEN_SHOT);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraText.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case REQUEST_LOCATION:
                    getMyLocation();
                    isLocatOK = true;
                    break;
                case REQUEST_SCREEN_SHOT:
                    captureScreen(REQUEST_SCREEN_SHOT);
                    break;
            }

        } else {
            //user do reject
        }
    }

    public void getMyLocation() {
        Location bestLocation = null;
        locationMgr = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        List<String> providers = locationMgr.getProviders(true);
        for (String provider : providers) {
            Location l = locationMgr.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        myLocat = bestLocation;

//        Log.d("GPS", String.valueOf(myLocat.getLatitude()) + ", " + String.valueOf(myLocat.getLongitude()));
    }

    private void initView() {
        cameraText = (TextureView) findViewById(R.id.textureView);
        frameAR = (FrameLayout) findViewById(R.id.framelayout);
        btnScreen = (ImageButton) findViewById(R.id.imgBtn);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    /* invoke onSurfaceTextureAvailable when TextureView is activity */
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
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

            OverlayView arContent = new OverlayView(getApplicationContext());
            frameAR.addView(arContent);
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
            } catch (CameraAccessException e) {
                e.printStackTrace();
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
        } catch (CameraAccessException e) {
            e.printStackTrace();
            //Error
        }

        capBuilder.addTarget(surface);

        try {
            camDevice.createCaptureSession(Arrays.asList(surface), mCameraCaptureSessionCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            //Error
        }
    }

    private void closeAllCameraSession() {
        if (cameraSeeion != null) {
            cameraSeeion.close();
            cameraSeeion = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (int i = 0; i < dbAR.getSize(); i++) {
                    if (dbAR.getIsShown(i) == true) {
                        if (useAPI.isTouchInContain(AR_OBJECT_WIDTH, x, y, dbAR.getXCoord(i), dbAR.getYCoord(i))) {
                            Intent intCont = new Intent();
                            intCont.setClass(CamActivity.this, ContentActivity.class);
                            intCont.putExtra("name", dbAR.getName(i));
                            startActivity(intCont);
                        }
                    }
                }

                return true;
        }

        return super.onTouchEvent(event);
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
            dispCount = 0;

            dbgCreateArObj(canvas, ((float) (screenWidth * 0.35)), ((float) (screenHeight * 0.85)), "debug mode");

            for (int i = 0; i < dbAR.getSize(); i++) {
                if (dbAR.getXCoord(i) == 0 || dbAR.getYCoord(i) == 0) {
                    continue;
                }

                if (dbAR.getDistance(i) <= MAXIMUM_DISTANCE) {
                    createNewObj(canvas, dbAR.getXCoord(i) + (dispCount * 200), dbAR.getYCoord(i), i);
                    dispCount++;
                }

                if (dispCount > MAXIMUM_NUM_DISPLAY_AR) {
                    break;
                }
            }

            if (false) {
                switch (myOri) {
                    case 0:
                    case 1:
                        if (DEBUG_MESSAGE == true)
                            dbgCreateArObj(canvas, ((float) (screenWidth * 0.35)), ((float) (screenHeight * 0.85)), "北");
                        /*************************  For Debug Beg **************************/
                        for (int i = 0; i < dbAR.getSize(); i++) {
//                        if ((dbAR.getQuadrant(i) == 1) || (dbAR.getQuadrant(i) == 7)) {
//                            createNewObj(canvas, ((float) sampleXCoord[dispCount]), ((float) sampleYCoord[dispCount]), i);
//                            dbAR.setXYcoord(i, ((float) sampleXCoord[dispCount]), ((float) sampleYCoord[dispCount]));
//                            dbAR.setIsShown(i, true);
//                            dispCount++;
//                        } else {
//                            dbAR.setIsShown(i, false);
//                        }
//
//                        if (dispCount > MAXIMUM_NUM_DISPLAY_AR) {
//                            break;
//                        }
                            if (dbAR.getDistance(i) <= 5000 && dbAR.getXCoord(i) != 0) {
                                createNewObj(canvas, dbAR.getXCoord(i), dbAR.getYCoord(i), i);
                                dispCount++;
                            }

                            if (dispCount > MAXIMUM_NUM_DISPLAY_AR) {
                                break;
                            }
                        }
                        /*************************  For Debug End **************************/
                        break;
                    case 2:
                    case 3:
                        if (DEBUG_MESSAGE == true)
                            dbgCreateArObj(canvas, ((float) (screenWidth * 0.35)), ((float) (screenHeight * 0.85)), "東");
                        break;
                    case 4:
                    case 5:
                        if (DEBUG_MESSAGE == true)
                            dbgCreateArObj(canvas, ((float) (screenWidth * 0.35)), ((float) (screenHeight * 0.85)), "南");
//                    for (int i = 0; i < 10; i++) {
//                        if (arOri[i] == 3) {
//                            createNewObj(canvas, sampleXCoord[dispCount], sampleYCoord[dispCount], i);
//                            dispCount++;
//                        }
//
//                        if (dispCount > 1) {
//                            break;
//                        }
//                    }
                        /*************************  For Debug Beg **************************/
//                    for (int i = 0; i < dbAR.getSize(); i++) {
//                        if ((dbAR.getQuadrant(i) == 5) || (dbAR.getQuadrant(i) == 3)) {
//                            createNewObj(canvas, ((float) sampleXCoord[dispCount]), ((float) sampleYCoord[dispCount]), i);
//                            dbAR.setXYcoord(i, ((float) sampleXCoord[dispCount]), ((float) sampleYCoord[dispCount]));
//                            dbAR.setIsShown(i, true);
//                            dispCount++;
//                        } else {
//                            dbAR.setIsShown(i, false);
//                        }
//
//                        if (dispCount > MAXIMUM_NUM_DISPLAY_AR) {
//                            break;
//                        }
//                    }
                        /*************************  For Debug End **************************/
                        break;
                    case 6:
                    case 7:
                        if (DEBUG_MESSAGE == true)
                            dbgCreateArObj(canvas, ((float) (screenWidth * 0.35)), ((float) (screenHeight * 0.85)), "西");
                        /*************************  For Debug Beg **************************/
//                    for (int i = 0; i < dbAR.getSize(); i++) {
//                        if ((dbAR.getQuadrant(i) == 5) || (dbAR.getQuadrant(i) == 7)) {
//                            createNewObj(canvas, ((float) sampleXCoord[dispCount]), ((float) sampleYCoord[dispCount]), i);
//                            dbAR.setXYcoord(i, ((float) sampleXCoord[dispCount]), ((float) sampleYCoord[dispCount]));
//                            dbAR.setIsShown(i, true);
//                            dispCount++;
//                        } else {
//                            dbAR.setIsShown(i, false);
//                        }
//
//                        if (dispCount > MAXIMUM_NUM_DISPLAY_AR) {
//                            break;
//                        }
//                    }
                        /*************************  For Debug End **************************/
                        break;
                    default:
                        break;
                }
            }

            if (dispCount == 0) {
                showArNotFound(canvas, ((float) (screenWidth * 0.35)), (((float) (screenHeight * 0.6))), arNotFound);
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            double azimuth;
            float dist[] = new float[1];

            if (isLocatOK == true) {
                if (myLocat != null) {
                    dbAR.updateDB(myLocat);
                }
            }

//            StringBuilder msg = new StringBuilder(event.sensor.getName()).append(" ");
//            for (float value : event.values) {
//                msg.append("[").append(value).append("]");
//            }

            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
//                    accelData = msg.toString();
                    accelerometerValues = event.values;
                    break;
//                case Sensor.TYPE_GYROSCOPE:
//                    gyroData = msg.toString();
//                    Log.d("TYPE_GYROSCOPE", gyroData);
//                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
//                    compassData = msg.toString();
                    magneticFieldValues = event.values;
                    break;
            }

            /* get myself orientation */
            calculateOrientation();

            /* well be call OnDraw() always */
            this.invalidate();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

    }

    public void createNewObj(Canvas canvas, float x, float y, int index) {
        Bitmap b = Bitmap.createScaledBitmap(arLocatMark, arLocatMark.getWidth() / 2, arLocatMark.getHeight() / 2, false);
        Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        contentPaint.setTextSize(50);
        canvas.drawBitmap(b, x, y, contentPaint);
//        canvas.drawText(namesArr[index], x.floatValue() + 110, y.floatValue() + 150, contentPaint);
        canvas.drawText(dbAR.getName(index), x + 110, y + 150, contentPaint);
        canvas.drawText("距離為 : " + dbAR.getDistance(index) + "公尺", x + 110, y + 200, contentPaint);
    }

    public void dbgCreateArObj(Canvas canvas, float x, float y, String str) {
        Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        contentPaint.setColor(Color.RED);
        contentPaint.setTextSize(50);

        w = ((int) x) + 400;
        h = ((int) y) + 100;
        areaRect = new Rect(((int) x), ((int) y), w, h);
        canvas.drawRect(areaRect, contentPaint);

//        RectF rectf = new RectF(areaRect);
//        rectf.left += (areaRect.width()) / 10.0f;
//        rectf.top += (areaRect.height()) / 4.0f;

        contentPaint.setColor(Color.WHITE);
        canvas.drawText(str, x, y + 60, contentPaint);

        if (isLocatOK == true) {
            if (myLocat != null) {
                canvas.drawText("My location : " + myLocat.getLatitude() + ", " + myLocat.getLongitude(), 0, y + 150, contentPaint);
            }
        }

        canvas.drawText("window : " + dbAR.getWindowWidth() + ", " + dbAR.getWindowHeight(), 0, y + 200, contentPaint);

    }

    public void showArNotFound(Canvas canvas, float x, float y, Bitmap bitmap) {
        Bitmap b = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 5, bitmap.getHeight() / 5, false);
        Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        contentPaint.setTextAlign(Paint.Align.CENTER);
        contentPaint.setTextSize(50);
        canvas.drawBitmap(b, x, y, contentPaint);
        canvas.drawText("附近無景點", x + 85, y + 215, contentPaint);
    }

    private double getAzimuthFromGPS(double lat_a, double lng_a, double lat_b, double lng_b) {
        double d, b;
        lat_a = lat_a * Math.PI / 180;
        lng_a = lng_a * Math.PI / 180;
        lat_b = lat_b * Math.PI / 180;
        lng_b = lng_b * Math.PI / 180;

        d = Math.sin(lat_a) * Math.sin(lat_b) + Math.cos(lat_a) * Math.cos(lat_b) * Math.cos(lng_b - lng_a);
        d = Math.sqrt(1 - d * d);
        d = Math.cos(lat_b) * Math.sin(lng_b - lng_a) / d;
        b = Math.cos(lat_a) * Math.sin(lng_b - lng_a) / d;
        b = Math.asin(b) * 180 / Math.PI + 180;
        d = Math.asin(d) * 180 / Math.PI;

//        if (d < 0) {
//            d += 360;
//        }
        Log.d("Azimuth", "Azimuth = " + String.valueOf(b));

// d = Math.round(d*10000);
        return b;
    }

    private void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);

        // 要经过一次数据格式的转换，转换为度
        values[0] = (float) Math.toDegrees(values[0]);
//        Log.i("Orientation", values[0] + "");
        //values[1] = (float) Math.toDegrees(values[1]);
        //values[2] = (float) Math.toDegrees(values[2]);

        if (values[0] >= -5 && values[0] < 5) {
            myOri = 0;
//            Log.i("Orientation", "正北");
        } else if (values[0] >= 5 && values[0] < 85) {
            myOri = 1;
//            Log.i("Orientation", "东北");
        } else if (values[0] >= 85 && values[0] <= 95) {
            myOri = 2;
//            Log.i("Orientation", "正东");
        } else if (values[0] >= 95 && values[0] < 175) {
            myOri = 3;
//            Log.i("Orientation", "东南");
        } else if ((values[0] >= 175 && values[0] <= 180) || (values[0]) >= -180 && values[0] < -175) {
            myOri = 4;
//            Log.i("Orientation", "正南");
        } else if (values[0] >= -175 && values[0] < -95) {
            myOri = 5;
//            Log.i("Orientation", "西南");
        } else if (values[0] >= -95 && values[0] < -85) {
            myOri = 6;
//            Log.i("Orientation", "正西");
        } else if (values[0] >= -85 && values[0] < -5) {
            myOri = 7;
//            Log.i("Orientation", "西北");
        }
    }

    private Bitmap screenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private static Bitmap getScreenShot(View view) {
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private void captureScreen(int requestCode) {
        boolean hasObj = false;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
        } else {
            Bitmap bitmap = cameraText.getBitmap();
            Canvas canvas = new Canvas(bitmap);

            for (int i = 0; i < dbAR.getSize(); i++) {
                if (dbAR.getIsShown(i) == true) {
                    createNewObj(canvas, dbAR.getXCoord(i), dbAR.getYCoord(i), i);
                    hasObj = true;
                }
            }

            if (hasObj == false) {
                showArNotFound(canvas, ((float) (screenWidth * 0.35)), (((float) (screenHeight * 0.6))), arNotFound);
            }

            Date now = new Date();
            android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
            CaptureScreen.savePic(bitmap, "sdcard/" + now + ".jpg");
        }
    }
}
