package com.dtapia.clearskies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dtapia.clearskies.R;
import com.dtapia.clearskies.data.WeatherContract;
import com.dtapia.clearskies.ui.DailyForecastFragment;
import com.dtapia.clearskies.ui.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.R.attr.choiceMode;
import static com.dtapia.clearskies.R.layout.list_item_daily;

/**
 * Created by Daniel on 7/28/2015.
 */

public class DayAdapter extends RecyclerView.Adapter <DayAdapter.DayViewHolder> {

    private Cursor mCursor;
    final private Context mContext;
    final private DayAdapterOnClickHandler mClickHandler;
    final private View mEmptyView;
    final private ItemChoiceManager mICM;

    public class DayViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        @Bind(R.id.dayNameLabel) public TextView dayNameView;
        @Bind(R.id.shortSummaryLabel) public TextView dayShortSummaryView;
        @Bind(R.id.iconImageView) public ImageView iconView;
        @Bind(R.id.highTemperatureLabel) public TextView highTempView;
        @Bind(R.id.lowTemperatureLabel) public TextView lowTempView;

        public DayViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int dateColumnIndex = mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_CURRENT_TIME);
            mClickHandler.onClick(mCursor.getLong(dateColumnIndex), this);
            mICM.onClick(this);
        }
    }

    public static interface DayAdapterOnClickHandler {
        void onClick(Long date, DayViewHolder vh);
    }

    public DayAdapter(Context context, DayAdapterOnClickHandler handler, View emptyView, int choiceMo) {
        mContext = context;
        mClickHandler = handler;
        mEmptyView = emptyView;
        mICM = new ItemChoiceManager(this);
        mICM.setChoiceMode(choiceMode);
    }

    @Override
    public DayViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(list_item_daily, viewGroup, false);
        view.setFocusable(true);

        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DayViewHolder viewHolder, int position) {
        mCursor.moveToPosition(position);

        //boolean isMetric = Utility.isMetric(context);
        Long dateInMillis = mCursor.getLong(DailyForecastFragment.COL_WEATHER_DATE);

        String dayOfTheWeek = Utility.getDayName(mContext, dateInMillis);
        viewHolder.dayNameView.setText(dayOfTheWeek);

        /*String monthDay = Utility.getFormattedMonthDay(mContext, dateInMillis);
        viewHolder.dateView.setText(monthDay);*/

        String iconDescription = mCursor.getString(DailyForecastFragment.COL_WEATHER_ICON_ID);
        viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(iconDescription));

        String shortSummary = Utility.getDailySummary(iconDescription);
        viewHolder.dayShortSummaryView.setText(shortSummary);

        double dailyHighTemp = mCursor.getDouble(DailyForecastFragment.COL_WEATHER_HIGH_TEMP);
        viewHolder.highTempView.setText(Utility.formatTemperature(mContext, dailyHighTemp));

        double dailyLowTemp = mCursor.getDouble(DailyForecastFragment.COL_WEATHER_LOW_TEMP);
        viewHolder.lowTempView.setText(Utility.formatTemperature(mContext, dailyLowTemp));

        //For accessibility, add a content description to the icon field
        viewHolder.iconView.setContentDescription(iconDescription);
        mICM.onBindViewHolder(viewHolder, position);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mICM.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        mICM.onSaveInstanceState(outState);
    }


    public int getSelectedItemPosition() {
        return mICM.getSelectedItemPosition();
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

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if ( viewHolder instanceof DayViewHolder ) {
            DayViewHolder vfh = (DayViewHolder)viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }
}

