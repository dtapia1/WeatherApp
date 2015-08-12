package com.dtapia.clearskies.ui;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.dtapia.clearskies.adapters.HourAdapter;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import com.dtapia.clearskies.R;

import com.dtapia.clearskies.weather.Hour;

public class HourlyForecastActivity extends ActionBarActivity {

    private Hour[] mHours;
    @Bind(R.id.recyclerView)RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hourly_forecast);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.HOURLY_FORECAST);
        mHours = Arrays.copyOf(parcelables, parcelables.length, Hour[].class);

        HourAdapter adapter = new HourAdapter(this, mHours);
        mRecyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        //use for fixed size data
        mRecyclerView.setHasFixedSize(true);
    }
}
