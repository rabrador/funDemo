package com.justdemo.vincent.functiondemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
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
    private final int REQUEST_SCREEN_SHOT = 3;
    private int isLocatOK = 0;
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
    private double sampleXCoord[] = {600, 500, 700};
    private double sampleYCoord[] = {1000, 1200, 1400};
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_cam);

        initView();
        arNotFound = BitmapFactory.decodeResource(getResources(), R.drawable.locat_not_found);
        arLocatMark = BitmapFactory.decodeResource(getResources(), R.drawable.ar_locat_mark);
        cameraText.setSurfaceTextureListener(mSurfaceTextureListener);

        OverlayView arContent = new OverlayView(getApplicationContext());
        frameAR.addView(arContent);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .build();

        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.CAMERA}, REQUEST_LOCATION);
        } else {
            getMyLocation();
            isLocatOK = 1;
        }

        // Load Raw file and covert to String
        covRawToString(loadRawFile());

        // Parser json data
        parserJson(data);

        // insert and init to ListView
        LongitudeArr = new String[dbTouris.size()];
        LatitudeArr = new String[dbTouris.size()];
        namesArr = new String[dbTouris.size()];
        xCoordinate = new double[dbTouris.size()];
        yCoordinate = new double[dbTouris.size()];
        arOri = new int[dbTouris.size()];

        for (int i = 0; i < dbTouris.size(); i++) {
            LongitudeArr[i] = dbTouris.get(i).getLongitude();
            LatitudeArr[i] = dbTouris.get(i).getLatitude();
            namesArr[i] = dbTouris.get(i).getName();
            Log.d("Coordinate", "Longitude: " + LongitudeArr[i].toString() + ", Latitude: " + LatitudeArr[i].toString());
        }

        btnScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //screenShot(getWindow().getDecorView().getRootView());
                captureScreen(REQUEST_SCREEN_SHOT);
            }
        });

        /* Get Screen height and width */
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (requestCode == REQUEST_LOCATION) {
                getMyLocation();
                isLocatOK = 1;
            } else if (requestCode == REQUEST_SCREEN_SHOT) {
                captureScreen(REQUEST_SCREEN_SHOT);
            }
            openCamera();
        } else {
            //user do reject
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void getMyLocation() {
        locationMgr = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        String provider = this.locationMgr.getBestProvider(new Criteria(), true);
        myLocat = locationMgr.getLastKnownLocation(provider);
        //Toast.makeText(CamActivity.this, String.valueOf(location.getLatitude()) + ", "+ String.valueOf(location.getLongitude()), Toast.LENGTH_LONG).show();
        Log.d("GPS", String.valueOf(myLocat.getLatitude()) + ", " + String.valueOf(myLocat.getLongitude()));
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
            int dispCount = 0;

            switch (myOri) {
                case 0:
                case 1:
                    dbgCreateArObj(canvas, ((float) (screenWidth * 0.35)), ((float) (screenHeight * 0.85)), "正北");
                    break;
                case 2:
                case 3:
                    dbgCreateArObj(canvas, ((float) (screenWidth * 0.35)), ((float) (screenHeight * 0.85)), "正東");
                    break;
                case 4:
                case 5:
                    dbgCreateArObj(canvas, ((float) (screenWidth * 0.35)), ((float) (screenHeight * 0.85)), "正南");
                    for (int i = 0; i < 10; i++) {
                        if (arOri[i] == 3) {
                            createNewObj(canvas, sampleXCoord[dispCount], sampleYCoord[dispCount], i);
                            dispCount++;
                        }

                        if (dispCount > 1) {
                            break;
                        }
                    }
                    break;
                case 6:
                case 7:
                    dbgCreateArObj(canvas, ((float) (screenWidth * 0.35)), ((float) (screenHeight * 0.85)), "正西");
                    break;
                default:
                    break;
            }

            if (dispCount == 0) {
                showArNotFound(canvas, ((float) (screenWidth * 0.35)), (((float) (screenHeight * 0.6))), arNotFound);
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            double azimuth;

            if (isLocatOK == 1) {
                for (int i = 0; i < 10; i++) {
                    azimuth = getAzimuthFromGPS(Double.parseDouble(LatitudeArr[i].toString()), Double.parseDouble(LongitudeArr[i].toString()), myLocat.getLatitude(), myLocat.getLongitude());

                    if (azimuth > 140 && azimuth < 200) {
                        arOri[i] = 3;
//                    xCoordinate[i] = azimuth + 600;
//                    yCoordinate[i] = azimuth + 1000;
                    } else if (azimuth > 180) {
                        arOri[i] = 5;
                    }

//                azimuth = getAzimuthFromGPS(Double.parseDouble(LatitudeArr[i].toString()), Double.parseDouble(LongitudeArr[i].toString()),
//                        Double.parseDouble(LatitudeArr[i + 10].toString()), Double.parseDouble(LongitudeArr[i + 10].toString()));
//
//                /* dummy x-coordinate and y-coordinate */
//                if (azimuth < 90) {
//                    xCoordinate[i] = azimuth + 600;
//                    yCoordinate[i] = azimuth + 300;
//                } else if (azimuth < 180) {
//                    xCoordinate[i] = azimuth + 600;
//                    yCoordinate[i] = azimuth + 1000;
//                } else if (azimuth < 270) {
//                    xCoordinate[i] = azimuth + 50;
//                    yCoordinate[i] = azimuth + 1000;
//                } else {
//                    xCoordinate[i] = azimuth + 50;
//                    yCoordinate[i] = azimuth + 300;
//                }
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

            calculateOrientation();
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
        Bitmap b = Bitmap.createScaledBitmap(arLocatMark, arLocatMark.getWidth() / 2, arLocatMark.getHeight() / 2, false);
        Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        contentPaint.setColor(Color.GREEN);
        contentPaint.setTextSize(50);

//        w = x.intValue() + 400;
//        h = y.intValue() + 100;
//        areaRect = new Rect(x.intValue(), y.intValue(), w, h);
//        canvas.drawRect(areaRect, contentPaint);

//        RectF rectf = new RectF(areaRect);
//        rectf.left += (areaRect.width()) / 10.0f;
//        rectf.top += (areaRect.height()) / 4.0f;

//        contentPaint.setColor(Color.WHITE);
        canvas.drawBitmap(b, x.floatValue(), y.floatValue(), contentPaint);
        canvas.drawText(namesArr[index], x.floatValue() + 110, y.floatValue() + 150, contentPaint);
    }

    public void dbgCreateArObj(Canvas canvas, float x, float y, String str) {
        Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        contentPaint.setColor(Color.RED);
        contentPaint.setTextSize(50);

        w = ((int) x) + 400;
        h = ((int) y) + 100;
        areaRect = new Rect(((int) x), ((int) y), w, h);
        canvas.drawRect(areaRect, contentPaint);

        RectF rectf = new RectF(areaRect);
        rectf.left += (areaRect.width()) / 10.0f;
        rectf.top += (areaRect.height()) / 4.0f;

        contentPaint.setColor(Color.WHITE);
        canvas.drawText(str, rectf.left, rectf.top - contentPaint.ascent(), contentPaint);
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
//        Log.d("Azimuth", "Azimuth = " + String.valueOf(b));
// d = Math.round(d*10000);
        return b;
    }

    public void calculateOrientation() {
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

    public Bitmap screenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public static Bitmap getScreenShot(View view) {
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private void captureScreen(int requestCode) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
        } else {
//            CaptureScreen.shoot(getWindow().getDecorView().findViewById(android.R.id.content));
//            CaptureScreen.savePic(getScreenShot(getWindow().getDecorView().findViewById(android.R.id.content)), "sdcard/yyy.png");

            Bitmap bitmap = cameraText.getBitmap();
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
//            dbgCreateArObj(canvas, ((double) 0), ((double) 0), "TESTTTTTTTT");
            showArNotFound(canvas, 300, 1000, arNotFound);

            CaptureScreen.savePic(bitmap, "sdcard/yyy.png");
//            Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

////            Canvas canvas = new Canvas(bitmap);
//            screenShotCanvas.drawBitmap(bitmap, 100, 1000, contentPaint);
////            getWindow().getDecorView().getRootView().draw(canvas);
//            CaptureScreen.savePic(bitmap,"sdcard/ttt.png");

//            Canvas canvas = new Canvas(screenShot(cameraText));
//            Drawable bgDrawable = cameraText.getBackground();
//            if (bgDrawable!=null)
//                bgDrawable.draw(canvas);
//            else
//                canvas.drawColor(Color.WHITE);
//
//            cameraText.draw(canvas);
//            CaptureScreen.savePic(screenShot(cameraText),"sdcard/ttt.png");

        }
    }
}
