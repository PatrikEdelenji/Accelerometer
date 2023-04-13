package com.example.accelerometer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AccelerationDataDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "accelerationData.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "acceleration_data";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TOTAL_ACCELERATION = "acceleration";
    public static final String COLUMN_X_ACCELERATION = "x";
    public static final String COLUMN_Y_ACCELERATION = "y";
    public static final String COLUMN_Z_ACCELERATION = "z";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    public AccelerationDataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE acceleration_data (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " acceleration REAL," +
                " x REAL," +
                " y REAL," +
                " z REAL," +
                " timestamp INTEGER)";
        db.execSQL(CREATE_TABLE);
    }


    /*
    onUpgrade() is called (you do not call it yourself) when version of your DB changed which means underlying table structure changed etc.

    In general it means that OS is telling you "hey, you asked for database structure version 10 but
    I found we got something older here, so this is you chance to fix that before you start using database (and potentially crash due to structure mismatch)".

    In that method you should do all that is necessary to, well.. upgrade structure of your old database to structure matching current version requirements
    like adding/dropping columns, converting row contents or even dropping old db completely and create it from scratch
    For Android it does not matter what you do here - it's just a sort of emergency callback for your code to do the necessary job (if any).
    You need to be aware that users may not update frequently so you have to always handle upgrade from version X to Y knowing that X may not be equal to i.e. (Y-1).
    https://stackoverflow.com/questions/13159210/onupgrade-sqlite-database-in-android

    TL;DR IT GETS CALLED WHENEVER YOU CHANGE DB VERSION
    */


    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }

    public void addAccelerationData(float acceleration, float x, float y, float z, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TOTAL_ACCELERATION, acceleration);
        values.put(COLUMN_X_ACCELERATION, x);
        values.put(COLUMN_Y_ACCELERATION, y);
        values.put(COLUMN_Z_ACCELERATION, z);
        values.put(COLUMN_TIMESTAMP, timestamp);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

/*
    public List<AccelerationDataModel> fetchAllData() {
        List<AccelerationDataModel> dataList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        while (cursor.moveToNext()) {
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            int totalValueIndex = cursor.getColumnIndex(COLUMN_TOTAL_ACCELERATION);
            int xValueIndex = cursor.getColumnIndex(COLUMN_X_ACCELERATION);
            int yValueIndex = cursor.getColumnIndex(COLUMN_Y_ACCELERATION);
            int zValueIndex = cursor.getColumnIndex(COLUMN_Z_ACCELERATION);
            int timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);

            if (idIndex != -1 && xValueIndex != -1 && yValueIndex != -1 && zValueIndex != -1 && timestampIndex != -1) {
                int id = cursor.getInt(idIndex);
                double totalValue = cursor.getDouble(totalValueIndex);
                double xValue = cursor.getDouble(xValueIndex);
                double yValue = cursor.getDouble(yValueIndex);
                double zValue = cursor.getDouble(zValueIndex);
                long timestamp = cursor.getLong(timestampIndex);

                // Create a MyDataObject instance with retrieved data
                AccelerationDataModel dataObject = new AccelerationDataModel(id, totalValue, xValue, yValue, zValue, timestamp);

                // Add the data object to the list
                dataList.add(dataObject);
            } else {
                // Handle the case where a column name is not found in the cursor
                Log.e("TAG", "Column not found in cursor");
            }
        }
        cursor.close();
        db.close();
        return dataList;
    }
    */

    public double fetchHighestAccelerationForCurrentDay() {
        double highestAcceleration = 0.0;

        // Get the current date and time
        Date currentDate = new Date();

        // Convert the date to the required format for the query
        SimpleDateFormat sdfFormatted = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = sdfFormatted.format(currentDate);

        // Query to fetch the highest acceleration value for one day
        String query = "SELECT MAX(acceleration) AS max_acceleration FROM " + TABLE_NAME  +
                " WHERE date(timestamp/1000, 'unixepoch', 'localtime') = ?";

        // Execute the query and fetch the result
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{formattedDate});
        if (cursor != null && cursor.moveToFirst()) {
            int maxAccelerationIndex = cursor.getColumnIndex("max_acceleration");
            if (maxAccelerationIndex >= 0 ) {
                highestAcceleration = cursor.getDouble(maxAccelerationIndex);
            }
            cursor.close();
        }
        db.close();

        return highestAcceleration;
    }

    public double calculateAverageTotalAcceleration() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT AVG(" + COLUMN_TOTAL_ACCELERATION + ") FROM " + TABLE_NAME, null);
        double average = 0;
        if (cursor.moveToFirst()) {
            average = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return average;
    }

    public double calculateTimeSpentAboveLimit() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(CASE WHEN acceleration > 3.5 THEN timestamp - prev_timestamp ELSE 0 END) AS time_above_threshold" +
                " FROM ( SELECT a1.timestamp, a1.acceleration, COALESCE(MAX(a2.timestamp), 0) AS prev_timestamp FROM acceleration_data a1" +
                " LEFT JOIN acceleration_data a2 ON a1.timestamp > a2.timestamp" +
                " GROUP BY a1.timestamp, a1.acceleration" +
                " ) subquery", null);

        // Process the cursor and retrieve the calculated time above threshold
        double timeAboveThreshold = 0.0;
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex("time_above_threshold");
            if (columnIndex >= 0) {
                timeAboveThreshold = cursor.getDouble(columnIndex);
            }
        }

        cursor.close();
        db.close();

        return timeAboveThreshold;
    }

    public String formatTimeSpentAboveLimit(double timeInMillis) {
        long seconds = (long) (timeInMillis / 1000);
        long minutes = seconds / 60;
        long hours = minutes / 60;

        // Calculate remaining minutes and seconds
        minutes %= 60;
        seconds %= 60;

        // Format the time as "HH:mm:ss" and add labels
        String formattedTime = String.format("%d hours, %d minutes, %d seconds", hours, minutes, seconds);

        return formattedTime;
    }



    public int countAccelerationBreaches() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) AS breach_count" +
                " FROM (" +
                "   SELECT acceleration, LAG(acceleration) OVER (ORDER BY timestamp) AS prev_acceleration" +
                "   FROM acceleration_data" +
                " )" +
                " WHERE acceleration > 3.5 AND prev_acceleration <= 3.5", null);

        // Process the cursor and retrieve the breach count
        int breachCount = 0;
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex("breach_count");
            if (columnIndex >= 0) {
                breachCount = cursor.getInt(columnIndex);
            }
        }

        cursor.close();
        db.close();

        return breachCount;
    }


    public double calculateBiggestAccelerationDifference() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(ABS(acceleration - prev_acceleration)) AS max_difference" +
                " FROM ( SELECT acceleration, LAG(acceleration) OVER (ORDER BY timestamp) AS prev_acceleration FROM acceleration_data" +
                " ) subquery", null);

        // Process the cursor and retrieve the maximum difference
        double maxDifference = 0.0;
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex("max_difference");
            if (columnIndex >= 0) {
                maxDifference = cursor.getDouble(columnIndex);
            }
        }

        cursor.close();
        db.close();

        return maxDifference;
    }

}