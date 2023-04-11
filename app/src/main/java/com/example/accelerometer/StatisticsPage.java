package com.example.accelerometer;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class StatisticsPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.statistics_page); // Set the layout for this activity
        // Initialize and set up your activity here
        fetchAllData();

        // Placeholder code for illustration
        // Retrieve and display statistics data from SQLite database
        // Set up UI elements to display statistics
        // Configure event listeners or callbacks for user interactions



    }
}