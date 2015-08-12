package com.dtapia.clearskies.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Parcelable;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dtapia.clearskies.adapters.DayAdapter;
import com.dtapia.clearskies.weather.Day;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import com.dtapia.clearskies.R;

public class DailyForecastActivity extends ListActivity {

    private Day[] mDays;
    @Bind(R.id.locationLabel) TextView mLocationLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);
        ButterKnife.bind(this);

        mLocationLabel.setText(MainActivity.location);

        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST);
        mDays = Arrays.copyOf(parcelables, parcelables.length, Day[].class);

        DayAdapter adapter = new DayAdapter(this, mDays);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String dayOfTheWeek = mDays[position].getDayOfTheWeek();
        String conditions = mDays[position].getSummary();
        String highTemp = mDays[position].getTemperatureMax() + "";
        String message = String.format("On %s the high will be %s. %s",
                dayOfTheWeek,
                highTemp,
                conditions);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
