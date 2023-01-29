package com.example.accelerometer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

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
                        COLUMN_ZVALUE + " REAL)";
        db.execSQL(query);
        Log.d("TAG database :", "DATABASE CREATED");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }
}


