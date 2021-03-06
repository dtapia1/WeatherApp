package com.dtapia.clearskies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dtapia.clearskies.R;
import com.dtapia.clearskies.weather.Hour;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Daniel on 7/29/2015.
 */
public class HourAdapter extends RecyclerView.Adapter<HourAdapter.HourViewHolder> {

    private Hour[] mHours;
    private Context mContext;

    public HourAdapter(Context context, Hour[] hours) {
        mContext = context;
        mHours = hours;
    }

    @Override
    public HourViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.hourly_list_item, viewGroup, false);
        HourViewHolder viewHolder = new HourViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(HourViewHolder hourViewHolder, int i) {
        hourViewHolder.bindHour(mHours[i]);
    }

    @Override
    public int getItemCount() {
        return mHours.length;
    }

    public class HourViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        @Bind(R.id.timeLabel)
        TextView mTimeLabel;
        @Bind(R.id.summaryLabel)
        TextView mSummaryLabel;
        @Bind(R.id.temperatureLabel)
        TextView mTemperatureLabel;
        @Bind(R.id.iconImageView)
        ImageView mIconImageView;

        public HourViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        public void bindHour(Hour hour) {

            String getHour = hour.getHour();
            char firstInt = getHour.charAt(0);
            if(firstInt == '0'){
                getHour = getHour.replaceFirst("0", ""); //remove leading zero
                getHour = getHour.replaceFirst(" ", "   "); //add spacing between hour and am/pm marker
            }
            mTimeLabel.setText(getHour);
            mSummaryLabel.setText(hour.getSummary());
            mTemperatureLabel.setText(hour.getTemperature() + "");
            mIconImageView.setImageResource(hour.getIconId());
        }

        @Override
        public void onClick(View v) {
            String time = mTimeLabel.getText().toString();
            String temperature = mTemperatureLabel.getText().toString();
            String summary = mSummaryLabel.getText().toString();
            String message = String.format("%s %s and %s",
                    time, summary, temperature);
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        }
    }
}
