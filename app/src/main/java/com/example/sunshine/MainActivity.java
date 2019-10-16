package com.example.sunshine;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.sunshine.utils.Network;
import com.example.sunshine.utils.json;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements forecastAdapter.ListItemOnClickListener, LoaderManager.LoaderCallbacks<String[]> {

    ProgressBar progressBar;
    TextView errorMessage;
    RecyclerView recycle;
    forecastAdapter adapter;
    private final static int LOADER = 22;

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
                URL url = Network.buildUri("London");

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


//    public void showWeather() {
//
//        URL url = Network.buildUri("London");
//        Toast.makeText(MainActivity.this, url.toString(), Toast.LENGTH_SHORT).show();
//
//
//    }



    public void showJSONData() {
        recycle.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.INVISIBLE);
    }

    public void showErrorMessage() {
        recycle.setVisibility(View.INVISIBLE);
        errorMessage.setVisibility(View.VISIBLE);
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

        return super.onOptionsItemSelected(item);
    }
}
