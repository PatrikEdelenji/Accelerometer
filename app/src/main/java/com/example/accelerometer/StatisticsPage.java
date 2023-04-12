package com.example.accelerometer;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class StatisticsPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.statistics_page); // Set the layout for this activity


        // Assuming you have an instance of your database helper class named 'dbHelper'
        // You can replace 'YourDatabaseHelper' with the actual name of your database helper class

        // Create an instance of your database helper class
        AccelerationDataDbHelper dbHelper = new AccelerationDataDbHelper(this);

        // Call the 'fetchHighestAccelerationForCurrentDay()' method to fetch the highest acceleration value for the current day

        double highestAcceleration = dbHelper.fetchHighestAccelerationForCurrentDay();
        if (highestAcceleration >= 0) {
            Log.d("StatisticsPage", "Highest acceleration for current day: " + highestAcceleration);
        } else {
            Log.d("StatisticsPage", "No data found for current day.");
        }


        // You can then use the 'highestAcceleration' value as needed in your 'StatisticsPage' class



    }
}