package com.example.sunshine;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

public class forecastAdapter extends RecyclerView.Adapter<forecastAdapter.forecastViewHolder> {

    private String[] weatherData;

    final private ListItemOnClickListener mOnClickListener;

    public interface ListItemOnClickListener {
        void onItemClickListener(String weather);
    }

    public forecastAdapter(ListItemOnClickListener onClickListener) {
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
        String weatherForThisDay = weatherData[position];
        forecastAdapterViewHolder.weatherDataTextView.setText(weatherForThisDay);
    }

    @Override
    public int getItemCount() {
        if (null == weatherData) return 0;
        return weatherData.length;
    }

    public void setWeatherData(String[] data) {
        weatherData = data;
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
            String weather = weatherData[clickedPosition];
            mOnClickListener.onItemClickListener(weather);
        }

    }
}
