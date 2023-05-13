package com.example.accelerometer;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;


public class AccelerationController extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private MediaPlayer mediaPlayer;
    private AccelerationDataDbHelper dbHelper;
    private AccelerationView AccelerationView;
    private boolean isSoundEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize MediaPlayer with the warning sound file
        mediaPlayer = MediaPlayer.create(this, R.raw.warning_sound);
        setContentView(R.layout.activity_main);

        // set screen orientation to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Initialize sensor manager and accelerometer sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        // Get a reference to the TextView that will show the acceleration values
        AccelerationView = findViewById(R.id.GaugeView);

        // Create an instance of the database helper
        dbHelper = new AccelerationDataDbHelper(this);

        final Button button = findViewById(R.id.statistics_page_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                // Add code to start the StatisticsPageView activity
                Intent intent = new Intent(AccelerationController.this, StatisticsPageView.class);
                startActivity(intent);
            }
        });

        ToggleButton soundToggleButton = findViewById(R.id.soundToggleButton);
        soundToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSoundEnabled = isChecked;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        // I tried playing with those - GAME, FASTEST, UI
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            Log.d("Sensor Data", "X: " + event.values[0] + " Y: " + event.values[1] + " Z: " + event.values[2]);

            // Calculate acceleration
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float acceleration = (float) Math.sqrt(x * x + y * y + z * z) - 9.81f;

            // Check if acceleration is outside the desired range
            if (acceleration > 2.98 || acceleration < -2.98) {
                playWarningSound();
            }

            AccelerationView.setAccelerationValue(acceleration);

            // Load the font from the app/res/font/ directory

// Update the TextView with the new acceleration value
            TextView accelerationTextView = findViewById(R.id.accelerationTextView);
            accelerationTextView.setText(String.format("    %.1f \n   m/s²", acceleration));
            long timestamp = System.currentTimeMillis();

            // Add acceleration data to the database
            dbHelper.addAccelerationData(acceleration, x, y, z, timestamp);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not required in this example
    }


    private void playWarningSound() {
        if (isSoundEnabled && mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }
}