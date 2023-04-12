package com.example.accelerometer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    //private static final String DATABASE_NAME = "AccelerometerData.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "MyAccelerometerData";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_XVALUE = "xValue";
    private static final String COLUMN_YVALUE = "yValue";
    private static final String COLUMN_ZVALUE = "zValue";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    public MyDatabaseHelper(@Nullable Context context) {
        //super(context, DATABASE_NAME, null, DATABASE_VERSION);
        super(context, String.valueOf(Calendar.getInstance().getTime())+".db", null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
        this.context = context;
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_XVALUE + " REAL, " +
                COLUMN_YVALUE + " REAL, " +
                COLUMN_ZVALUE + " REAL, " +
                COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP)"; // Add TIMESTAMP column as INTEGER
        db.execSQL(query);
        Log.d("TAG database :", "DATABASE CREATED");
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

    public void fetchAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        while (cursor.moveToNext()) {
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            int xValueIndex = cursor.getColumnIndex(COLUMN_XVALUE);
            int yValueIndex = cursor.getColumnIndex(COLUMN_YVALUE);
            int zValueIndex = cursor.getColumnIndex(COLUMN_ZVALUE);
            int timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);

            if (idIndex != -1 && xValueIndex != -1 && yValueIndex != -1 && zValueIndex != -1 && timestampIndex != -1) {
                int id = cursor.getInt(idIndex);
                double xValue = cursor.getDouble(xValueIndex);
                double yValue = cursor.getDouble(yValueIndex);
                double zValue = cursor.getDouble(zValueIndex);
                long timestamp = cursor.getLong(timestampIndex);

                // Process the retrieved data using the id, xValue, yValue, zValue, and timestamp values
                // ...
                Log.d("TAG", "Retrieved data - id: " + id + ", xValue: " + xValue + ", yValue: " + yValue + ", zValue: " + zValue + ", timestamp: " + timestamp);
            } else {
                // Handle the case where a column name is not found in the cursor
                Log.e("TAG", "Column not found in cursor");
            }
        }
        cursor.close();
        db.close();
    }

    public double fetchHighestAccelerationForCurrentDay() {
        double highestAcceleration = 0.0;

        // Get the current date and time
        Date currentDate = new Date();

        // Convert the date to the required format for the query
        SimpleDateFormat sdfFormatted = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = sdfFormatted.format(currentDate);

        // Query to fetch the highest acceleration value for one day
        String query = "SELECT MAX(acceleration_x) AS max_acceleration_x, " +
                "MAX(" + COLUMN_XVALUE + ") AS max_acceleration_y, " +
                "MAX(acceleration_z) AS max_acceleration_z " +
                "FROM " + TABLE_NAME  +
                " WHERE date(timestamp/1000, 'unixepoch', 'localtime') = ?";

        // Execute the query and fetch the result
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{formattedDate});
        if (cursor != null && cursor.moveToFirst()) {
            int maxAccelerationXIndex = cursor.getColumnIndex("max_acceleration_x");
            int maxAccelerationYIndex = cursor.getColumnIndex("max_acceleration_y");
            int maxAccelerationZIndex = cursor.getColumnIndex("max_acceleration_z");
            if (maxAccelerationXIndex >= 0 && maxAccelerationYIndex >= 0 && maxAccelerationZIndex >= 0) {
                highestAcceleration = Math.max(Math.max(
                        cursor.getDouble(maxAccelerationXIndex),
                        cursor.getDouble(maxAccelerationYIndex)
                ), cursor.getDouble(maxAccelerationZIndex));
            }
            cursor.close();
        }
        db.close();

        return highestAcceleration;
    }


}


