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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.dtapia.clearskies.R;
import com.dtapia.clearskies.adapters.DayAdapter;
import com.dtapia.clearskies.data.WeatherContract;
import com.dtapia.clearskies.sync.ForecastSyncAdapter;


public class DailyForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = DailyForecastFragment.class.getSimpleName();
    private DayAdapter mDayAdapter;
    private ListView mDailyListView;
    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;
    //private RecyclerView mRecyclerView;
    //private int mPosition = RecyclerView.NO_POSITION;


    private String mLocation;
    private String mUnits;

    private static final int DAILY_FORECAST_LOADER = 2;
    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] DAILY_FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_CURRENT_TIME,
            WeatherContract.WeatherEntry.COLUMN_ICON_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
    };

    // These indices are tied to DAILY_FORECAST_COLUMNS.  If DAILY_FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_ICON_ID = 2;
    public static final int COL_WEATHER_HIGH_TEMP = 3;
    public static final int COL_WEATHER_LOW_TEMP = 4;
    public static final int COL_WEATHER_DESC = 5;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    public DailyForecastFragment() {
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_daily_forecast, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.daily_forecast_fragment, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_daily_forecast);

        //set layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        View emptyView = rootView.findViewById(R.id.recyclerview_forecast_empty);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // The DayAdapter will take data from a source and
        // use it to populate the RecyclerView it's attached to.
        mDayAdapter = new DayAdapter(getActivity(), new DayAdapter.DayAdapterOnClickHandler() {
            @Override
            public void onClick(Long date, DayAdapter.DayViewHolder vh) {
                String locationSetting = Utility.getPreferredLocation(getActivity());
                ((Callback) getActivity())
                        .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                locationSetting, date)
                        );
                mPosition = vh.getAdapterPosition();
            }
        }, emptyView);

        /*// The DayAdapter will take data from a source and
        // use it to populate the RecyclerView it's attached to.
        mDayAdapter = new DayAdapter(getActivity(), emptyView);*/

        // specify an adapter (see also next example)
        mRecyclerView.setAdapter(mDayAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DAILY_FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    // since we read the location when we create the loader, all we need to do is restart things
    void onLocationChanged( ) {
        updateWeather();
        getLoaderManager().restartLoader(DAILY_FORECAST_LOADER, null, this);
    }

    private void updateWeather() {
        ForecastSyncAdapter.syncImmediately(getActivity());
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        /*if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }*/
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_CURRENT_TIME + " ASC";

        Long currentTime = System.currentTimeMillis();
        String locationSetting = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithWeatherTypeAndDate(
                locationSetting, ForecastSyncAdapter.DAILY, currentTime);
        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                DAILY_FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mDayAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDayAdapter.swapCursor(null);
    }

}