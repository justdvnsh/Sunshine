package com.example.sunshine;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sunshine.data.weatherContract;
import com.example.sunshine.utils.Network;
import com.example.sunshine.utils.json;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import java.net.URL;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements forecastAdapter.ListItemOnClickListener, LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener{

    ProgressBar progressBar;
    TextView errorMessage;
    TextView mLocation;
    RecyclerView recycle;
    forecastAdapter adapter;
    private final static int LOADER = 22;
    protected static LatLng userLocation;
    protected  static Location lastKnownLocation;
    LocationManager locationManager;
    LocationListener locationListener;
    URL urlToSearch;

    private int mPosition = RecyclerView.NO_POSITION;

    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_CONDITION_ID = 3;

    public static final String[] MAIN_FORECAST_PROJECTION = {
            weatherContract.weatherEntry.COLUMN_DATE,
            weatherContract.weatherEntry.COLUMN_MAX_TEMP,
            weatherContract.weatherEntry.COLUMN_MIN_TEMP,
            weatherContract.weatherEntry.COLUMN_WEATHER_ID,
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {

                try {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 10, locationListener);
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable final Bundle args) {
        switch (id) {
            case LOADER:

                Uri forecastQueryUri = weatherContract.weatherEntry.CONTENT_URI;
                String sortOrder = weatherContract.weatherEntry.COLUMN_DATE + "ASC";
                String mSelection = weatherContract.weatherEntry.getSqlSelectForTodayOnwards();

                return new CursorLoader(this, forecastQueryUri, MAIN_FORECAST_PROJECTION, mSelection, null, sortOrder);

            default:
                throw new RuntimeException("Loader not Implemented" + id);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);

        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;

        recycle.smoothScrollToPosition(mPosition);
        if (data.getCount() != 0) showJSONData();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public void showJSONData() {
        recycle.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.INVISIBLE);
    }

    public void showErrorMessage() {
        recycle.setVisibility(View.INVISIBLE);
        errorMessage.setVisibility(View.VISIBLE);
    }

    public String getAddress(LatLng latLng) {

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        String address = "";
        try {

            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if ( addresses != null && addresses.size() > 0 ) {

                Log.i("Address", addresses.get(0).toString());
                address = addresses.get(0).getSubAdminArea() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
                return address;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return address;
    }

    public void getUrlToSearch(final SharedPreferences sharedPreferences) {
        if (sharedPreferences.getBoolean(getResources().getString(R.string.location_key), true)) {

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(final Location location) {
                    Log.i("Location", location.toString());
                    if (sharedPreferences.getBoolean(getResources().getString(R.string.location_key), true)) {
                        userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        String address = getAddress(userLocation);
                        mLocation.setText(address);
                        urlToSearch = Network.buildUriFromCoordinates(userLocation);
                        adapter.setWeatherData(null);
                        getSupportLoaderManager().restartLoader(LOADER, null, MainActivity.this);
                    }
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                try {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 10, locationListener);
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    LatLng latLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    String address  = getAddress(latLng);
                    mLocation.setText(address);
                    urlToSearch = Network.buildUriFromCoordinates(latLng);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            String location = sharedPreferences.getString(getResources().getString(R.string.key_editText), getResources().getString(R.string.default_value_editText));
            urlToSearch = Network.buildUriFromCityName(location);
            mLocation.setText(location);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        errorMessage = (TextView) findViewById(R.id.error_message);
        mLocation = (TextView) findViewById(R.id.location);
        recycle = (RecyclerView) findViewById(R.id.recycle);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recycle.setLayoutManager(layoutManager);
        recycle.setHasFixedSize(true);
        adapter = new forecastAdapter(this, this);
        recycle.setAdapter(adapter);
        adapter.setWeatherData(null);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        getUrlToSearch(sharedPreferences);

        Bundle queryBundle = null;

        getSupportLoaderManager().initLoader(LOADER, queryBundle, this);


    }

    @Override
    public void onItemClickListener(long date) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        Uri uriForDateClicked = weatherContract.weatherEntry.buildWeatherUriWithDate(date);
        intent.setData(uriForDateClicked);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            adapter.setWeatherData(null);
            getSupportLoaderManager().restartLoader(LOADER, null, this);
        }

        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getResources().getString(R.string.location_key))) {
            adapter.setWeatherData(null);
            getUrlToSearch(sharedPreferences);
            getSupportLoaderManager().restartLoader(LOADER, null, this);
        }
        if (s.equals(getResources().getString(R.string.key_editText))) {
            adapter.setWeatherData(null);
            getUrlToSearch(sharedPreferences);
            getSupportLoaderManager().restartLoader(LOADER, null, this);
        }
    }

}
