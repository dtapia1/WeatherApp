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
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dtapia.clearskies.R;
import com.dtapia.clearskies.adapters.DayAdapter;
import com.dtapia.clearskies.adapters.ViewPagerAdapter;
import com.dtapia.clearskies.sync.ForecastSyncAdapter;

import static com.dtapia.clearskies.R.id.toolbar;

public class MainActivity extends AppCompatActivity implements DailyForecastFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String CURRENTFRAGMENT_TAG = "CFTAG";

    private boolean mTwoPane;
    private String mLocation;
    private String mCityName;

    ViewPagerAdapter mViewPagerAdapter;
    ViewPager mViewPager;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        mLocation = Utility.getPreferredLocation(this);
        mCityName = Utility.getPreferredCityName(this);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.setTitle(mCityName);

        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), this);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(mViewPagerAdapter);

        CurrentForecastFragment currentForecastFragment = new CurrentForecastFragment();

        if(getResources().getBoolean(R.bool.two_pane)){
            Bundle arguments = new Bundle();
            arguments.putBoolean(CurrentForecastFragment.TWO_PANE, true);
            currentForecastFragment.setArguments(arguments);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_weather_container, currentForecastFragment, CURRENTFRAGMENT_TAG)
                    .commit();
        }

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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String cityName = Utility.getPreferredCityName(this);

        if (cityName != null && !cityName.equals(mCityName)) {
            mCityName = cityName;
            mToolbar.setTitle(mCityName);
        }
    }

    @Override
    public void onItemSelected(Uri contentUri, DayAdapter.DayViewHolder vh) {

        Intent intent = new Intent(this, DetailActivity.class)
                .setData(contentUri);
        ActivityOptionsCompat activityOptions =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        new Pair<View, String>(vh.iconView, getString(R.string.detail_icon_transition_name)));
        ActivityCompat.startActivity(this, intent, activityOptions.toBundle());

    }
    /*@Override
    public void onClickCurrentLayout(Uri contentUri) {
        Intent intent = new Intent(this, DetailActivity.class)
                .setData(contentUri);
        intent.putExtra(DetailFragment.DISPLAY_CURRENT, true);
        startActivity(intent);
    }*/

}
