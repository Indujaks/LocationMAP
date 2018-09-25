package com.apeks.atom.justamap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

public class Settings extends AppCompatActivity {
    SeekBar seekBarX, seekBarY;
    int progressX = 0, progressY = 0;

    private ImageView mapView;
    private Bitmap mapImage, blueDot, positionBitmap;
    Canvas canvas;
    private static float X0, Y0 ,X1, Y1, X2, Y2, globalLeft, globalTop;
    private int startX, startY, endX1, endY1, endX2, endY2, mapWidth, mapHeight;
    public float k0_x = 0.120f, k0_y = 0.910f, k1_x = 0.85f, k2_y = 0.030f;

    DisplayMetrics displayMetrics = new DisplayMetrics();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        mapView = findViewById(R.id.imageView);
        mapImage = BitmapFactory.decodeResource(getResources(), R.drawable.newmap);
        blueDot = BitmapFactory.decodeResource(getResources(), R.drawable.direction_marker);
        blueDot = Bitmap.createScaledBitmap(blueDot, 125,125, false);
        positionBitmap = Bitmap.createBitmap(mapImage.getWidth(),mapImage.getHeight(), mapImage.getConfig());

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

        mapHeight = /*mapImage.getHeight();*/ displayHeight * 2; // displayHeight / 2;
        mapWidth = /*mapImage.getWidth();*/ displayWidth * 2; // displayWidth / 2;
        Log.e("MAP:","Height = "+mapHeight+" Width = "+mapWidth);

        canvas = new Canvas(positionBitmap);
        canvas.drawBitmap(mapImage, new Matrix(), null);
        canvas.drawBitmap(blueDot, mapWidth, mapHeight, new Paint());
        mapView.setImageBitmap(positionBitmap);

        seekBarX = findViewById(R.id.seekBarX);
        seekBarY = findViewById(R.id.seekBarY);

        seekBarX.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                progressX = progress;
                Toast toast = Toast.makeText(getApplicationContext(),"seekbar progress: "+progress, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Toast toast = Toast.makeText(getApplicationContext(),"You are changing the X co-ordinate", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP|Gravity.LEFT, 0, 0);
                toast.show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float left = mapWidth * progressX / 100;
                //float top = mapHeight;
                globalLeft = left;
                //globalTop = top;
                Log.e("MAP:","Y bar: left="+globalLeft+" Top="+globalTop);
                canvas.drawBitmap(mapImage, new Matrix(), null);
                canvas.drawBitmap(blueDot, globalLeft, globalTop, new Paint());
                mapView.setImageBitmap(positionBitmap);
                //Toast.makeText(getApplicationContext(),"Finished setting X co-ordinate", Toast.LENGTH_SHORT).show();
            }
        });

        seekBarY.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                progressY = progress;
                Toast toast = Toast.makeText(getApplicationContext(),"seekbar progress: "+progress, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Toast toast = Toast.makeText(getApplicationContext(),"You are changing the X co-ordinate", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM, 0, 0);
                toast.show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //float left = mapWidth / 2;
                float top = mapHeight * progressY / 100;
                //globalLeft = left;
                globalTop = top;
                Log.e("MAP:","Y bar: left="+globalLeft+" Top="+globalTop);
                canvas.drawBitmap(mapImage, new Matrix(), null);
                canvas.drawBitmap(blueDot, globalLeft, globalTop, new Paint());
                mapView.setImageBitmap(positionBitmap);
                //Toast.makeText(getApplicationContext(),"Finished setting Y co-ordinate", Toast.LENGTH_SHORT).show();
            }
        });



    }

}
