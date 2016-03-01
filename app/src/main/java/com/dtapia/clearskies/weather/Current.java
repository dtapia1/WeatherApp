package com.dtapia.clearskies.weather;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Daniel on 7/27/2015.
 */
public class Current {
    private String mIcon;
    private long mTime;
    private double mTemperature;
    private double mTemperatureMax;
    private double mTempertureMin;
    private double mApparentTemperature;
    private String mAlertTitle;
    private long mSunriseTime;
    private long mSunsetTime;
    private long mDate;
    private double mHumidity;
    private double mPrecipChance;
    private double mWindSpeed;
    private String mSummary;
    private String mTimeZone;

    public String getTimeZone() {
        return mTimeZone;
    }

    public void setTimeZone(String timeZone) {
        mTimeZone = timeZone;
    }
    public void setDate(long date) {
        mDate = date;
    }

    public String getDate(){
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, MMM dd");
        formatter.setTimeZone(TimeZone.getTimeZone(mTimeZone));
        Date dateTime = new Date(mTime * 1000);
        return formatter.format(dateTime);
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    public int getIconId()
    {
        return Forecast.getIconId(mIcon, mTime, mSunsetTime);
    }

    public long getTime() {
        return mTime;
    }

    public String getFormattedTime(long time)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        formatter.setTimeZone(TimeZone.getTimeZone(getTimeZone()));
        Date dateTime = new Date(time * 1000);
        String timeString = formatter.format(dateTime);

        return timeString;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public int getTemperature() {
        return (int) Math.round(mTemperature);
    }

    public void setTemperature(double temperature) {
        mTemperature = temperature;
    }


    public int getTempertureMin() {
        return (int) Math.round(mTempertureMin);
    }

    public void setTempertureMin(double tempertureMin) {
        mTempertureMin = tempertureMin;
    }

    public int getHumidity() {
        double humidityPercentage = mHumidity * 100;
        return (int)Math.round(humidityPercentage);
    }

    public void setHumidity(double humidity) {
        mHumidity = humidity;
    }

    public int getWindSpeed() {
        double windSpeed = mWindSpeed;
        return (int)Math.round(windSpeed);
    }

    public void setWindSpeed(double windSpeed) {
        mWindSpeed = windSpeed;
    }

    public int getPrecipChance() {
        double precipPercentage = mPrecipChance * 100;
        return (int)Math.round(precipPercentage);
    }

    public void setPrecipChance(double precipChance) {
        mPrecipChance = precipChance;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }


    public String getAlertTitle() {
        return mAlertTitle;
    }

    public void setAlertTitle(String alertTitle) {
        mAlertTitle = alertTitle;
    }

    public void setApparentTemperature(double apparentTemperature) {
        mApparentTemperature = apparentTemperature;
    }

    public int getApparentTemperature() {
        return (int)Math.round(mApparentTemperature);
    }

    public void setTemperatureMax(double temperatureMax) {
        mTemperatureMax = temperatureMax;
    }

    public int getTemperatureMax() {
        return (int)Math.round(mTemperatureMax);
    }

    public void setSunriseTime(long sunriseTime) {
        mSunriseTime = sunriseTime;
    }

    public long getSunriseTime() {
        return mSunriseTime;
    }

    public void setSunsetTime(long sunsetTime) {

        mSunsetTime = sunsetTime;
    }

    public long getSunsetTime() {
        return mSunsetTime;
    }


}
