package com.dtapia.clearskies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dtapia.clearskies.R;
import com.dtapia.clearskies.ui.HourlyForecastFragment;
import com.dtapia.clearskies.ui.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.dtapia.clearskies.R.layout.list_item_hourly;

/**
 * Created by Daniel on 7/29/2015.
 */
public class HourAdapter extends RecyclerView.Adapter<HourAdapter.HourViewHolder> {

    private Cursor mCursor;
    final private Context mContext;
    //final private HourAdapterOnClickHandler mClickHandler;
    final private View mEmptyView;
    private int selectedItem = 0;

    public class HourViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.timeLabel)
        TextView mTimeLabel;
        @Bind(R.id.shortSummaryLabel)
        TextView mSummaryLabel;
        @Bind(R.id.temperatureLabel)
        TextView mTemperatureLabel;
        @Bind(R.id.iconImageView)
        ImageView mIconImageView;

        public HourViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notifyItemChanged(selectedItem);
                    selectedItem = getLayoutPosition();
                    notifyItemChanged(selectedItem);
                }
            });
        }

    }

    public HourAdapter(Context context, View emptyView) {
        mContext = context;
        //mClickHandler = handler;
        mEmptyView = emptyView;
    }

    @Override
    public HourViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(list_item_hourly, viewGroup, false);
        view.setFocusable(true);

        return new HourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HourViewHolder viewHolder, int position) {
        mCursor.moveToPosition(position);
        viewHolder.itemView.setSelected(selectedItem == position);
        //boolean isMetric = Utility.isMetric(context);
        long time = mCursor.getLong(HourlyForecastFragment.COL_WEATHER_TIME);
        String hour = Utility.getFormattedHour(time);
        viewHolder.mTimeLabel.setText(hour);
        double temperature = mCursor.getDouble(HourlyForecastFragment.COL_WEATHER_TEMP);
        viewHolder.mTemperatureLabel.setText(Utility.formatTemperature(mContext, temperature));
        String summary = mCursor.getString(HourlyForecastFragment.COL_WEATHER_DESC);
        viewHolder.mSummaryLabel.setText(summary);
        String iconDescription = mCursor.getString(HourlyForecastFragment.COL_WEATHER_ICON_ID);
        viewHolder.mIconImageView.setImageResource(Utility.getIconResourceForWeatherCondition(iconDescription));
        //For accessibility, add a content description to the icon field
        viewHolder.mIconImageView.setContentDescription(iconDescription);
    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }


}
