package com.twave.step02;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by TIGER on 2017-05-15.
 */

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    private String[] mWeatherData;

    private final ForecastAdapterOnClickHandler mClickHandler;
    // 아이템 클릭시 실행함수
    public interface ForecastAdapterOnClickHandler {
        void onClick(String weatherForDay);
    }

    public ForecastAdapter(ForecastAdapterOnClickHandler clickHandler) {
        this.mClickHandler = clickHandler;
    }

    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public final TextView mWeatherTextView;
        public ForecastAdapterViewHolder(View view) {
            super(view);
            this.mWeatherTextView = (TextView) view.findViewById(R.id.tv_weather_data);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            String weatherForDay = mWeatherData[adapterPosition];
            mClickHandler.onClick(weatherForDay);
        }
    }

    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.forecast_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new ForecastAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder forecastAdapterViewHolder, int position) {
        String weatherForThisDay = mWeatherData[position];
        forecastAdapterViewHolder.mWeatherTextView.setText(weatherForThisDay);
    }

    @Override
    public int getItemCount() {
        if(mWeatherData == null) return 0;
        return mWeatherData.length;
    }

    public void setWeatherData(String[] weatherData) {
        mWeatherData = weatherData;
        notifyDataSetChanged();
    }

}
