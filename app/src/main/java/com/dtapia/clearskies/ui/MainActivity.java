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
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.dtapia.clearskies.R;
import com.dtapia.clearskies.adapters.ViewPagerAdapter;
import com.dtapia.clearskies.sync.ForecastSyncAdapter;

public class MainActivity extends AppCompatActivity implements DailyForecastFragment.Callback, CurrentForecastFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private boolean mTwoPane;
    private String mLocation;
    private String mCityName;

    ViewPagerAdapter mViewPagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocation = Utility.getPreferredLocation(this);
        mCityName = Utility.getPreferredCityName(this);
        setContentView(R.layout.activity_main);

        setTitle(mCityName);

        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(mViewPagerAdapter);
        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                /*getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();*/
            }
        } else {
            mTwoPane = false;
            // getSupportActionBar().setElevation(0f);
        }

        CurrentForecastFragment forecastFragment = ((CurrentForecastFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast));
        //forecastFragment.setUseTodayLayout(!mTwoPane);
        ForecastSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation(this);
        // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
            /*CurrentForecastFragment ff = (CurrentForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if (null != ff) {
                ff.onLocationChanged();
            }
            HourlyForecastFragment hf = (HourlyForecastFragment) getSupportFragmentManager().findFragmentById(R.id.hourly_forecast);
            if (null != hf) {
                hf.onLocationChanged();
            }
            DailyForecastFragment df = (DailyForecastFragment) getSupportFragmentManager().findFragmentById(R.id.daily_forecast);
            if (null != df) {
                df.onLocationChanged();
            }
            mLocation = location;*/
        }
        String cityName = Utility.getPreferredCityName(this);

        if(cityName != null && !cityName.equals(mCityName)){
            mCityName = cityName;
            setTitle(mCityName);
        }

    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.action_settings, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {

            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }

    @Override
    public void displayAttribute(String attributionUrl) {
        Uri uri = Uri.parse(attributionUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
