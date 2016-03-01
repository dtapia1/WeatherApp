package com.dtapia.clearskies.weather;

import com.dtapia.clearskies.R;

/**
 * Created by Daniel on 7/28/2015.
 */
public class Forecast {

    private Current mCurrent;
    private Hour[] mHourlyForecast;
    private Day[] mDailyForecast;

    public Current getCurrent() {
        return mCurrent;
    }

    public void setCurrent(Current current) {
        mCurrent = current;
    }

    public Hour[] getHourlyForecast() {
        return mHourlyForecast;
    }

    public void setHourlyForecast(Hour[] hourlyForecast) {
        mHourlyForecast = hourlyForecast;
    }

    public Day[] getDailyForecast() {
        return mDailyForecast;
    }

    public void setDailyForecast(Day[] dailyForecast) {
        mDailyForecast = dailyForecast;
    }

    public static int getIconId(String iconString, Long currentTime, Long sunsetTime){
        int iconId = R.drawable.ic_weather_clear;

        if (iconString.equals("clear-day")) {
            iconId = R.drawable.ic_weather_clear;
        }
        else if (iconString.equals("clear-night")) {
            iconId = R.drawable.ic_weather_clear_night;
        }
        else if (iconString.equals("rain") && currentTime >= sunsetTime) {
            iconId = R.drawable.ic_weather_rain_night;
        }
        else if (iconString.equals("rain")) {
            iconId = R.drawable.ic_weather_rain_day;
        }
        else if (iconString.equals("snow")) {
            iconId = R.drawable.ic_weather_snow;
        }
        else if (iconString.equals("sleet")) {
            iconId = R.drawable.ic_weather_snow_rain;
        }
        else if (iconString.equals("wind")) {
            iconId = R.drawable.ic_weather_wind;
        }
        else if (iconString.equals("fog")) {
            iconId = R.drawable.ic_weather_fog;
        }
        else if (iconString.equals("cloudy")) {
            iconId = R.drawable.ic_weather_clouds;
        }
        else if (iconString.equals("partly-cloudy-day")) {
            iconId = R.drawable.ic_weather_few_clouds;
        }
        else if (iconString.equals("partly-cloudy-night")) {
            iconId = R.drawable.ic_weather_clouds_night;
        }
        return iconId;
    }
}
