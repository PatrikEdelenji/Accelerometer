package com.example.accelerometer;

import androidx.appcompat.app.AppCompatActivity;


import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;


public class StatisticsPageView extends AppCompatActivity {

    private TextView accelerationTextView;
    private TextView highestAccelerationTextView;
    private TextView averageTotalAccelerationTextView;
    private TextView timeAboveLimitTextView;
    private TextView numberOfAggressiveBrakingTextView;
    private TextView numberOfAggressiveAccelerationTextView;
    private TextView typeOfDriverTextView;
    private long startTimestamp = 0;
    private long endTimestamp = 0;

    private int lastSelectedRadioButtonId = 0;
    private Calendar startPickerLastSelectedDate = null;
    private Calendar endPickerLastSelectedDate = null;
    private long lastSelectedStartTimestamp = 0;
    private long lastSelectedEndTimestamp = 0;


    AccelerationDataDbHelper dbHelper = new AccelerationDataDbHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics_page);



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
        averageTotalAccelerationTextView = findViewById(R.id.averageTotalAccelerationTextView);
        timeAboveLimitTextView = findViewById(R.id.timeAboveLimitTextView);
        numberOfAggressiveBrakingTextView = findViewById(R.id.numberOfAggressiveBrakingTextView);
        numberOfAggressiveAccelerationTextView = findViewById(R.id.numberOfAggressiveAccelerationTextView);
        typeOfDriverTextView = findViewById(R.id.typeOfDriver);
        // Update the UI with the default time range
        updateStatisticsUI(startTimestamp, endTimestamp);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Retrieve the last selected radio button id from SharedPreferences and select it
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        lastSelectedRadioButtonId = sharedPreferences.getInt("lastSelectedRadioButtonId", -1);
        if (lastSelectedRadioButtonId != -1) {
            RadioButton lastSelectedRadioButton = findViewById(lastSelectedRadioButtonId);
            if (lastSelectedRadioButton != null) {
                lastSelectedRadioButton.setChecked(true);
            }
        }
        lastSelectedStartTimestamp = sharedPreferences.getLong("lastSelectedStartTimestamp", 0);
        lastSelectedEndTimestamp = sharedPreferences.getLong("lastSelectedEndTimestamp", 0);
        if(lastSelectedStartTimestamp != 0 && lastSelectedEndTimestamp != 0){
            updateStatisticsUI(lastSelectedStartTimestamp, lastSelectedEndTimestamp);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save the last selected radio button id to SharedPreferences
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("lastSelectedRadioButtonId", lastSelectedRadioButtonId);
        editor.putLong("lastSelectedStartTimestamp", lastSelectedStartTimestamp);
        editor.putLong("lastSelectedEndTimestamp", lastSelectedEndTimestamp);
        editor.apply();
    }

    private void showFilterDialog() {

        // Create a new dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.statistics_page_menu);
        dialog.setCancelable(false);

        // Set the checked state of the radio button based on the last selected ID
        RadioButton lastSelectedRadioButton = dialog.findViewById(lastSelectedRadioButtonId);
        if (lastSelectedRadioButton != null) {
            lastSelectedRadioButton.setChecked(true);
        }

        // Load the last selected dates from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        long startPickerLastSelectedDateMillis = preferences.getLong("startPickerLastSelectedDate", -1);
        long endPickerLastSelectedDateMillis = preferences.getLong("endPickerLastSelectedDate", -1);


        // Set the default values for the date pickers if last selected dates are available
        if (startPickerLastSelectedDateMillis != -1) {
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTimeInMillis(startPickerLastSelectedDateMillis);
            DatePicker startDatePicker = dialog.findViewById(R.id.fromDatePicker);
            startDatePicker.init(startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH), null);
        }

        if (endPickerLastSelectedDateMillis != -1) {
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTimeInMillis(endPickerLastSelectedDateMillis);
            DatePicker endDatePicker = dialog.findViewById(R.id.toDatePicker);
            endDatePicker.init(endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH), null);
        }

        dialog.show();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);


        // Set up the confirm button
        Button confirmButton = dialog.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioGroup timeRangeRadioGroup = dialog.findViewById(R.id.radioGroup);
                int selectedRadioButtonId = timeRangeRadioGroup.getCheckedRadioButtonId();
                lastSelectedRadioButtonId = selectedRadioButtonId;
                long startTimestamp = 0;
                long endTimestamp = 0;
                if (selectedRadioButtonId == R.id.todaysValues) {
                    // Get data for today
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    startTimestamp = calendar.getTimeInMillis();
                    endTimestamp = System.currentTimeMillis();
                    lastSelectedStartTimestamp  = startTimestamp;
                    lastSelectedEndTimestamp = endTimestamp;
                } else if (selectedRadioButtonId == R.id.values24hAgo) {
                    // Get data from the past 24 hours
                    startTimestamp = System.currentTimeMillis() - 86400000L; // 86400000 ms = 24 hours
                    endTimestamp = System.currentTimeMillis();
                    lastSelectedStartTimestamp  = startTimestamp;
                    lastSelectedEndTimestamp = endTimestamp;
                } else if (selectedRadioButtonId == R.id.lastWeekValues) {
                    // Get data from the past 7 days
                    startTimestamp = System.currentTimeMillis() - 604800000L; // 604800000 ms = 7 days
                    endTimestamp = System.currentTimeMillis();
                    lastSelectedStartTimestamp  = startTimestamp;
                    lastSelectedEndTimestamp = endTimestamp;
                } else if (selectedRadioButtonId == R.id.lastMonthValues) {
                    // Get data from the past 30 days
                    startTimestamp = System.currentTimeMillis() - 2592000000L; // 2592000000 ms = 30 days
                    endTimestamp = System.currentTimeMillis();
                    lastSelectedStartTimestamp  = startTimestamp;
                    lastSelectedEndTimestamp = endTimestamp;
                } else if (selectedRadioButtonId == R.id.customValues) {
                    Calendar startCalendar = Calendar.getInstance();
                    DatePicker startDatePicker = dialog.findViewById(R.id.fromDatePicker);
                    startCalendar.set(startDatePicker.getYear(), startDatePicker.getMonth(), startDatePicker.getDayOfMonth(), 0, 0, 0);
                    startCalendar.set(Calendar.MILLISECOND, 0);
                    startTimestamp = startCalendar.getTimeInMillis();

                    Calendar endCalendar = Calendar.getInstance();
                    DatePicker endDatePicker = dialog.findViewById(R.id.toDatePicker);
                    endCalendar.set(endDatePicker.getYear(), endDatePicker.getMonth(), endDatePicker.getDayOfMonth(), 23, 59, 59);
                    endCalendar.set(Calendar.MILLISECOND, 999);
                    endTimestamp = endCalendar.getTimeInMillis();

                    lastSelectedStartTimestamp  = startTimestamp;
                    lastSelectedEndTimestamp = endTimestamp;

                    startPickerLastSelectedDate = startCalendar;
                    endPickerLastSelectedDate = endCalendar;

                }


                // Update the statistics UI with the selected time range
                updateStatisticsUI(startTimestamp, endTimestamp);

                // Dismiss the dialog
                dialog.dismiss();
            }
        });
    }

    private void updateStatisticsUI(long startTimestamp, long endTimestamp) {
        // Update the UI with the new data

        double highestAcceleration = dbHelper.getHighestAcceleration(startTimestamp, endTimestamp);
        double averageAcceleration = dbHelper.getAverageAcceleration(startTimestamp, endTimestamp);
        double timeSpentAboveLimit = dbHelper.getTimeSpentAboveLimit(startTimestamp, endTimestamp);
        double percentageTimeAboveLimit = dbHelper.getPercentageAboveThreshold(startTimestamp, endTimestamp);
        int aggressiveBrakingCount = dbHelper.getAggressiveBrakingCount(startTimestamp, endTimestamp);
        int aggressiveAccelerationCount = dbHelper.getAggressiveAccelerationCount(startTimestamp, endTimestamp);

        String totalScore = PointCalculatorController.calculateScore(highestAcceleration, averageAcceleration, timeSpentAboveLimit, percentageTimeAboveLimit, aggressiveAccelerationCount, aggressiveBrakingCount, startTimestamp, endTimestamp);




        // Set text and colors for each TextView based on the calculated statistics
        highestAccelerationTextView.setText("Najveće zabilježeno ubrzavanje: " + highestAcceleration + "m/s^2");
        highestAccelerationTextView.setTextColor(highestAcceleration >= 3.5f ? Color.RED : Color.GREEN);

        averageTotalAccelerationTextView.setText("Prosječno zabilježeno ubrzavanje: " + averageAcceleration + "m/s^2");
        averageTotalAccelerationTextView.setTextColor(averageAcceleration >= 3.5f ? Color.RED : Color.GREEN);

        long timeSpentAboveLimitInSeconds = (long) timeSpentAboveLimit / 1000;
        long hours = TimeUnit.SECONDS.toHours(timeSpentAboveLimitInSeconds);
        long minutes = TimeUnit.SECONDS.toMinutes(timeSpentAboveLimitInSeconds - TimeUnit.HOURS.toSeconds(hours));
        long seconds = timeSpentAboveLimitInSeconds - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes);


        String timeAboveLimitString = String.format("%02d h:%02d min:%02d s", hours, minutes, seconds);
        String percentageTimeAboveLimitString = String.format("%.2f", percentageTimeAboveLimit);
        timeAboveLimitTextView.setText("Vrijeme provedeno iznad limita: " + timeAboveLimitString + "(" + percentageTimeAboveLimitString + "% ukupnog vremena)");

        numberOfAggressiveBrakingTextView.setText("Broj naglih kočenja: " + aggressiveBrakingCount);
        numberOfAggressiveBrakingTextView.setTextColor(Color.RED);

        numberOfAggressiveAccelerationTextView.setText("Broj agresivnih ubrzavajna: " + aggressiveAccelerationCount);
        numberOfAggressiveAccelerationTextView.setTextColor(Color.RED);

        typeOfDriverTextView.setText("Vi ste: " + totalScore);
    }

}