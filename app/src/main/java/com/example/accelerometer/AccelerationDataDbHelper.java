package com.example.accelerometer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AccelerationDataDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "accelerationData.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "acceleration_data";
    public static final String COLUMN_TOTAL_ACCELERATION = "acceleration";
    public static final String COLUMN_X_ACCELERATION = "x";
    public static final String COLUMN_Y_ACCELERATION = "y";
    public static final String COLUMN_Z_ACCELERATION = "z";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static long startTimestamp = 0;
    public static long endTimestamp = 0;

    public AccelerationDataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS acceleration_data (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " acceleration REAL," +
                " x REAL," +
                " y REAL," +
                " z REAL," +
                " timestamp INTEGER)";
        db.execSQL(CREATE_TABLE);
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if(oldVersion == newVersion){
            return;
        }
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



    public double getHighestAcceleration() {

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT MAX(acceleration) FROM " + TABLE_NAME + " WHERE timestamp BETWEEN ? AND ?";
        String[] args = { String.valueOf(startTimestamp), String.valueOf(endTimestamp) };
        Cursor cursor = db.rawQuery(query, args);
        double highestAcceleration = 0.0;
        if (cursor != null && cursor.moveToFirst()) {
            highestAcceleration = cursor.getDouble(0);
            cursor.close();
        }
        db.close();
        return highestAcceleration;
    }


    public double getAverageAcceleration() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT AVG(acceleration) FROM " + TABLE_NAME + " WHERE timestamp BETWEEN ? AND ?";
        String[] args = { String.valueOf(startTimestamp), String.valueOf(endTimestamp) };
        Cursor cursor = db.rawQuery(query, args);
        double averageAcceleration = 0.0;
        if (cursor != null && cursor.moveToFirst()) {
            averageAcceleration = cursor.getDouble(0);
            cursor.close();
        }
        db.close();
        return averageAcceleration;
    }


    public double getTimeSpentAboveLimit() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM((timestamp - prev_timestamp) / 1000.0) " +
                "FROM (SELECT timestamp, LAG(timestamp) OVER (ORDER BY timestamp) AS prev_timestamp " +
                "FROM " + TABLE_NAME + " WHERE timestamp BETWEEN ? AND ? AND acceleration > 2.98) " +
                "WHERE prev_timestamp IS NOT NULL";
        String[] args = { String.valueOf(startTimestamp), String.valueOf(endTimestamp) };
        Cursor cursor = db.rawQuery(query, args);
        double timeSpentAboveLimit = 0.0;
        if (cursor != null && cursor.moveToFirst()) {
            timeSpentAboveLimit = cursor.getDouble(0);
            cursor.close();
        }
        db.close();
        return timeSpentAboveLimit;
    }



    public double getPercentageAboveThreshold() {
        double timeAboveThreshold = getTimeSpentAboveLimit();
        double totalTime = (endTimestamp - startTimestamp) / 1000.0;
        return (timeAboveThreshold / totalTime) * 100.0;
    }


    public int getAggressiveAccelerationCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT acceleration FROM " + TABLE_NAME + " WHERE timestamp BETWEEN ? AND ? AND acceleration";
        String[] args = { String.valueOf(startTimestamp), String.valueOf(endTimestamp) };
        Cursor cursor = db.rawQuery(query, args);

        int aggressiveAccelerationCount = 0;
        boolean wasAboveThreshold = false;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                double acceleration = cursor.getDouble(0);
                if (!wasAboveThreshold && acceleration >= 2.98) {
                    aggressiveAccelerationCount++;
                    wasAboveThreshold = true;
                } else if (wasAboveThreshold && acceleration < 2.98) {
                    wasAboveThreshold = false;
                }
            } while (cursor.moveToNext());

            cursor.close();
        }
        db.close();
        return aggressiveAccelerationCount;
    }


    public int getAggressiveBrakingCount() {

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT acceleration FROM " + TABLE_NAME + " WHERE timestamp BETWEEN ? AND ?";
        String[] args = { String.valueOf(startTimestamp), String.valueOf(endTimestamp) };
        Cursor cursor = db.rawQuery(query, args);

        int aggressiveBrakingCount = 0;
        boolean wasBelowThreshold = false;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                double acceleration = cursor.getDouble(0);
                if (!wasBelowThreshold && acceleration < -2.98) {
                    aggressiveBrakingCount++;
                    wasBelowThreshold = true;
                } else if (wasBelowThreshold && acceleration >= -2.98) {
                    wasBelowThreshold = false;
                }
            } while (cursor.moveToNext());

            cursor.close();
        }
        db.close();
        return aggressiveBrakingCount;
    }



    public int getAggressiveLeftTurnCount(long startTimestamp, long endTimestamp) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to get all x, y, and z acceleration values within the time range
        String query = "SELECT acceleration, x, y, z FROM " + TABLE_NAME + " WHERE timestamp BETWEEN ? AND ?";

        // Execute the query
        String[] args = { String.valueOf(startTimestamp), String.valueOf(endTimestamp) };
        Cursor cursor = db.rawQuery(query, args);

        // Count the number of times a sharp left turn is detected
        int aggressiveLeftTurnCount = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                double totalAcceleration = cursor.getDouble(0);
                double xAcceleration = cursor.getDouble(1);
                double yAcceleration = cursor.getDouble(2);


                // Set a threshold for detecting a sharp turn
                double threshold = 8.0;

                // Check if the magnitude exceeds the threshold
                if (totalAcceleration > threshold) {
                    // Calculate the dot product of the acceleration vector and the device's orientation vector
                    double dotProduct = xAcceleration * Math.cos(Math.toRadians(90)) + yAcceleration * Math.sin(Math.toRadians(90));

                    // Determine if the turn is a sharp left turn based on the sign of the dot product
                    if (dotProduct < 0) {
                        aggressiveLeftTurnCount++;
                    }
                }
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return aggressiveLeftTurnCount;
    }

    public int getAggressiveRightTurnCount() {
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to get all x, y, and z acceleration values within the time range
        String query = "SELECT acceleration, x, y, z FROM " + TABLE_NAME + " WHERE timestamp BETWEEN ? AND ?";

        // Execute the query
        String[] args = { String.valueOf(startTimestamp), String.valueOf(endTimestamp) };
        Cursor cursor = db.rawQuery(query, args);

        // Count the number of times a sharp right turn is detected
        int aggressiveRightTurnCount = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                double totalAcceleration = cursor.getDouble(0);
                double xAcceleration = cursor.getDouble(1);
                double yAcceleration = cursor.getDouble(2);


                // Set a threshold for detecting a sharp turn
                double threshold = 8.0;

                // Check if the magnitude exceeds the threshold
                if (totalAcceleration > threshold) {
                    // Calculate the dot product of the acceleration vector and the device's orientation vector
                    double dotProduct = xAcceleration * Math.cos(Math.toRadians(90)) + yAcceleration * Math.sin(Math.toRadians(90));

                    // Determine if the turn is a sharp right turn based on the sign of the dot product
                    if (dotProduct > 0) {
                        aggressiveRightTurnCount++;
                    }
                }
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return aggressiveRightTurnCount;
    }

    public void removeDataByTimestamp() {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_TIMESTAMP + " BETWEEN ? AND ?";
        String[] whereArgs = { String.valueOf(startTimestamp), String.valueOf(endTimestamp) };
        db.delete(TABLE_NAME, whereClause, whereArgs);
        db.close();
    }

    public void getTimestamps(int selection) {
        long[] timestamps = new long[2];

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        switch (selection) {
            case 1:

                timestamps[0] = calendar.getTimeInMillis();
                timestamps[1] = System.currentTimeMillis();
                break;

            case 2:
                timestamps[0] = System.currentTimeMillis() - 86400000L; // 86400000 ms = 24 hours
                timestamps[1] = System.currentTimeMillis();
                break;

            case 3:
                timestamps[0] = System.currentTimeMillis() - 604800000L; // 604800000 ms = 7 days
                timestamps[1] = System.currentTimeMillis();
                break;

            case 4:
                timestamps[0] = System.currentTimeMillis() - 2592000000L; // 2592000000 ms = 30 days
                timestamps[1] = System.currentTimeMillis();
                break;
            case 5:
                timestamps[0] = 0;
                timestamps[1] = System.currentTimeMillis();
                break;

        }

        this.startTimestamp = timestamps[0];
        this.endTimestamp = timestamps[1];
    }

    public void getTimestamps(long startTimestamp, long endTimestamp) {
        long[] timestamps = new long[2];
        timestamps[0] = startTimestamp;
        timestamps[1] = endTimestamp;
        this.startTimestamp = timestamps[0];
        this.endTimestamp = timestamps[1];
    }

    public long[] getCurrentTimestamps() {
        long[] timestamps = new long[2];
        timestamps[0] = startTimestamp;
        timestamps[1] = endTimestamp;
        return timestamps;
    }

}
