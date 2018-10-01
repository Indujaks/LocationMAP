package com.apeks.atom.justamap;

import android.content.Intent;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;

public class MainActivity extends AppCompatActivity implements SensorEventListener ,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private ImageView mapView;
    private Bitmap mapImage, blueDot, positionBitmap;
    Canvas canvas;
    private static float X0, Y0 ,X1, Y1, X2, Y2, left, top;
    private int startX, startY, endX1, endY1, endX2, endY2;
    public float k0_x = 0.105f, k0_y = 0.86f, k1_x = 0.90f, k2_y = 0.070f;
    private static int sensorCount = 0;
    EditText x ,z,coortv;
    TextView coordinates_tv;
    Button go;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    FloatingActionButton fab;
    private  DatabaseReference rootRef;
    private static final String KEY_ROOT_DIR = "shared_anchor_codelab_root_helloAR_app";

    final int mapEndX = 450;
    final int mapEndY = 850;
    private GoogleApiClient mGoogleApiClient;
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
    private StorageManager storageManager;
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

        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        Log.e("KALA:","Display height="+height);
        Log.e("KALA:","Display width="+width);
        x = findViewById(R.id.textView);
        z = findViewById(R.id.textView2);
        go = findViewById(R.id.button);
        coordinates_tv =findViewById(R.id.textView3);
        mapView = findViewById(R.id.mapImageView);
        mapImage = BitmapFactory.decodeResource(getResources(), R.drawable.map);

        X0 = mapImage.getWidth() * k0_x;
        Y0 = mapImage.getHeight() * k0_y;

        X1 = mapImage.getWidth() * k1_x;
        Y1 = mapImage.getHeight() * k0_y;

        X2 = mapImage.getWidth() * k0_x;
        Y2 = mapImage.getHeight() * k2_y;

        startX = Math.round(X0);
        startY = Math.round(Y0);

        endX1 = Math.round(X1);
        endY1 = Math.round(Y1);

        endX2 = Math.round(X2);
        endY2 = Math.round(Y2);

        blueDot = BitmapFactory.decodeResource(getResources(), R.drawable.direction_marker);
        blueDot = Bitmap.createScaledBitmap(blueDot, 90,90, false);
        positionBitmap = Bitmap.createBitmap(mapImage.getWidth(),mapImage.getHeight(), mapImage.getConfig());
        canvas = new Canvas(positionBitmap);
        canvas.drawBitmap(mapImage, new Matrix(), null);
        float deltaX = Math.abs(150 * (endX1 - startX) / mapEndX);
        float deltaY = Math.abs(400 * (startY - endY2) / mapEndY);

       //canvas.drawBitmap(blueDot, endX1 , endY1, new Paint()); // or try (endY2 + deltaY)
      //  canvas.drawBitmap(blueDot, endX1, endY1, new Paint());
        canvas.drawBitmap(blueDot, endX2, endY2, new Paint());


        mapView.setImageBitmap(positionBitmap);

        Log.e("kala:","\nstartX="+startX+" startY="+startY);
        Log.e("kala:","endX_="+endX2+" endY_="+endY2);
        Log.e("kala:","Point diff = "+Integer.toString(endY2 - startY));

        go.setOnClickListener(view -> {
            String x_c = x.getText().toString();
            String y_c = z.getText().toString();

            if (x_c.isEmpty() || y_c.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Entering Settings Layout", Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity.this, Settings.class));
            } else
                drawOnMap(Float.valueOf(x_c), Float.valueOf(y_c));
        });
        //buildGoogleApiClient();
       // storageManager = new StorageManager(this);
        buildfirebase();
    }
    /**
     * Builds {@link GoogleApiClient}, enabling automatic lifecycle management using
     * {@link GoogleApiClient.Builder#enableAutoManage(FragmentActivity,
     * int, GoogleApiClient.OnConnectionFailedListener)}. I.e., GoogleApiClient connects in
     * {@link AppCompatActivity#onStart}, or if onStart() has already happened, it connects
     * immediately, and disconnects automatically in {@link AppCompatActivity#onStop}.
     */
    private void buildfirebase(){
       // FileInputStream serviceAccount = new FileInputStream("");

// Initialize the app with a service account, granting admin privileges
     /*   FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId("com.apeks.atom.justamap")
                .setDatabaseUrl("https://<databaseName>.firebaseio.com")
                .build();
        FirebaseApp.initializeApp(options);*/

        FirebaseApp firebaseApp = FirebaseApp.initializeApp(getApplicationContext());
        rootRef = FirebaseDatabase.getInstance(firebaseApp).getReference().child(KEY_ROOT_DIR);
        rootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String[] data=dataSnapshot.getValue(String.class).split(":");
                Log.e("indu","data"+data[0]);
                coordinates_tv.setText("X :"+Math.round(Float.valueOf(data[0]))+"\nY :"+Math.round(Float.valueOf(data[1])));
                drawOnMap(Float.valueOf(data[0]),Float.valueOf(data[1]));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }
    private void buildGoogleApiClient() {
        if (mGoogleApiClient != null) {
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .build();
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
        mapView.setImageBitmap(positionBitmap);
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
