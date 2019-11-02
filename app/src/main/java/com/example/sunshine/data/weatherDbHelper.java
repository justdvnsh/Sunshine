package com.example.sunshine.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class weatherDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "weather.db";
    private static final int DATABASE_VERSION = 1;

    public weatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " +
                weatherContract.weatherEntry.TABLE_NAME + " (" +
                weatherContract.weatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                weatherContract.weatherEntry.COLUMN_DATE + " INTEGER NOT NULL," +
                weatherContract.weatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL," +
                weatherContract.weatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL," +
                weatherContract.weatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL," +
                weatherContract.weatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL," +
                weatherContract.weatherEntry.COLUMN_PRESSURE + " REAL NOT NULL," +
                weatherContract.weatherEntry.COLUMN_WIND_SPEED + "REAL NOT NULL," +
                weatherContract.weatherEntry.COLUMN_DEGREES + " REAL NOT NULL," + "UNIQUE (" + weatherContract.weatherEntry.COLUMN_DATE + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + weatherContract.weatherEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
