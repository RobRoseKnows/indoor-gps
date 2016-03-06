package io.robrose.hack.indoorgps.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.robrose.hack.indoorgps.data.TrainingContract.TrainingEntry;

/**
 * Created by Robert on 3/5/2016.
 * Borrowed from: https://github.com/udacity/Sunshine-Version-2/blob/4.23_fix_settings/app/src/main/java/com/example/android/sunshine/app/data/WeatherDbHelper.java
 */
public class TrainingDbHelper extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 1;

        static final String DATABASE_NAME = "training.db";

        public TrainingDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            final String SQL_CREATE_SIGNAL_TABLE = "CREATE TABLE " + TrainingEntry.TABLE_NAME + " (" +
                    TrainingEntry._ID + " INTEGER PRIMARY KEY," +
                    TrainingEntry.COLUMN_LOCATION + " TEXT NOT NULL, " +
                    TrainingEntry.COLUMN_MAC + " TEXT NOT NULL, " +
                    TrainingEntry.COLUMN_AVG_STRENGTH + " REAL NOT NULL, " +
                    TrainingEntry.COLUMN_STD_DEV + " REAL NOT NULL, " +
                    TrainingEntry.COLUMN_SAMPLE + " INTEGER NOT NULL, UNIQUE( " +
                    TrainingEntry.COLUMN_LOCATION + ", " +
                    TrainingEntry.COLUMN_MAC + ") ON CONFLICT FAIL" +
                    " );";

            sqLiteDatabase.execSQL(SQL_CREATE_SIGNAL_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrainingEntry.TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
}
