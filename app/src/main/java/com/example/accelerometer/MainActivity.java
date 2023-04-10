package com.example.accelerometer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.util.Log;


import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView accelerationTextView;
    private AccelerationDataDbHelper dbHelper;
    private GaugeView GaugeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize sensor manager and accelerometer sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Get a reference to the TextView that will show the acceleration values
        GaugeView = findViewById(R.id.GaugeView);

        // Create an instance of the database helper
        dbHelper = new AccelerationDataDbHelper(this);


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

            GaugeView.setAccelerationValue(acceleration);

            /* Update the TextView with the new acceleration value
            TextView accelerationTextView = findViewById(R.id.accelerationTextView);
            accelerationTextView.setText("Acceleration: " + acceleration + " m/s²");
            */



            long timestamp = System.currentTimeMillis();

            // Add acceleration data to the database
            dbHelper.addAccelerationData(acceleration, timestamp);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not required in this example
    }
}