package com.example.sunshine;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.sunshine.utils.Network;
import com.example.sunshine.utils.json;
import com.google.android.gms.maps.model.LatLng;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements forecastAdapter.ListItemOnClickListener, LoaderManager.LoaderCallbacks<String[]> {

    ProgressBar progressBar;
    TextView errorMessage;
    RecyclerView recycle;
    forecastAdapter adapter;
    private final static int LOADER = 22;
    protected static LatLng userLocation;
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5*60*1000, 100, locationListener);

            }
        }
    }

    public void getLocation() {

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 60 * 1000, 10, locationListener);
        }

    }

    @NonNull
    @Override
    public Loader<String[]> onCreateLoader(int id, @Nullable final Bundle args) {
        return new AsyncTaskLoader<String[]>(this) {

            String weatherJSON[] = null;

            @Override
            protected void onStartLoading() {

                if (weatherJSON != null) {
                    deliverResult(weatherJSON);
                } else {

                    progressBar.setVisibility(View.VISIBLE);
                    forceLoad();

                }

            }

            @Nullable
            @Override
            public String[] loadInBackground() {
                URL url = Network.buildUriFromCityName("london");

                try {

                    String result = Network.getResponseFromHttpUrl(url);

                    String[] simpleJsonWeatherData = json.parseJson(
                            MainActivity.this, result);

                    return simpleJsonWeatherData;

                } catch(Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(String[] data) {
                weatherJSON = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String[]> loader, String[] data) {
        progressBar.setVisibility(View.INVISIBLE);
        adapter.setWeatherData(data);
        if ( data != null) {
            showJSONData();
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String[]> loader) {

    }

    public void showJSONData() {
        recycle.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.INVISIBLE);
    }

    public void showErrorMessage() {
        recycle.setVisibility(View.INVISIBLE);
        errorMessage.setVisibility(View.VISIBLE);
    }

    public void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        errorMessage = (TextView) findViewById(R.id.error_message);
        recycle = (RecyclerView) findViewById(R.id.recycle);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recycle.setLayoutManager(layoutManager);
        recycle.setHasFixedSize(true);
        adapter = new forecastAdapter(this);
        recycle.setAdapter(adapter);
        adapter.setWeatherData(null);

        Bundle queryBundle = null;

        getSupportLoaderManager().initLoader(LOADER, queryBundle, this);

        setupSharedPreferences();

    }

    @Override
    public void onItemClickListener(String weather) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("WeatherData", weather);
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
}
