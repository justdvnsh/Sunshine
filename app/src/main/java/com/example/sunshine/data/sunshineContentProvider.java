package com.example.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sunshine.utils.SunshineDateUtils;

public class sunshineContentProvider extends ContentProvider {

    private weatherDbHelper mWeatherDbHelper;

    public static final int CODE_WEATHER = 100;
    public static final int CODE_WEATHER_WITH_DATE = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = weatherContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, weatherContract.PATH_WEATHER, CODE_WEATHER);
        uriMatcher.addURI(authority, weatherContract.PATH_WEATHER + "/#", CODE_WEATHER_WITH_DATE);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mWeatherDbHelper = new weatherDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        final SQLiteDatabase db = mWeatherDbHelper.getWritableDatabase();
        Cursor cursor = null;
        int match = sUriMatcher.match(uri);

        switch (match) {
            case CODE_WEATHER:
                cursor = db.query(weatherContract.weatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_WEATHER_WITH_DATE:
                String date = uri.getLastPathSegment();
                String mSelection = " = ? ";
                String[] mSelectionArgs = new String[]{date};
                cursor = db.query(weatherContract.weatherEntry.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri Exception" + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        final SQLiteDatabase db = mWeatherDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        int delete;

        switch (match) {
            case CODE_WEATHER:
                String date = uri.getLastPathSegment();
                String mSelection = " = ? ";
                String[] mSelectionArgs = new String[] {date};
                delete = db.delete(weatherContract.weatherEntry.TABLE_NAME, mSelection, mSelectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unkown Uri Exception" + uri);
        }

        if (delete != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return delete;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {

        final SQLiteDatabase db = mWeatherDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        switch (match) {

            case CODE_WEATHER:
                db.beginTransaction();
                int rowsInserted = 0;

                try {

                    for ( ContentValues value : values ) {
                        long weatherDate = value.getAsLong(weatherContract.weatherEntry.COLUMN_DATE);

                        long _id = db.insert(weatherContract.weatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }

                    }

                    db.setTransactionSuccessful();

                } finally {

                    db.endTransaction();

                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            default:
                return super.bulkInsert(uri, values);

        }

    }
}
