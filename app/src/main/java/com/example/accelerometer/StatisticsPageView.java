package com.example.accelerometer;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class StatisticsPageView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics_page);
        AccelerationDataDbHelper dbHelper = new AccelerationDataDbHelper(this);


        // Fetch the highest acceleration value for the current day
        double highestAcceleration = dbHelper.fetchHighestAccelerationForCurrentDay();
        if (highestAcceleration >= 0) {
            Log.d("StatisticsPageView", "Highest acceleration for current day: " + highestAcceleration + "m/s2");

            // Update the UI with the fetched highest acceleration value
            TextView highestAccelerationTextView = findViewById(R.id.highestAccelerationTextView);
            highestAccelerationTextView.setText("Highest Acceleration: " + highestAcceleration + "m/s2");
            if (highestAcceleration >= 3.5) {
                highestAccelerationTextView.setTextColor(Color.RED);
            } else {
                highestAccelerationTextView.setTextColor(Color.parseColor("#006400")); // Set text color to dark green
            }

        } else {
            Log.d("StatisticsPageView", "No data found for current day.");
        }



        double biggestDifference = dbHelper.calculateBiggestAccelerationDifference();
        TextView biggestDifferenceTextView = findViewById(R.id.biggestDifferenceTextView);
        if(biggestDifference >= 0) {
            biggestDifferenceTextView.setText("Highest acceleration difference: " + biggestDifference + "m/s2");

        }


        // Calculate the average total acceleration
        double averageTotalAcceleration = dbHelper.calculateAverageTotalAcceleration();
        if (averageTotalAcceleration >= 0) {
            TextView averageTotalAccelerationTextView = findViewById(R.id.averageTotalAccelerationTextView);
            averageTotalAccelerationTextView.setText("Average Total Acceleration: " + averageTotalAcceleration + "m/s2");
            if(averageTotalAcceleration >= 3.5) {
                Log.d("StatisticsPageView", "Average total acceleration: " + averageTotalAcceleration + "m/s2");
                averageTotalAccelerationTextView.setTextColor(Color.RED);
                // Update the UI with the calculated average total acceleration value
            }
            else {
                averageTotalAccelerationTextView.setTextColor(Color.parseColor("#006400")); // Set text color to dark green
            }
        } else {
            Log.d("StatisticsPageView", "No data found for current day.");
        }


        // Calculate time above limit
        double timeAboveLimitInMillis = dbHelper.calculateTimeSpentAboveLimit();

        // Format time above limit value as hours, minutes, and seconds
        String formattedTimeAboveLimit = dbHelper.formatTimeSpentAboveLimit(timeAboveLimitInMillis);

        // Update the text of the 'timeAboveLimitTextView' with the formatted time above limit value
        TextView timeAboveLimitTextView = findViewById(R.id.timeAboveLimitTextView);
        timeAboveLimitTextView.setText("Time above the limit: " + formattedTimeAboveLimit);



// The amount of times the acceleration limit was breached
        double numberOfAggressiveAccelerationBreaches = dbHelper.getAggressiveAccelerationCount();
        TextView numberOfAggressiveAccelerationBreachesTextView = findViewById(R.id.numberOfAggressiveAccelerationTextView);
        if (numberOfAggressiveAccelerationBreaches >= 0){
            numberOfAggressiveAccelerationBreachesTextView.setText("Broj naglih ubrzavanja: " + String.valueOf(numberOfAggressiveAccelerationBreaches));
            numberOfAggressiveAccelerationBreachesTextView.setTextColor(Color.RED);
        }
        else{
            numberOfAggressiveAccelerationBreachesTextView.setText("Broj naglih ubrzavanja: Nema podataka");
        }


        double numberOfAggressiveBrakingBreaches = dbHelper.getAggressiveBrakingCount();
        TextView numberOfAggressiveBrakingBreachesTextView = findViewById(R.id.numberOfAggressiveBreakingTextView);
        if (numberOfAggressiveBrakingBreaches >= 0){
            numberOfAggressiveBrakingBreachesTextView.setText("Broj agresivnih kočenja: " + String.valueOf(numberOfAggressiveBrakingBreaches));
            numberOfAggressiveBrakingBreachesTextView.setTextColor(Color.RED);
        }
        else{
            numberOfAggressiveBrakingBreachesTextView.setText("Broj agresivnih kočenja: Nema podataka");
        }

    }




}