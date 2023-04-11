package com.example.accelerometer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    //private static final String DATABASE_NAME = "AccelerometerData.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "MyAccelerometerData";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_XVALUE = "xValue";
    private static final String COLUMN_YVALUE = "yValue";
    private static final String COLUMN_ZVALUE = "zValue";

    public MyDatabaseHelper(@Nullable Context context) {
        //super(context, DATABASE_NAME, null, DATABASE_VERSION);
        super(context, String.valueOf(Calendar.getInstance().getTime())+".db", null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
        this.context = context;
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME +
                        " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        COLUMN_XVALUE + " REAL, " +
                        COLUMN_YVALUE + " REAL, " +
                        COLUMN_ZVALUE + " REAL,)";
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

                // Create an AccelerometerData object with the retrieved data
                AccelerometerData data = new AccelerometerData(id, xValue, yValue, zValue, timestamp);

                // Process the retrieved data as needed
                // ...
            } else {
                // Handle the case where a column name is not found in the cursor
                Log.e("TAG", "Column not found in cursor");
            }
        }
        cursor.close();
        db.close();
    }

}


