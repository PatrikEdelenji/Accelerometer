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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class StatisticsPageView extends AppCompatActivity {

    private TextView accelerationTextView;
    private TextView highestAccelerationTextView;
    private TextView averageTotalAccelerationTextView;
    private TextView timeAboveLimitTextView;
    private TextView numberOfAggressiveBrakingTextView;
    private TextView numberOfAggressiveAccelerationTextView;
    private TextView typeOfDriverTextView;
    private long startPickerLastSelectedDate = 0;
    private long endPickerLastSelectedDate = 0;
    private int lastSelection = 0;
    private int lastSelectedRadioButtonId = 0;



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


        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        lastSelectedRadioButtonId = sharedPreferences.getInt("lastSelectedRadioButtonId", -1);
        startPickerLastSelectedDate = sharedPreferences.getLong("startPickerLastSelectedDate", -1);
        endPickerLastSelectedDate = sharedPreferences.getLong("endPickerLastSelectedDate", -1);
        lastSelection = sharedPreferences.getInt("lastSelection", -1);
        Log.i("SELECTION", "Loaded last selection ON CREATE = " + lastSelection);
        if (lastSelectedRadioButtonId != -1 && lastSelection < 5) {
            RadioButton lastSelectedRadioButton = findViewById(lastSelectedRadioButtonId);
            dbHelper.getTimestamps(lastSelection);
            updateStatisticsUI();
            if (lastSelectedRadioButton != null) {
                lastSelectedRadioButton.setChecked(true);
            }
        }else if(lastSelectedRadioButtonId != 1 && lastSelection == 5){
            RadioButton lastSelectedRadioButton = findViewById(lastSelectedRadioButtonId);
            dbHelper.getTimestamps(startPickerLastSelectedDate, endPickerLastSelectedDate);
            updateStatisticsUI();
            if (lastSelectedRadioButton != null) {
                lastSelectedRadioButton.setChecked(true);
            }
        }

    }




    @Override
    protected void onResume() {
        super.onResume();
        // Retrieve the last selected radio button id from SharedPreferences and select it
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        lastSelectedRadioButtonId = sharedPreferences.getInt("lastSelectedRadioButtonId", -1);
        startPickerLastSelectedDate = sharedPreferences.getLong("startPickerLastSelectedDate", -1);
        endPickerLastSelectedDate = sharedPreferences.getLong("endPickerLastSelectedDate", -1);
        lastSelection = sharedPreferences.getInt("lastSelection", 0);
        if (lastSelectedRadioButtonId != -1 && lastSelection < 5) {
            RadioButton lastSelectedRadioButton = findViewById(lastSelectedRadioButtonId);
            dbHelper.getTimestamps(lastSelection);
            updateStatisticsUI();
            if (lastSelectedRadioButton != null) {
                lastSelectedRadioButton.setChecked(true);
            }
        }else if(lastSelectedRadioButtonId != 1 && lastSelection == 5){
            RadioButton lastSelectedRadioButton = findViewById(lastSelectedRadioButtonId);
            dbHelper.getTimestamps(startPickerLastSelectedDate, endPickerLastSelectedDate);
            updateStatisticsUI();
            if (lastSelectedRadioButton != null) {
                lastSelectedRadioButton.setChecked(true);
            }
        }

    }




    @Override
    protected void onPause() {
        super.onPause();
        // Save the last selected radio button id to SharedPreferences
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("lastSelectedRadioButtonId", lastSelectedRadioButtonId);
        editor.putInt("lastSelection", lastSelection);
        editor.putLong("startPickerLastSelectedDate", startPickerLastSelectedDate);
        editor.putLong("endPickerLastSelectedDate", endPickerLastSelectedDate);
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

        // Set the default values for the date pickers if last selected dates are available
        if (startPickerLastSelectedDate != -1) {
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTimeInMillis(startPickerLastSelectedDate);
            DatePicker startDatePicker = dialog.findViewById(R.id.fromDatePicker);
            startDatePicker.init(startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH), null);
        }

        if (endPickerLastSelectedDate != -1) {
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTimeInMillis(endPickerLastSelectedDate);
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


        Button deleteButton = dialog.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the confirmation dialog
                Dialog confirmationDialog = new Dialog(StatisticsPageView.this);
                confirmationDialog.setContentView(R.layout.confirmation_dialog);
                confirmationDialog.setCancelable(false);

                // Get the timestamp of the data to be deleted
                long timestamp = dbHelper.getTimestampOfDataToBeDeleted(); // Replace this with your actual method to retrieve the timestamp

                // Format the timestamp into a readable date and time string
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String formattedDate = dateFormat.format(new Date(timestamp));

                // Set the formatted date in the confirmation dialog
                TextView dateTextView = confirmationDialog.findViewById(R.id.dateTextView);
                dateTextView.setText(formattedDate);

                // Set up the buttons
                Button yesButton = confirmationDialog.findViewById(R.id.yesButton);
                Button noButton = confirmationDialog.findViewById(R.id.noButton);

                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dbHelper.removeDataByTimestamp();  // Call the data deletion method
                        confirmationDialog.dismiss();
                        dialog.dismiss();

                        onDataDeleted(); // Notify the data deletion to StatisticsPageView
                    }
                });

                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmationDialog.dismiss();
                    }
                });

                confirmationDialog.show();
            }
        });


        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RadioGroup timeRangeRadioGroup = dialog.findViewById(R.id.radioGroup);
                int selectedRadioButtonId = timeRangeRadioGroup.getCheckedRadioButtonId();
                lastSelectedRadioButtonId = selectedRadioButtonId;
                if (selectedRadioButtonId == R.id.todaysValues) {
                    lastSelection = 1;
                    dbHelper.getTimestamps(lastSelection);


                } else if (selectedRadioButtonId == R.id.values24hAgo) {
                    lastSelectedRadioButtonId = selectedRadioButtonId;
                    lastSelection = 2;
                    dbHelper.getTimestamps(lastSelection);

                } else if (selectedRadioButtonId == R.id.lastWeekValues) {
                    lastSelectedRadioButtonId = selectedRadioButtonId;
                    lastSelection = 3;
                    dbHelper.getTimestamps(lastSelection);

                } else if (selectedRadioButtonId == R.id.lastMonthValues) {
                    lastSelectedRadioButtonId = selectedRadioButtonId;
                    lastSelection = 4;
                    dbHelper.getTimestamps(lastSelection);

                } else if (selectedRadioButtonId == R.id.customValues) {
                    lastSelectedRadioButtonId = selectedRadioButtonId;
                    lastSelection = 5;
                    Calendar startCalendar = Calendar.getInstance();
                    DatePicker startDatePicker = dialog.findViewById(R.id.fromDatePicker);
                    startCalendar.set(startDatePicker.getYear(), startDatePicker.getMonth(), startDatePicker.getDayOfMonth(), 0, 0, 0);
                    startCalendar.set(Calendar.MILLISECOND, 0);
                    startPickerLastSelectedDate = startCalendar.getTimeInMillis();

                    Calendar endCalendar = Calendar.getInstance();
                    DatePicker endDatePicker = dialog.findViewById(R.id.toDatePicker);
                    endCalendar.set(endDatePicker.getYear(), endDatePicker.getMonth(), endDatePicker.getDayOfMonth(), 23, 59, 59);
                    endCalendar.set(Calendar.MILLISECOND, 999);
                    endPickerLastSelectedDate = endCalendar.getTimeInMillis();

                    dbHelper.getTimestamps(startPickerLastSelectedDate, endPickerLastSelectedDate);
                }

                SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("lastSelectedRadioButtonId", lastSelectedRadioButtonId);
                editor.putInt("lastSelection", lastSelection);
                editor.putLong("startPickerLastSelectedDate", startPickerLastSelectedDate);
                editor.putLong("endPickerLastSelectedDate", endPickerLastSelectedDate);
                Log.i("SELECTION", "Stored last selection INSIDE DIALOG = " + lastSelection);
                editor.apply();


                // Set up the delete button



                updateStatisticsUI();
                // Dismiss the dialog
                dialog.dismiss();
            }
        });
    }

    private void updateStatisticsUI() {


        // Update the UI with the new data

        double highestAcceleration = dbHelper.getHighestAcceleration();
        double averageAcceleration = dbHelper.getAverageAcceleration();
        double timeSpentAboveLimit = dbHelper.getTimeSpentAboveLimit();
        double percentageTimeAboveLimit = dbHelper.getPercentageAboveThreshold();
        int aggressiveBrakingCount = dbHelper.getAggressiveBrakingCount();
        int aggressiveAccelerationCount = dbHelper.getAggressiveAccelerationCount();

        String totalScore = PointCalculatorController.calculateScore(highestAcceleration, averageAcceleration, timeSpentAboveLimit, percentageTimeAboveLimit, aggressiveAccelerationCount, aggressiveBrakingCount);
        // Set text and colors for each TextView based on the calculated statistics
        highestAccelerationTextView.setText("Najveće zabilježeno ubrzavanje: " + highestAcceleration + "m/s^2");
        highestAccelerationTextView.setTextColor(highestAcceleration >= 2.93 ? Color.RED : Color.GREEN);

        averageTotalAccelerationTextView.setText("Prosječno zabilježeno ubrzavanje: " + averageAcceleration + "m/s^2");
        averageTotalAccelerationTextView.setTextColor(averageAcceleration >= 2.93f ? Color.RED : Color.GREEN);

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