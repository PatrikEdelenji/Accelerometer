package com.example.accelerometer;

import androidx.appcompat.app.AppCompatActivity;


import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.statistics_page_menu);
        dialog.setCancelable(false);

        Calendar startCalendar = Calendar.getInstance();
        DatePicker startDatePicker = dialog.findViewById(R.id.fromDatePicker);

        Calendar endCalendar = Calendar.getInstance();
        DatePicker endDatePicker = dialog.findViewById(R.id.toDatePicker);




        Button sortButton = findViewById(R.id.sortButton);

        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog(startCalendar, endCalendar, startDatePicker, endDatePicker, dialog);
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



        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.statistics_page_menu);
        dialog.setCancelable(false);

        Calendar startCalendar = Calendar.getInstance();
        DatePicker startDatePicker = dialog.findViewById(R.id.fromDatePicker);

        Calendar endCalendar = Calendar.getInstance();
        DatePicker endDatePicker = dialog.findViewById(R.id.toDatePicker);

        calculateTimestamps(startCalendar, endCalendar, startDatePicker, endDatePicker, lastSelectedRadioButtonId);
        updateStatisticsUI(lastStartTimestamp, lastEndTimestamp);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("lastSelectedRadioButtonId", lastSelectedRadioButtonId);
        editor.putLong("lastStartTimestamp", lastStartTimestamp);
        editor.putLong("lastEndTimestamp", lastEndTimestamp);
        editor.apply();
        updateStatisticsUI(lastStartTimestamp, lastEndTimestamp);
    }


    private void showFilterDialog(Calendar startCalendar, Calendar endCalendar, DatePicker startDatePicker, DatePicker endDatePicker, Dialog dialog) {




        RadioButton lastSelectedRadioButton = dialog.findViewById(lastSelectedRadioButtonId);
        if (lastSelectedRadioButton != null) {
            lastSelectedRadioButton.setChecked(true);
        }

        if (lastStartTimestamp != -1) {
            startCalendar.setTimeInMillis(lastStartTimestamp);
            startDatePicker.init(startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH), null);
        }

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
                selectedRadioButtonId.set(checkedId);
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


        RadioGroup timeRangeRadioGroup = dialog.findViewById(R.id.radioGroup);
        timeRangeRadioGroup.setOnCheckedChangeListener(radioGroupListener);
        startDatePicker.init(startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH), startDateListener);
        endDatePicker.init(endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH), endDateListener);







        Button deleteButton = dialog.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteData(lastStartTimestamp, lastEndTimestamp);
            }
        });

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

        String timeAboveLimitString = String.format("%02dh: %02dmin: %02ds", hours, minutes, seconds);
        String percentageTimeAboveLimitString = String.format("%.2f", percentageTimeAboveLimit);
        timeAboveLimitTextView.setText("Vrijeme provedeno iznad limita: " + timeAboveLimitString + "(" + percentageTimeAboveLimitString + "% ukupnog vremena)");

        numberOfAggressiveBrakingTextView.setText("Broj naglih kočenja: " + aggressiveBrakingCount);
        numberOfAggressiveBrakingTextView.setTextColor(aggressiveBrakingCount >= 1 ? Color.RED : Color.GREEN);

        numberOfAggressiveAccelerationTextView.setText("Broj agresivnih ubrzavajna: " + aggressiveAccelerationCount);
        numberOfAggressiveAccelerationTextView.setTextColor(aggressiveAccelerationCount >= 1 ? Color.RED : Color.GREEN);
        typeOfDriverTextView.setText("Vi ste: " + totalScore);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        startDate = dateFormat.format(new Date(lastStartTimestamp));
        endDate = dateFormat.format(new Date(lastEndTimestamp));

        if(lastStartTimestamp != 0 && lastEndTimestamp != 0){
            String selectedTimestampsMessage = "Podaci od: " + startDate + " do " + endDate;
            selectedTimestampsTextView.setText(selectedTimestampsMessage);
        }else{
            String selectedTimestampsMessage = "Svi podaci:";
            selectedTimestampsTextView.setText(selectedTimestampsMessage);
        }
    }


    public void calculateTimestamps(Calendar startCalendar, Calendar endCalendar, DatePicker startDatePicker, DatePicker endDatePicker, int selectedRadioButtonId){

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
            lastStartTimestamp = System.currentTimeMillis() - 86400000L; // 86400000 ms = 24 sata
            lastEndTimestamp = System.currentTimeMillis();
        } else if (selectedRadioButtonId == R.id.lastWeekValues) {
            lastSelectedRadioButtonId = selectedRadioButtonId;
            lastStartTimestamp = System.currentTimeMillis() - 604800000L; // 604800000 ms = 7 dana
            lastEndTimestamp = System.currentTimeMillis();
        } else if (selectedRadioButtonId == R.id.lastMonthValues) {
            lastSelectedRadioButtonId = selectedRadioButtonId;
            lastStartTimestamp = System.currentTimeMillis() - 2592000000L; // 2592000000 ms = 30 dana
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
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("lastSelectedRadioButtonId", lastSelectedRadioButtonId);
        editor.putLong("lastStartTimestamp", lastStartTimestamp);
        editor.putLong("lastEndTimestamp", lastEndTimestamp);
        editor.apply();

        updateStatisticsUI(lastStartTimestamp, lastEndTimestamp);

    }


    private void deleteData(long startTimestamp, long endTimestamp) {

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
                dbHelper.removeDataByTimestamp(lastStartTimestamp, lastEndTimestamp);
                updateStatisticsUI(lastStartTimestamp, lastEndTimestamp);
                confirmationDialog.dismiss();
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

