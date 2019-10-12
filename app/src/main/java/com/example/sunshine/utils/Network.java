package com.example.sunshine.utils;

import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Network {

    final static String BASE_URL = "https://api.openweathermap.org/data/2.5/forecast";
    final static String PARAM_QUERY = "q";
    final static String UNITS = "units";
    final static String CNT = "cnt";
    final static String APP_ID = "appid";


    public static URL buildUri(String text) {

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


    public static String getResponseFromHttpUrl(URL url) throws IOException {

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


}
