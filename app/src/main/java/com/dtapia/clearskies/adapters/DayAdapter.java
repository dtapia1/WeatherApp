package com.dtapia.clearskies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dtapia.clearskies.R;
import com.dtapia.clearskies.weather.Day;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Daniel on 7/28/2015.
 */

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {

    private Day[] mDays;
    private Context mContext;
    private String mSummary;

    public DayAdapter(Context context, Day[] days) {
        mContext = context;
        mDays = days;
    }

    @Override
    public DayViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.daily_list_item, viewGroup, false);
        DayViewHolder viewHolder = new DayViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DayViewHolder dayViewHolder, int i) {

        dayViewHolder.bindDay(mDays[i], i);
    }

    @Override
    public int getItemCount() {
        return mDays.length;
    }

    public class DayViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        @Bind(R.id.dayNameLabel) TextView mDayNameLabel;
        @Bind(R.id.dateLabel) TextView mDateLabel;
        @Bind(R.id.highTemperatureLabel) TextView mHighTemperatureLabel;
        @Bind(R.id.lowTemperatureLabel) TextView mLowTemperatureLabel;
        @Bind(R.id.iconImageView) ImageView mIconImageView;

        public DayViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bindDay(Day day, int i) {

            String dayString = day.getDayOfTheWeek();
            String monthString = day.getMonth().substring(0,3); //extract first 3 letters of month name
            if (i == 0) {
                mDayNameLabel.setText("Today");

            } else {
                mDayNameLabel.setText(dayString);
            }
            mDateLabel.setText(monthString + day.getDate());
            mHighTemperatureLabel.setText(day.getTemperatureMax() + "");
            mLowTemperatureLabel.setText(day.getTemperatureMin() + "");
            mIconImageView.setImageResource(day.getIconId());
            mSummary = day.getSummary();
        }

        @Override
        public void onClick(View v) {
            // Retrieve accurate position of current view
            String summary = String.format("%s", mDays[getAdapterPosition()].getSummary().toString());

            Toast.makeText(mContext, summary, Toast.LENGTH_LONG).show();
        }
    }
}
