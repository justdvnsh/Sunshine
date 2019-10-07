package com.example.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    TextView weatherData;
    ProgressBar progressBar;
    TextView errorMessage;
    final static String BASE_URL = "https://api.openweathermap.org/data/2.5/forecast";
    final static String PARAM_QUERY = "q";
    final static String UNITS = "units";
    final static String CNT = "cnt";
    final static String APP_ID = "appid";


    public URL buildUri(String text) {

        Uri builtUri = Uri.parse(BASE_URL).buildUpon().appendQueryParameter(PARAM_QUERY, text)
                .appendQueryParameter(CNT, "7").appendQueryParameter(UNITS, "metric")
                .appendQueryParameter(APP_ID, "7ae534c3dc8e5ffc15f22533a0f91e11").build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return url;
    };


    public String getResponseFromHttpUrl(URL url) throws IOException {

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {

            InputStream inputStream = connection.getInputStream();

            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();

            if ( hasInput ) {
                return scanner.next();
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }

        return null;

    }

    public class LoadWeatherData extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            String result = null;
            try {

                result = getResponseFromHttpUrl(url);

            } catch(Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            progressBar.setVisibility(View.INVISIBLE);
            if ( s != null & !s.equals("") ) {
                showJSONData();
                weatherData.setText(s);
                parseJSON(s);
            } else {
                showErrorMessage();
            }
        }
    }

    public void showWeather() {

        URL url = buildUri("London");
        Toast.makeText(MainActivity.this, url.toString(), Toast.LENGTH_SHORT).show();
        new LoadWeatherData().execute(url);

    }

    public void parseJSON(String result) {

        try {
            JSONObject weather = new JSONObject(result);
            JSONObject coord = weather.getJSONObject("coord");
            weatherData.append("\n");
            weatherData.append(coord.getString("lon"));
            weatherData.append("\n");
            weatherData.append(coord.getString("lat"));
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void showJSONData() {
        weatherData.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.INVISIBLE);
    }

    public void showErrorMessage() {
        weatherData.setVisibility(View.INVISIBLE);
        errorMessage.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        weatherData = (TextView) findViewById(R.id.weather_data);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        errorMessage = (TextView) findViewById(R.id.error_message);

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
            weatherData.setText("");
            showWeather();
        }

        return super.onOptionsItemSelected(item);
    }
}
