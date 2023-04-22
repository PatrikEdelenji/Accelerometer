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
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.app.DatePickerDialog;
import java.util.Calendar;
import java.util.List;
import android.widget.TextView;


public class StatisticsPageView extends AppCompatActivity {

    private TextView accelerationTextView;
    private TextView highestAccelerationTextView;
    private TextView biggestDifferenceTextView;
    private TextView averageTotalAccelerationTextView;
    private TextView timeAboveLimitTextView;
    private TextView numberOfAggressiveBreakingTextView;
    private TextView numberOfAggressiveAccelerationTextView;

    private float highestAcceleration;
    private float averageAcceleration;
    private float timeSpentAboveLimit;
    private int aggressiveBrakingCount;
    private int aggressiveAccelerationCount;

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


        // Set text and colors for each TextView based on the calculated statistics
        highestAccelerationTextView.setText(String.format("%.2f m/s^2", highestAcceleration));
        highestAccelerationTextView.setTextColor(highestAcceleration >= 3.5f ? Color.RED : Color.GREEN);

        averageTotalAccelerationTextView.setText(String.format("%.2f m/s^2", averageAcceleration));
        averageTotalAccelerationTextView.setTextColor(averageAcceleration >= 3.5f ? Color.RED : Color.GREEN);

        timeAboveLimitTextView.setText(String.format("%d seconds", (int) timeSpentAboveLimit));
        timeAboveLimitTextView.setTextColor(timeSpentAboveLimit == 0 ? Color.GREEN : Color.BLACK);

        numberOfAggressiveBreakingTextView.setText(String.format("%d", aggressiveBrakingCount));
        numberOfAggressiveBreakingTextView.setTextColor(Color.RED);

        numberOfAggressiveAccelerationTextView.setText(String.format("%d", aggressiveAccelerationCount));
        numberOfAggressiveAccelerationTextView.setTextColor(Color.RED);
    }


    private void showFilterDialog() {

        AccelerationDataDbHelper dbHelper = new AccelerationDataDbHelper(this);

        // Create a new dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.statistics_page_menu);


        // Find the filter button in the dialog and set its OnClickListener
        Button filterButton = dialog.findViewById(R.id.sortButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle filter button click here
                // Get the selected time range from the radio buttons
                RadioGroup timeRangeRadioGroup = dialog.findViewById(R.id.statistics_page_button);
                int selectedRadioButtonId = timeRangeRadioGroup.getCheckedRadioButtonId();
                String timeRange = "";
                long startTimestamp = 0;
                long endTimestamp = 0;
                double highestAcceleration = 0;
                double averageAcceleration = 0;
                double timeSpentAboveLimit = 0;
                int aggressiveBrakingCount = 0;
                int aggressiveAccelerationCount = 0;

                if (selectedRadioButtonId == R.id.todaysValues) {
                    // Get data for today
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    startTimestamp = calendar.getTimeInMillis();
                    endTimestamp = System.currentTimeMillis();
                    timeRange = "Today";
                    highestAcceleration = dbHelper.getHighestAcceleration(startTimestamp, endTimestamp);
                    averageAcceleration = dbHelper.getAverageAcceleration(startTimestamp, endTimestamp);
                    timeSpentAboveLimit = dbHelper.getTimeSpentAboveLimit(startTimestamp, endTimestamp);
                    aggressiveBrakingCount = dbHelper.getAggressiveBrakingCount(startTimestamp, endTimestamp);
                    aggressiveAccelerationCount = dbHelper.getAggressiveAccelerationCount(startTimestamp, endTimestamp);

                } else if (selectedRadioButtonId == R.id.values24hAgo) {
                    // Get data from the past 24 hours
                    startTimestamp = System.currentTimeMillis() - 86400000L; // 86400000 ms = 24 hours
                    endTimestamp = System.currentTimeMillis();
                    timeRange = "Last 24 Hours";
                    highestAcceleration = dbHelper.getHighestAcceleration(startTimestamp, endTimestamp);
                    averageAcceleration = dbHelper.getAverageAcceleration(startTimestamp, endTimestamp);
                    timeSpentAboveLimit = dbHelper.getTimeSpentAboveLimit(startTimestamp, endTimestamp);
                    aggressiveBrakingCount = dbHelper.getAggressiveBrakingCount(startTimestamp, endTimestamp);
                    aggressiveAccelerationCount = dbHelper.getAggressiveAccelerationCount(startTimestamp, endTimestamp);

                } else if (selectedRadioButtonId == R.id.lastWeekValues) {
                    // Get data from the past 7 days
                    startTimestamp = System.currentTimeMillis() - 604800000L; // 604800000 ms = 7 days
                    endTimestamp = System.currentTimeMillis();
                    timeRange = "Last 7 Days";
                    highestAcceleration = dbHelper.getHighestAcceleration(startTimestamp, endTimestamp);
                    averageAcceleration = dbHelper.getAverageAcceleration(startTimestamp, endTimestamp);
                    timeSpentAboveLimit = dbHelper.getTimeSpentAboveLimit(startTimestamp, endTimestamp);
                    aggressiveBrakingCount = dbHelper.getAggressiveBrakingCount(startTimestamp, endTimestamp);
                    aggressiveAccelerationCount = dbHelper.getAggressiveAccelerationCount(startTimestamp, endTimestamp);
                } else if (selectedRadioButtonId == R.id.lastMonthValues) {
                    // Get data from the past 30 days
                    startTimestamp = System.currentTimeMillis() - 2592000000L; // 2592000000 ms = 30 days
                    endTimestamp = System.currentTimeMillis();
                    timeRange = "Last 30 Days";
                    highestAcceleration = dbHelper.getHighestAcceleration(startTimestamp, endTimestamp);
                    averageAcceleration = dbHelper.getAverageAcceleration(startTimestamp, endTimestamp);
                    timeSpentAboveLimit = dbHelper.getTimeSpentAboveLimit(startTimestamp, endTimestamp);
                    aggressiveBrakingCount = dbHelper.getAggressiveBrakingCount(startTimestamp, endTimestamp);
                    aggressiveAccelerationCount = dbHelper.getAggressiveAccelerationCount(startTimestamp, endTimestamp);
                } else if (selectedRadioButtonId == R.id.customValues) {
                    // Get data for the custom time range selected on the calendar
                    Calendar startCalendar = Calendar.getInstance();
                    DatePicker startDatePicker = dialog.findViewById(R.id.fromDatePicker);
                    startCalendar.set(startDatePicker.getYear(), startDatePicker.getMonth(), startDatePicker.getDayOfMonth());
                    startCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    startCalendar.set(Calendar.MINUTE, 0);
                    startCalendar.set(Calendar.SECOND, 0);
                    startTimestamp = startCalendar.getTimeInMillis();

                    Calendar endCalendar = Calendar.getInstance();
                    DatePicker endDatePicker = dialog.findViewById(R.id.toDatePicker);
                    endCalendar.set(endDatePicker.getYear(), endDatePicker.getMonth(), endDatePicker.getDayOfMonth());
                    endCalendar.set(Calendar.HOUR_OF_DAY, 23);
                    endCalendar.set(Calendar.MINUTE, 59);
                    endCalendar.set(Calendar.SECOND, 59);
                    endTimestamp = endCalendar.getTimeInMillis();

                    timeRange = "Custom Range";
                    highestAcceleration = dbHelper.getHighestAcceleration(startTimestamp, endTimestamp);
                    averageAcceleration = dbHelper.getAverageAcceleration(startTimestamp, endTimestamp);
                    timeSpentAboveLimit = dbHelper.getTimeSpentAboveLimit(startTimestamp, endTimestamp);
                    aggressiveBrakingCount = dbHelper.getAggressiveBrakingCount(startTimestamp, endTimestamp);
                    aggressiveAccelerationCount = dbHelper.getAggressiveAccelerationCount(startTimestamp, endTimestamp);
                }
                }
            });
        }
    }