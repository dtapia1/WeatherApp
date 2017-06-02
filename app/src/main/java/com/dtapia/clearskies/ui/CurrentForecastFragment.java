/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dtapia.clearskies.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dtapia.clearskies.R;
import com.dtapia.clearskies.data.WeatherContract;
import com.dtapia.clearskies.sync.ForecastSyncAdapter;


public class CurrentForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = CurrentForecastFragment.class.getSimpleName();
    public static final String TWO_PANE = "TWO_PANE";
    private int mPosition = ListView.INVALID_POSITION;
    private String mLocation;
    private String mUnits;
    private boolean mTwoPane;
    private Uri weatherForLocationUri;

    private static final String SELECTED_KEY = "selected_position";

    private static final int FORECAST_LOADER = 0;
    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    public static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.LocationEntry.COLUMN_CITY_NAME,         //0
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,         //1
            WeatherContract.WeatherEntry.COLUMN_ICON_ID,            //2
            WeatherContract.WeatherEntry.COLUMN_CURRENT_TEMP,       //3
            WeatherContract.WeatherEntry.COLUMN_APPARENT_TEMP,      //4
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,           //5
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,           //6
            WeatherContract.WeatherEntry.COLUMN_PRECIPITATION,      //7
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,         //8
            WeatherContract.WeatherEntry.COLUMN_DEGREES,            //9
            WeatherContract.WeatherEntry.COLUMN_CURRENT_TIME,       //10
            WeatherContract.WeatherEntry.COLUMN_SUNRISE_TIME,       //11
            WeatherContract.WeatherEntry.COLUMN_SUNSET_TIME,        //12
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,           //13
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP            //14
    };


    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_CITY_NAME = 0;
    public static final int COL_WEATHER_DESC = 1;
    public static final int COL_WEATHER_ICON_ID = 2;
    public static final int COL_WEATHER_CURRENT_TEMP = 3;
    public static final int COL_WEATHER_APARRENT_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_PRECIPITATION = 7;
    public static final int COL_WEATHER_WIND_SPEED = 8;
    public static final int COL_WEATHER_DEGREES = 9;
    public static final int COL_WEATHER_TIME = 10;
    public static final int COL_WEATHER_SUNRISE_TIME = 11;
    public static final int COL_WEATHER_SUNSET_TIME = 12;
    public static final int COL_WEATHER_HIGH_TEMP = 13;
    public static final int COL_WEATHER_LOW_TEMP = 14;

    private ImageView mIconView;
    private TextView mLocationView;
    private TextView mFriendlyDateView;
    private TextView mDateView;
    private TextView mHighLowTempView;
    private TextView mSummaryView;
    private TextView mCurrentTemperatureView;
    private TextView mLowTemperatureView;
    //private TextView mApparrentTemperatureView;
    private TextView mPrecipitationView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    /*public interface Callback {
        *//**
         * MainActivityCallback for when an item has been selected.
         *//*
        public void onClickCurrentLayout(Uri uri);
    }*/

    public CurrentForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
        mLocation = Utility.getPreferredLocation(getActivity());
        mUnits = Utility.getPreferredUnits(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.currentforecastfragment, menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation(getActivity());
        String units = Utility.getPreferredUnits(getActivity());
        // update the location
        if (location != null && !location.equals(mLocation)
                || units != null && !units.equals(mUnits)) {
            onLocationChanged();

            mLocation = location;
            mUnits = units;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        /*if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_current_forecast, container, false);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mTwoPane = arguments.getBoolean(CurrentForecastFragment.TWO_PANE);
        }
        /*if(!mTwoPane){
            LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.current_forecast_fragment_layout);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((Callback) getActivity()).onClickCurrentLayout(weatherForLocationUri);
                }
            });
        }*/
        mIconView = (ImageView) rootView.findViewById(R.id.iconImageView);
        /*mLocationView = (TextView) rootView.findViewById(R.id.locationLabel);*/
        mDateView = (TextView) rootView.findViewById(R.id.dateLabel);
        //mFriendlyDateView = (TextView) rootView.findViewById(R.id.dateLabel);
        //mHighLowTempView = (TextView) rootView.findViewById(R.id.highLowTemperature);
        mSummaryView = (TextView) rootView.findViewById(R.id.shortSummaryLabel);
        mCurrentTemperatureView = (TextView) rootView.findViewById(R.id.temperatureLabel);
        mLowTemperatureView = (TextView) rootView.findViewById(R.id.lowTemperatureLabel);
        //mApparrentTemperatureView = (TextView) rootView.findViewById(R.id.apparentTemperatureLabel);
        if (mTwoPane) {
            mPrecipitationView = (TextView) rootView.findViewById(R.id.precipValue);
            mHumidityView = (TextView) rootView.findViewById(R.id.humidityValue);
            mPressureView = (TextView) rootView.findViewById(R.id.pressureValue);
            mWindView = (TextView) rootView.findViewById(R.id.windValue);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    // since we read the location when we create the loader, all we need to do is restart things
    void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    private void updateWeather() {
        ForecastSyncAdapter.syncImmediately(getActivity());
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_CURRENT_TIME + " ASC";

        String locationSetting = Utility.getPreferredLocation(getActivity());
        weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithWeatherType(
                locationSetting, ForecastSyncAdapter.CURRENTLY);
        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        if (data != null && data.moveToFirst()) {
            // Read weather icon ID from cursor
            long currentTime = data.getLong(COL_WEATHER_TIME);
            long sunriseTime = data.getLong(COL_WEATHER_SUNRISE_TIME);
            long sunsetTime = data.getLong(COL_WEATHER_SUNSET_TIME);

            String iconId = data.getString(COL_WEATHER_ICON_ID);

            // Use weather art image
            mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(iconId, currentTime, sunsetTime));

            // Read date from cursor and update views for day of week and date
            long date = data.getLong(COL_WEATHER_TIME);

            //long date = System.currentTimeMillis();
            String friendlyDateText = Utility.getDayName(getActivity(), date);
            String dateText = Utility.getFormattedMonthDay(getActivity(), date);
            //mFriendlyDateView.setText(friendlyDateText);
            mDateView.setText(dateText);

            // Read description from cursor and update view
            String description = data.getString(COL_WEATHER_DESC);
            mSummaryView.setText(description);

            // For accessibility, add a content description to the icon field
            mIconView.setContentDescription(description);

            // Read high temperature from cursor and update view
            boolean isMetric = Utility.isMetric(getActivity());

            double current = data.getDouble(COL_WEATHER_CURRENT_TEMP);
            String currentTemperature = Utility.formatTemperature(getActivity(), current);
            mCurrentTemperatureView.setText(currentTemperature);

            //double apparent = data.getDouble(COL_WEATHER_APARRENT_TEMP);
            //String apparentTemperature = Utility.formatTemperature(getActivity(), apparent);
            //mApparrentTemperatureView.setText(getActivity().getString(R.string.format_apparent_temperature, apparentTemperature));

            double highTemperature = data.getDouble(COL_WEATHER_HIGH_TEMP);
            double lowTemperature = data.getDouble(COL_WEATHER_LOW_TEMP);
            String high = Utility.formatTemperature(getActivity(), highTemperature);
            String low = Utility.formatTemperature(getActivity(), lowTemperature);
            //mHighLowTempView.setText(getActivity().getString(R.string.format_high_low_temperature, high, low));
            mLowTemperatureView.setText(low);

            if (mTwoPane) {
                double precipationChance = data.getDouble(COL_WEATHER_PRECIPITATION);
                int formattedPrecip = Utility.formatPercentage(precipationChance);
                mPrecipitationView.setText(getActivity().getString(R.string.format_humidity, formattedPrecip));

                // Read humidity from cursor and update view
                double humidity = data.getDouble(COL_WEATHER_HUMIDITY);
                int formattedHumdity = Utility.formatPercentage(humidity);
                mHumidityView.setText(getActivity().getString(R.string.format_humidity, formattedHumdity));

                // Read wind speed and direction from cursor and update view
                float windSpeedStr = data.getFloat(COL_WEATHER_WIND_SPEED);
                float windDirStr = data.getFloat(COL_WEATHER_DEGREES);
                mWindView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));

                // Read pressure from cursor and update view
                float pressure = data.getFloat(COL_WEATHER_PRESSURE);
                mPressureView.setText(getActivity().getString(R.string.format_pressure, pressure));
            }


            // We still need this for the share intent
            //mForecast = String.format("%s - %s - %s/%s", dateText, description, high, low);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
           /* if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }*/
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        loader = null;
    }

}