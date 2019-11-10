package com.example.sunshine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.app.ShareCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.sunshine.data.weatherContract;
import com.example.sunshine.utils.SunshineDateUtils;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTemperatureView;
    private TextView mLowTemperatureView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;
    Intent intent;

    private String mForecastSummary;

    public final static String SHARE_STRING = "#SUNSHINE";

    public static final String[] WEATHER_DETAIL_PROJECTION = {
            weatherContract.weatherEntry.COLUMN_DATE,
            weatherContract.weatherEntry.COLUMN_MAX_TEMP,
            weatherContract.weatherEntry.COLUMN_MIN_TEMP,
            weatherContract.weatherEntry.COLUMN_HUMIDITY,
            weatherContract.weatherEntry.COLUMN_PRESSURE,
            weatherContract.weatherEntry.COLUMN_WIND_SPEED,
            weatherContract.weatherEntry.COLUMN_DEGREES,
            weatherContract.weatherEntry.COLUMN_WEATHER_ID
    };

    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_HUMIDITY = 3;
    public static final int INDEX_WEATHER_PRESSURE = 4;
    public static final int INDEX_WEATHER_WIND_SPEED = 5;
    public static final int INDEX_WEATHER_DEGREES = 6;
    public static final int INDEX_WEATHER_CONDITION_ID = 7;


    private static final int DETAIL_LOADER = 33;

    private Uri mUri;

    public void shareContent() {

        String mime = "text/plain";
        String text = intent.getStringExtra("WeatherData") + "\n" + SHARE_STRING;
        String title = "Weather Data";

        Intent ShareIntent = ShareCompat.IntentBuilder.from(this).setChooserTitle(title).setType(mime).setText(text).getIntent();

        if ( ShareIntent.resolveActivity(getPackageManager()) != null ) {
            startActivity(ShareIntent);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mDateView = (TextView) findViewById(R.id.date);
        mDescriptionView = (TextView) findViewById(R.id.weather_description);
        mHighTemperatureView = (TextView) findViewById(R.id.high_temperature);
        mLowTemperatureView = (TextView) findViewById(R.id.low_temperature);
        mHumidityView = (TextView) findViewById(R.id.humidity);
        mWindView = (TextView) findViewById(R.id.wind);
        mPressureView = (TextView) findViewById(R.id.pressure);

//      COMPLETED (14) Remove the code that checks for extra text

//      COMPLETED (16) Use getData to get a reference to the URI passed with this Activity's Intent
        mUri = getIntent().getData();
//      COMPLETED (17) Throw a NullPointerException if that URI is null
        if (mUri == null) throw new NullPointerException("URI for DetailActivity cannot be null");

//      COMPLETED (35) Initialize the loader for DetailActivity
        /* This connects our Activity into the loader lifecycle. */
        getSupportLoaderManager().initLoader(DETAIL_LOADER, null, this);

        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            shareContent();
        }

        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }

        if (id == R.id.action_settings) {
            Intent intent = new Intent(DetailActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {

//          COMPLETED (23) If the loader requested is our detail loader, return the appropriate CursorLoader
            case DETAIL_LOADER:

                return new CursorLoader(this,
                        mUri,
                        WEATHER_DETAIL_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }

        long localDateMidnightGmt = data.getLong(INDEX_WEATHER_DATE);
        String dateText = SunshineDateUtils.getFriendlyDateString(this, localDateMidnightGmt, true);

        mDateView.setText(dateText);

//      COMPLETED (27) Display the weather description (using SunshineWeatherUtils)
        /***********************
         * Weather Description *
         ***********************/
        /* Read weather condition ID from the cursor (ID provided by Open Weather Map) */
        int weatherId = data.getInt(INDEX_WEATHER_CONDITION_ID);
        /* Use the weatherId to obtain the proper description */
        String description = com.example.android.sunshine.utilities.SunshineWeatherUtils.getStringForWeatherCondition(this, weatherId);

        /* Set the text */
        mDescriptionView.setText(description);

//      COMPLETED (28) Display the high temperature
        /**************************
         * High (max) temperature *
         **************************/
        /* Read high temperature from the cursor (in degrees celsius) */
        double highInCelsius = data.getDouble(INDEX_WEATHER_MAX_TEMP);
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        String highString = com.example.android.sunshine.utilities.SunshineWeatherUtils.formatTemperature(this, highInCelsius);

        /* Set the text */
        mHighTemperatureView.setText(highString);

//      COMPLETED (29) Display the low temperature
        /*************************
         * Low (min) temperature *
         *************************/
        /* Read low temperature from the cursor (in degrees celsius) */
        double lowInCelsius = data.getDouble(INDEX_WEATHER_MIN_TEMP);
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        String lowString = com.example.android.sunshine.utilities.SunshineWeatherUtils.formatTemperature(this, lowInCelsius);

        /* Set the text */
        mLowTemperatureView.setText(lowString);

//      COMPLETED (30) Display the humidity
        /************
         * Humidity *
         ************/
        /* Read humidity from the cursor */
        float humidity = data.getFloat(INDEX_WEATHER_HUMIDITY);
        String humidityString = getString(R.string.format_humidity, humidity);

        /* Set the text */
        mHumidityView.setText(humidityString);

//      COMPLETED (31) Display the wind speed and direction
        /****************************
         * Wind speed and direction *
         ****************************/
        /* Read wind speed (in MPH) and direction (in compass degrees) from the cursor  */
        float windSpeed = data.getFloat(INDEX_WEATHER_WIND_SPEED);
        float windDirection = data.getFloat(INDEX_WEATHER_DEGREES);
        String windString = com.example.android.sunshine.utilities.SunshineWeatherUtils.getFormattedWind(this, windSpeed, windDirection);

        /* Set the text */
        mWindView.setText(windString);

//      COMPLETED (32) Display the pressure
        /************
         * Pressure *
         ************/
        /* Read pressure from the cursor */
        float pressure = data.getFloat(INDEX_WEATHER_PRESSURE);

        /*
         * Format the pressure text using string resources. The reason we directly access
         * resources using getString rather than using a method from SunshineWeatherUtils as
         * we have for other data displayed in this Activity is because there is no
         * additional logic that needs to be considered in order to properly display the
         * pressure.
         */
        String pressureString = getString(R.string.format_pressure, pressure);

        /* Set the text */
        mPressureView.setText(pressureString);

//      COMPLETED (33) Store a forecast summary in mForecastSummary
        /* Store the forecast summary String in our forecast summary field to share later */
        mForecastSummary = String.format("%s - %s - %s/%s",
                dateText, description, highString, lowString);

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
