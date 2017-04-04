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

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dtapia.clearskies.R;
import com.dtapia.clearskies.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private ShareActionProvider mShareActionProvider;
    private String mForecast;
    private Uri mUri;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            WeatherContract.LocationEntry.COLUMN_CITY_NAME,         //0
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,         //1
            WeatherContract.WeatherEntry.COLUMN_ICON_ID,            //2
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,           //3
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,           //4
            WeatherContract.WeatherEntry.COLUMN_PRECIPITATION,      //5
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,         //6
            WeatherContract.WeatherEntry.COLUMN_DEGREES,            //7
            WeatherContract.WeatherEntry.COLUMN_CURRENT_TIME,       //8
            WeatherContract.WeatherEntry.COLUMN_SUNRISE_TIME,       //9
            WeatherContract.WeatherEntry.COLUMN_SUNSET_TIME,        //10
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,           //11
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP            //12
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_CITY_NAME = 0;
    public static final int COL_WEATHER_DESC = 1;
    public static final int COL_WEATHER_ICON_ID = 2;
    public static final int COL_WEATHER_HUMIDITY = 3;
    public static final int COL_WEATHER_PRESSURE = 4;
    public static final int COL_WEATHER_PRECIPITATION = 5;
    public static final int COL_WEATHER_WIND_SPEED = 6;
    public static final int COL_WEATHER_DEGREES = 7;
    public static final int COL_WEATHER_TIME = 8;
    public static final int COL_WEATHER_SUNRISE_TIME = 9;
    public static final int COL_WEATHER_SUNSET_TIME = 10;
    public static final int COL_WEATHER_HIGH_TEMP = 11;
    public static final int COL_WEATHER_LOW_TEMP = 12;

    private ImageView mIconView;
    private TextView mDateView;
    private TextView mSummaryView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mPrecipitationView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;
    private TextView mSunriseView;
    private TextView mSunsetView;


    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.daily_fragment_detail, container, false);
        mIconView = (ImageView) rootView.findViewById(R.id.iconImageView);
        mDateView = (TextView) rootView.findViewById(R.id.dateLabel);
        mSummaryView = (TextView) rootView.findViewById(R.id.daily_summaryLabel);
        mHighTempView = (TextView) rootView.findViewById(R.id.highTemperatureLabel);
        mLowTempView = (TextView) rootView.findViewById(R.id.lowTemperatureLabel);
        mPrecipitationView = (TextView) rootView.findViewById(R.id.precipValue);
        mHumidityView = (TextView) rootView.findViewById(R.id.humidityValue);
        mWindView = (TextView) rootView.findViewById(R.id.windValue);
        mPressureView = (TextView) rootView.findViewById(R.id.pressureValue);
        mSunriseView = (TextView) rootView.findViewById(R.id.sunriseTimeValue);
        mSunsetView = (TextView) rootView.findViewById(R.id.sunsetTimeValue);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);
        // Retrieve the share menu item
       /* MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mForecast != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }*/
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {

            long currentTime = data.getLong(COL_WEATHER_TIME);
            long sunriseTime = data.getLong(COL_WEATHER_SUNRISE_TIME);
            long sunsetTime = data.getLong(COL_WEATHER_SUNSET_TIME);

            String iconId = data.getString(COL_WEATHER_ICON_ID);
            mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(iconId, currentTime, sunsetTime));

            long date = data.getLong(COL_WEATHER_TIME);
            String friendlyDateText = Utility.getDayName(getActivity(), date);
            String dateText = Utility.getFormattedMonthDay(getActivity(), date);
            mDateView.setText(getActivity().getString(R.string.format_full_friendly_date, friendlyDateText, dateText));

            String description = data.getString(COL_WEATHER_DESC);
            mSummaryView.setText(description);

            double highTemperature = data.getDouble(COL_WEATHER_HIGH_TEMP);
            String high = Utility.formatTemperature(getActivity(), highTemperature);
            mHighTempView.setText(high);

            double lowTemperature = data.getDouble(COL_WEATHER_LOW_TEMP);
            String low = Utility.formatTemperature(getActivity(), lowTemperature);
            mLowTempView.setText(low);

            double precipitation = data.getDouble(COL_WEATHER_PRECIPITATION);
            int formattedPrecipitation = Utility.formatPercentage(precipitation);
            mPrecipitationView.setText(getActivity().getString(R.string.format_precipitation, formattedPrecipitation));

            double humidity = data.getDouble(COL_WEATHER_HUMIDITY);
            int formattedHumdity = Utility.formatPercentage(humidity);
            mHumidityView.setText(getActivity().getString(R.string.format_humidity, formattedHumdity));

            float windSpeedStr = data.getFloat(COL_WEATHER_WIND_SPEED);
            float windDirStr = data.getFloat(COL_WEATHER_DEGREES);
            mWindView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));

            float pressure = data.getFloat(COL_WEATHER_PRESSURE);
            mPressureView.setText(getActivity().getString(R.string.format_pressure, pressure));

            mSunriseView.setText(Utility.getFormattedTime(sunriseTime));
            mSunsetView.setText(Utility.getFormattedTime(sunsetTime));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}