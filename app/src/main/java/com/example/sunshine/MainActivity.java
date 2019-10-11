package com.example.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    TextView weatherData;
    ProgressBar progressBar;
    TextView errorMessage;
    ListView listView;
    ArrayList<String> listAdapter = new ArrayList<String>();
    final static String BASE_URL = "https://api.openweathermap.org/data/2.5/forecast";
    final static String PARAM_QUERY = "q";
    final static String UNITS = "units";
    final static String CNT = "cnt";
    final static String APP_ID = "appid";


    public URL buildUri(String text) {

        Uri builtUri = Uri.parse(BASE_URL).buildUpon().appendQueryParameter(PARAM_QUERY, text)
                .appendQueryParameter(CNT, "34").appendQueryParameter(UNITS, "metric")
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
//                weatherData.setText(s);
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

        ArrayList<String> array = new ArrayList<String>();

        try {

            JSONObject weather = new JSONObject(result);
            JSONArray mainArray = weather.getJSONArray("list");
            if ( mainArray != null ) {
                for (int i = 0; i < mainArray.length(); i++) {
                    array.add(mainArray.getString(i));
                }
            }

            for ( int i = 0; i < array.size(); i++) {

                JSONObject details = new JSONObject(array.get(i));
                // Main Object
                JSONObject main = details.getJSONObject("main");
                String temp = main.getString("temp");

                // Date
                String dateAndTime = details.getString("dt_txt");

                String date = "";
                if ( dateAndTime.contains("21:00:00")) {
                    date = dateAndTime.substring(0,10);

                    listAdapter.add("Date -> " + date + "\t" + "Temp -> " + temp);

                }


                // Weather Details
                //Wind and Cloud Details

            }

            populateUI();

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void populateUI() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, listAdapter);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(MainActivity.this, "You clicked", Toast.LENGTH_SHORT).show();
            }
        });
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
        listView = (ListView)findViewById(R.id.list);

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
