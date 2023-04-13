package com.example.accelerometer;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.util.Log;


import androidx.appcompat.app.AppCompatActivity;


public class AccelerationController extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    //private TextView accelerationTextView;
    private AccelerationDataDbHelper dbHelper;
    private AccelerationView AccelerationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
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

            // Ignore acceleration values close to zero
            float accelerationThreshold = 0.1f; // Set your acceleration threshold here
            if (Math.abs(acceleration) > accelerationThreshold) {
                AccelerationView.setAccelerationValue(acceleration);
                Log.d("Sensor readings too low!", "Acceleration: " + acceleration);

            /* Update the TextView with the new acceleration value
            TextView accelerationTextView = findViewById(R.id.accelerationTextView);
            accelerationTextView.setText("Acceleration: " + acceleration + " m/s²");
            */

                long timestamp = System.currentTimeMillis();

                // Add acceleration data to the database
                dbHelper.addAccelerationData(acceleration, x, y, z, timestamp);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not required in this example
    }
}