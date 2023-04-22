package com.example.accelerometer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.app.DatePickerDialog;
import java.util.Calendar;

public class StatisticsPageView extends AppCompatActivity {

    private TextView accelerationTextView;
    private TextView highestAccelerationTextView;
    private TextView biggestDifferenceTextView;
    private TextView averageTotalAccelerationTextView;
    private TextView timeAboveLimitTextView;
    private TextView numberOfAggressiveBreakingTextView;
    private TextView numberOfAggressiveAccelerationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics_page);
        AccelerationDataDbHelper dbHelper = new AccelerationDataDbHelper(this);

        Button sortButton = findViewById(R.id.sortButton);
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });



        // Find each TextView by ID
        accelerationTextView = findViewById(R.id.accelerationTextView);
        highestAccelerationTextView = findViewById(R.id.highestAccelerationTextView);
        biggestDifferenceTextView = findViewById(R.id.biggestDifferenceTextView);
        averageTotalAccelerationTextView = findViewById(R.id.averageTotalAccelerationTextView);
        timeAboveLimitTextView = findViewById(R.id.timeAboveLimitTextView);
        numberOfAggressiveBreakingTextView = findViewById(R.id.numberOfAggressiveBreakingTextView);
        numberOfAggressiveAccelerationTextView = findViewById(R.id.numberOfAggressiveAccelerationTextView);


        // Get values from the database
        double highestAcceleration = dbHelper.calculateAverageTotalAcceleration();
        double averageAcceleration = dbHelper.calculateAverageTotalAcceleration();
        double timeAboveLimit = dbHelper.calculateTimeSpentAboveLimit();
        int aggressiveBreaking = dbHelper.getAggressiveBrakingCount();
        int aggressiveAcceleration = dbHelper.getAggressiveAccelerationCount();

        // Set text and colors for each TextView
        accelerationTextView.setText(String.format("%.2f m/s^2", highestAcceleration));
        highestAccelerationTextView.setText(String.format("%.2f m/s^2", highestAcceleration));
        if (highestAcceleration >= 3.5f) {
            highestAccelerationTextView.setTextColor(Color.RED);
        } else {
            highestAccelerationTextView.setTextColor(Color.GREEN);
        }
        averageTotalAccelerationTextView.setText(String.format("%.2f m/s^2", averageAcceleration));
        if (averageAcceleration >= 3.5f) {
            averageTotalAccelerationTextView.setTextColor(Color.RED);
        } else {
            averageTotalAccelerationTextView.setTextColor(Color.GREEN);
        }
        timeAboveLimitTextView.setText(String.format("%d seconds", timeAboveLimit));
        if (timeAboveLimit == 0) {
            timeAboveLimitTextView.setTextColor(Color.GREEN);
        } else {
            timeAboveLimitTextView.setTextColor(Color.BLACK);
        }
        numberOfAggressiveBreakingTextView.setText(String.format("%d", aggressiveBreaking));
        numberOfAggressiveBreakingTextView.setTextColor(Color.RED);
        numberOfAggressiveAccelerationTextView.setText(String.format("%d", aggressiveAcceleration));
        numberOfAggressiveAccelerationTextView.setTextColor(Color.RED);
    }


    private void showFilterDialog() {
        // Create a new dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.statistics_page_menu);

        // Find the filter button in the dialog and set its OnClickListener
        Button filterButton = dialog.findViewById(R.id.sortButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle filter button click here
                dialog.dismiss(); // Close the dialog
            }
        });

        // Display the dialog
        dialog.show();
    }

}



