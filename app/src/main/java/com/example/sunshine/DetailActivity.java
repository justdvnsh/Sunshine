package com.example.sunshine;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.app.ShareCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    TextView mMainText;
    Intent intent;

    public final static String SHARE_STRING = "#SUNSHINE";

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

        mMainText = (TextView) findViewById(R.id.main);
        intent = getIntent();
        if ( intent.resolveActivity(getPackageManager()) != null ) {
            mMainText.setText(intent.getStringExtra("WeatherData"));
        }

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

        return super.onOptionsItemSelected(item);
    }
}
