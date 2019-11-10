package com.example.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.sunshine.data.weatherContract;
import com.example.sunshine.utils.SunshineDateUtils;

import org.w3c.dom.Text;

public class forecastAdapter extends RecyclerView.Adapter<forecastAdapter.forecastViewHolder> {

    private Cursor mCursor;

    final private ListItemOnClickListener mOnClickListener;

    public Context mContext;

    public interface ListItemOnClickListener {
        void onItemClickListener(long date);
    }

    public forecastAdapter(Context context, ListItemOnClickListener onClickListener) {
        mContext = context;
        mOnClickListener = onClickListener;
    }

    @Override
    public forecastViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.forecast_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new forecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(forecastViewHolder forecastAdapterViewHolder, int position) {

        mCursor.moveToPosition(position);


        /*******************
         * Weather Summary *
         *******************/
//      COMPLETED (7) Generate a weather summary with the date, description, high and low
        /* Read date from the cursor */
        long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
        /* Get human readable string using our utility method */
        String dateString = SunshineDateUtils.getFriendlyDateString(mContext, dateInMillis, false);
        /* Use the weatherId to obtain the proper description */
        int weatherId = mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
        String description = com.example.android.sunshine.utilities.SunshineWeatherUtils.getStringForWeatherCondition(mContext, weatherId);
        /* Read high temperature from the cursor (in degrees celsius) */
        double highInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP);
        /* Read low temperature from the cursor (in degrees celsius) */
        double lowInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP);

        String highAndLowTemperature =
                com.example.android.sunshine.utilities.SunshineWeatherUtils.formatHighLows(mContext, highInCelsius, lowInCelsius);

        String weatherSummary = dateString + " - " + description + " - " + highAndLowTemperature;

        forecastAdapterViewHolder.weatherDataTextView.setText(weatherSummary);

    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        // After the new Cursor is set, call notifyDataSetChanged
        notifyDataSetChanged();
    }


    public class forecastViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView weatherDataTextView;

        public forecastViewHolder(View view) {
            super(view);
            weatherDataTextView = (TextView) view.findViewById(R.id.tv_weather_data);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mCursor.moveToPosition(clickedPosition);
            long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
            mOnClickListener.onItemClickListener(dateInMillis);
        }

    }
}
