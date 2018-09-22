package com.apeks.atom.justamap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView mapView;
    private Bitmap mapImage, blueDot, positionBitmap;
    Canvas canvas;
    private static float X0, Y0 ,X1, Y1, X2, Y2, left, top;
    private int startX, startY, endX1, endY1, endX2, endY2;
    private static int sensorCount = 0;
    EditText x ,z;
    Button go;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    FloatingActionButton fab;

    final int mapEndX = 300;
    final int mapEndY = 600;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Enable/Disable Magnetometer", Snackbar.LENGTH_LONG)
                        .setAction("Action", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(getApplicationContext(),"Now toggling magnetometer",Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        });
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        /*
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        */
        x = findViewById(R.id.textView);
        z = findViewById(R.id.textView2);
        go = findViewById(R.id.button);

        mapView = findViewById(R.id.mapImageView);
        mapImage = BitmapFactory.decodeResource(getResources(), R.drawable.newmap);
        X0 = mapImage.getWidth() * 0.120f;
        Y0 = mapImage.getHeight() * 0.910f;

        X1 = mapImage.getWidth() * 0.85f;
        Y1 = mapImage.getHeight() * 0.890f;

        X2 = mapImage.getWidth() * 0.120f;
        Y2 = mapImage.getHeight() * 0.030f;

        startX = Math.round(X0);
        startY = Math.round(Y0);

        endX1 = Math.round(X1);
        endY1 = Math.round(Y1);

        endX2 = Math.round(X2);
        endY2 = Math.round(Y2);

        blueDot = BitmapFactory.decodeResource(getResources(), R.drawable.direction_marker);
        blueDot = Bitmap.createScaledBitmap(blueDot, 125,125, false);
        positionBitmap = Bitmap.createBitmap(mapImage.getWidth(),mapImage.getHeight(), mapImage.getConfig());
        canvas = new Canvas(positionBitmap);
        canvas.drawBitmap(mapImage, new Matrix(), null);
        float deltaX = Math.abs(50 * (endX1 - startX) / mapEndX);
        float deltaY = Math.abs(400 * (startY - endY2) / mapEndY);
        //float temp =  300 * 2685 / 600;

        canvas.drawBitmap(blueDot, startX + deltaX, startY - deltaY, new Paint()); // or try (endY2 + deltaY)
        //canvas.drawBitmap(blueDot, endX, endY, new Paint());

        mapView.setImageBitmap(positionBitmap);

        Log.e("kala:","\nstartX="+startX+" startY="+startY);
        Log.e("kala:","endX_="+endX2+" endY_="+endY2);
        Log.e("kala:","Point diff = "+Integer.toString(endY2 - startY));

        go.setOnClickListener(view -> {
            float x_c = Float.valueOf(x.getText().toString());
            float y_c = Float.valueOf(z.getText().toString());
            drawOnMap(x_c, y_c);
        });
    }
    public static Bitmap rotate(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();

            m.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(
                        b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                //throw ex;
            }
        }
        return b;
    }
    public void drawOnMap(float x, float y) {
        float deltaX = Math.abs(x * (endX1 - startX) / mapEndX);
        float deltaY = Math.abs(y * (startY - endY2) / mapEndY);
        left = startX + deltaX;
        top = startY - deltaY;  // or try (endY2 + deltaY)

        canvas.drawBitmap(mapImage, new Matrix(), null);
        canvas.drawBitmap(blueDot, left, top, new Paint());

    }

    protected void rotateMarker(int mCurrentDegree) {
        canvas.drawBitmap(mapImage, new Matrix(), null);
        canvas.drawBitmap(rotate(blueDot, mCurrentDegree), left, top, new Paint());
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            sensorCount++;
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            float azimuthInDegress = (float)(Math.toDegrees(azimuthInRadians)+360)%360;
            RotateAnimation ra = new RotateAnimation(
                    mCurrentDegree,
                    -azimuthInDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            ra.setDuration(250);

            ra.setFillAfter(true);

            //mPointer.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
            if (sensorCount > 300) {
                runOnUiThread(() -> {
                    //go.setText((int)mCurrentDegree);
                    //rotateMarker((int) (mCurrentDegree));
                });
                sensorCount = 0;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }
}
