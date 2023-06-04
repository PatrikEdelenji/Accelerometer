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
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class StatisticsPageActivity extends AppCompatActivity {


    private TextView highestAccelerationTextView;
    private TextView averageTotalAccelerationTextView;
    private TextView timeAboveLimitTextView;
    private TextView numberOfAggressiveBrakingTextView;
    private TextView numberOfAggressiveAccelerationTextView;
    private TextView typeOfDriverTextView;
    private TextView selectedTimestampsTextView;
    private long lastStartTimestamp = 0;
    private long lastEndTimestamp = 0;
    private int lastSelectedRadioButtonId = 0;
    String startDate = "";
    String endDate = "";

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

        highestAccelerationTextView = findViewById(R.id.highestAccelerationTextView);
        averageTotalAccelerationTextView = findViewById(R.id.averageTotalAccelerationTextView);
        timeAboveLimitTextView = findViewById(R.id.timeAboveLimitTextView);
        numberOfAggressiveBrakingTextView = findViewById(R.id.numberOfAggressiveBrakingTextView);
        numberOfAggressiveAccelerationTextView = findViewById(R.id.numberOfAggressiveAccelerationTextView);
        selectedTimestampsTextView = findViewById(R.id.selectedTimestampsTextView);
        typeOfDriverTextView = findViewById(R.id.typeOfDriver);


        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        lastSelectedRadioButtonId = sharedPreferences.getInt("lastSelectedRadioButtonId", -1);
        lastStartTimestamp = sharedPreferences.getLong("lastStartTimestamp", -1);
        lastEndTimestamp = sharedPreferences.getLong("lastEndTimestamp", -1);
        if (lastSelectedRadioButtonId != -1) {
            RadioButton lastSelectedRadioButton = findViewById(lastSelectedRadioButtonId);
            updateStatisticsUI(lastStartTimestamp, lastEndTimestamp);
            if (lastSelectedRadioButton != null) {
                lastSelectedRadioButton.setChecked(true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        lastSelectedRadioButtonId = sharedPreferences.getInt("lastSelectedRadioButtonId", -1);
        lastStartTimestamp = sharedPreferences.getLong("lastStartTimestamp", -1);
        lastEndTimestamp = sharedPreferences.getLong("lastEndTimestamp", -1);
        if (lastSelectedRadioButtonId != -1) {
            RadioButton lastSelectedRadioButton = findViewById(lastSelectedRadioButtonId);
            updateStatisticsUI(lastStartTimestamp, lastEndTimestamp);
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
        editor.putLong("lastStartTimestamp", lastStartTimestamp);
        editor.putLong("lastEndTimestamp", lastEndTimestamp);
        editor.apply();
    }


    private void showFilterDialog() {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.statistics_page_menu);
        dialog.setCancelable(false);


        RadioButton lastSelectedRadioButton = dialog.findViewById(lastSelectedRadioButtonId);
        if (lastSelectedRadioButton != null) {
            lastSelectedRadioButton.setChecked(true);
        }

        RadioGroup timeRangeRadioGroup = dialog.findViewById(R.id.radioGroup);

        Calendar startCalendar = Calendar.getInstance();
        DatePicker startDatePicker = dialog.findViewById(R.id.fromDatePicker);
        DatePicker endDatePicker = dialog.findViewById(R.id.toDatePicker);
        if (lastStartTimestamp != -1) {
            startCalendar.setTimeInMillis(lastStartTimestamp);
            startDatePicker.init(startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH), null);
        }
        Calendar endCalendar = Calendar.getInstance();
        if (lastEndTimestamp != -1) {
            endCalendar.setTimeInMillis(lastEndTimestamp);
            endDatePicker.init(endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH), null);
        }

        dialog.show();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);



        final AtomicInteger selectedRadioButtonId = new AtomicInteger();
        RadioGroup.OnCheckedChangeListener radioGroupListener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                selectedRadioButtonId.set(checkedId); // Update the selectedRadioButtonId value
                calculateTimestamps(startCalendar, endCalendar, startDatePicker, endDatePicker, selectedRadioButtonId.get());
            }
        };

        DatePicker.OnDateChangedListener startDateListener = new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calculateTimestamps(startCalendar, endCalendar, startDatePicker, endDatePicker, selectedRadioButtonId.get());
            }
        };

        DatePicker.OnDateChangedListener endDateListener = new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calculateTimestamps(startCalendar, endCalendar, startDatePicker, endDatePicker, selectedRadioButtonId.get());
            }
        };

        timeRangeRadioGroup.setOnCheckedChangeListener(radioGroupListener);
        startDatePicker.init(startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH), startDateListener);
        endDatePicker.init(endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH), endDateListener);


        Button deleteButton = dialog.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the delete function
                deleteData(lastStartTimestamp, lastEndTimestamp);
            }
        });

        // Set up the confirm button
        Button confirmButton = dialog.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });





    }

    private void updateStatisticsUI(long lastStartTimestamp, long lastEndTimestamp) {

        double highestAcceleration = dbHelper.getHighestAcceleration(lastStartTimestamp, lastEndTimestamp);
        double averageAcceleration = dbHelper.getAverageAcceleration(lastStartTimestamp, lastEndTimestamp);
        double timeSpentAboveLimit = dbHelper.getTimeSpentAboveLimit(lastStartTimestamp, lastEndTimestamp);
        double percentageTimeAboveLimit = dbHelper.getPercentageAboveThreshold(lastStartTimestamp, lastEndTimestamp);
        int aggressiveBrakingCount = dbHelper.getAggressiveBrakingCount(lastStartTimestamp, lastEndTimestamp);
        int aggressiveAccelerationCount = dbHelper.getAggressiveAccelerationCount(lastStartTimestamp, lastEndTimestamp);

        String totalScore = PointCalculatorController.calculateScore(highestAcceleration,
                                                                        averageAcceleration,
                                                                        timeSpentAboveLimit,
                                                                        percentageTimeAboveLimit,
                                                                        aggressiveAccelerationCount,
                                                                        aggressiveBrakingCount,
                                                                        lastStartTimestamp,
                                                                        lastEndTimestamp);
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

        // Format the timestamps into readable date and time strings
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());

        startDate = dateFormat.format(new Date(lastStartTimestamp));
        Log.i("STATISTIKA", "lastStartTimestamp: " + lastStartTimestamp);
        endDate = dateFormat.format(new Date(lastEndTimestamp));
        Log.i("STATISTIKA", "lastEndTimestamp: " + lastEndTimestamp);

        if(lastStartTimestamp != 0 && lastEndTimestamp != 0){
            String selectedTimestampsMessage = "Podaci od: " + startDate + " do " + endDate;
            // Set the message in the TextView
            selectedTimestampsTextView.setText(selectedTimestampsMessage);
        }else{
            String selectedTimestampsMessage = "Svi podaci:";
            selectedTimestampsTextView.setText(selectedTimestampsMessage);
        }
    }


    public void calculateTimestamps(Calendar startCalendar, Calendar endCalendar, DatePicker startDatePicker, DatePicker endDatePicker , int selectedRadioButtonId){

        Log.i("BRUH", "Actually executed");
        if (selectedRadioButtonId == R.id.todaysValues) {
            Calendar calendarToday = Calendar.getInstance();
            calendarToday.set(Calendar.HOUR_OF_DAY, 0);
            calendarToday.set(Calendar.MINUTE, 0);
            calendarToday.set(Calendar.SECOND, 0);
            lastSelectedRadioButtonId = selectedRadioButtonId;
            lastStartTimestamp = calendarToday.getTimeInMillis();
            lastEndTimestamp = System.currentTimeMillis();
        } else if (selectedRadioButtonId == R.id.values24hAgo) {
            lastSelectedRadioButtonId = selectedRadioButtonId;
            lastStartTimestamp = System.currentTimeMillis() - 86400000L; // 86400000 ms = 24 hours
            lastEndTimestamp = System.currentTimeMillis();
        } else if (selectedRadioButtonId == R.id.lastWeekValues) {
            lastSelectedRadioButtonId = selectedRadioButtonId;
            lastStartTimestamp = System.currentTimeMillis() - 604800000L; // 604800000 ms = 7 days
            lastEndTimestamp = System.currentTimeMillis();
        } else if (selectedRadioButtonId == R.id.lastMonthValues) {
            lastSelectedRadioButtonId = selectedRadioButtonId;
            lastStartTimestamp = System.currentTimeMillis() - 2592000000L; // 2592000000 ms = 30 days
            lastEndTimestamp = System.currentTimeMillis();
        } else if (selectedRadioButtonId == R.id.allTimeValues) {
            lastSelectedRadioButtonId = selectedRadioButtonId;
            lastStartTimestamp = 0;
            lastEndTimestamp = System.currentTimeMillis();
        } else if (selectedRadioButtonId == R.id.customValues) {
            lastSelectedRadioButtonId = selectedRadioButtonId;

            startCalendar.set(startDatePicker.getYear(), startDatePicker.getMonth(), startDatePicker.getDayOfMonth(), 0, 0, 0);
            startCalendar.set(Calendar.MILLISECOND, 0);
            lastStartTimestamp = startCalendar.getTimeInMillis();

            endCalendar.set(endDatePicker.getYear(), endDatePicker.getMonth(), endDatePicker.getDayOfMonth(), 23, 59, 59);
            endCalendar.set(Calendar.MILLISECOND, 999);
            lastEndTimestamp = endCalendar.getTimeInMillis();

        }
        Log.i("BRUH", "CalculateTimestamps \n lastSelectedButton = " + lastSelectedRadioButtonId + "\n lastStartTimestamp = " + lastStartTimestamp + "\n lastEndTimestamp = " + lastEndTimestamp);
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("lastSelectedRadioButtonId", lastSelectedRadioButtonId);
        editor.putLong("lastStartTimestamp", lastStartTimestamp);
        editor.putLong("lastEndTimestamp", lastEndTimestamp);
        editor.apply();

        updateStatisticsUI(lastStartTimestamp, lastEndTimestamp);

    }


    private void deleteData(long startTimestamp, long endTimestamp) {
        // Show the confirmation dialog
        Dialog confirmationDialog = new Dialog(StatisticsPageActivity.this);
        confirmationDialog.setContentView(R.layout.confirmation_dialog);
        confirmationDialog.setCancelable(false);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        String startDate = dateFormat.format(new Date(startTimestamp));
        String endDate = dateFormat.format(new Date(endTimestamp));
        String message = "";

        if (startTimestamp != 0) {
            message = "Jeste li sigurni da želite obrisati podatke od: " + startDate + " do " + endDate + "?";
        } else {
            message = "Jeste li sigurni da želite obrisati SVE podatke?";
        }


        TextView timestampsTextView = confirmationDialog.findViewById(R.id.timestampsTextView);
        timestampsTextView.setText(message);

        Button yesButton = confirmationDialog.findViewById(R.id.yesButton);
        Button noButton = confirmationDialog.findViewById(R.id.noButton);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.removeDataByTimestamp(lastStartTimestamp, lastEndTimestamp);  // Call the data deletion method
                confirmationDialog.dismiss();

                // Show data deleted notification
                Toast.makeText(StatisticsPageActivity.this, "Podaci obrisani!", Toast.LENGTH_SHORT).show();
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





}

