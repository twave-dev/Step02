package com.twave.step02;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.twave.step02.ForecastAdapter.ForecastAdapterOnClickHandler;
import com.twave.step02.data.SunshinePreferences;
import com.twave.step02.utilities.NetworkUtils;
import com.twave.step02.utilities.OpenWeatherJsonUtils;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements
        ForecastAdapterOnClickHandler
        , LoaderManager.LoaderCallbacks<String[]> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private ForecastAdapter mForecastAdapter;

    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    private static final int FORECAST_LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        // Ctrl + Shift + A : 작업 찾기 Find Action
        // Ctrl + Shift + Space : 스마트 코드 완성(예상 형식을 기준으로 메서드 및 변수 목록 필터링)
        // Ctrl + Shift + Enter : 문법 자동 완성
        // Alt + Enter : Quick Fix
        // Ctrl + P : 선택한 메서드에 대한 매개변수 표시
        // Ctrl + Shift + F : 경로에서 찾기

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_forecast);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mForecastAdapter = new ForecastAdapter(this);
        mRecyclerView.setAdapter(mForecastAdapter);

        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        int loaderId = FORECAST_LOADER_ID;
        LoaderCallbacks<String[]> callback = MainActivity.this;
        Bundle bundleForLoader = null;
        getSupportLoaderManager().initLoader(loaderId, bundleForLoader, callback);
    }



    @Override
    public Loader<String[]> onCreateLoader(int id, Bundle loaderArgs) {
        return new AsyncTaskLoader<String[]>(this) {
            String[] mWeatherData = null;

            @Override
            protected void onStartLoading() {
                if(mWeatherData != null) {
                    deliverResult(mWeatherData);
                } else {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Override
            public String[] loadInBackground() {
                String locationQuery = SunshinePreferences.getPreferredWeatherLocation(MainActivity.this);

                URL weatherRequestUrl = NetworkUtils.buildUrl(locationQuery);

                try {
                    String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);

                    String[] simpleJsonWeatherData = OpenWeatherJsonUtils.getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);

                    return simpleJsonWeatherData;

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(String[] data) {
                mWeatherData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String[]> loader, String[] data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mForecastAdapter.setWeatherData(data);

        if(data == null) {
            showErrorMessage();
        } else {
            showWeatherDataView();
        }
    }

    @Override
    public void onLoaderReset(Loader<String[]> loader) {

    }

    private void invalidateData() {
        mForecastAdapter.setWeatherData(null);
    }

    private void openLocationMap() {
        String addressString = "1600 Ampitheatre Parkway, CA";
        Uri geoLocation = Uri.parse("geo:0,0?q=" + addressString);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if(intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
        }

    }

    @Override
    public void onClick(String weatherForDay) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, weatherForDay);
        startActivity(intentToStartDetailActivity);
    }

    private void showWeatherDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.forecast, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_refresh) {
            invalidateData();
            getSupportLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
            return  true;
        }

        if(id == R.id.action_map) {
            openLocationMap();
            return true;
        }

        if(id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }

        return  super.onOptionsItemSelected(item);
    }
}
