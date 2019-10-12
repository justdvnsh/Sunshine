package com.example.sunshine;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sunshine.utils.Network;
import com.example.sunshine.utils.json;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements forecastAdapter.ListItemOnClickListener {

    ProgressBar progressBar;
    TextView errorMessage;
    RecyclerView recycle;
    forecastAdapter adapter;

    public class LoadWeatherData extends AsyncTask<URL, Void, String[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[] doInBackground(URL... urls) {
            URL url = urls[0];

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

        @Override
        protected void onPostExecute(String[] data) {
            progressBar.setVisibility(View.INVISIBLE);
            if ( data != null) {
                showJSONData();
                adapter.setWeatherData(data);
            } else {
                showErrorMessage();
            }
        }
    }

    public void showWeather() {

        URL url = Network.buildUri("London");
        Toast.makeText(MainActivity.this, url.toString(), Toast.LENGTH_SHORT).show();
        new LoadWeatherData().execute(url);

    }



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
        showWeather();

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
            showWeather();
        }

        return super.onOptionsItemSelected(item);
    }
}
