package com.dtapia.clearskies.weather;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Daniel on 7/28/2015.
 */
public class Day implements Parcelable{

    private long mTime;
    private long mDate;
    private String mSummary;
    private double mTemperatureMax;
    private double mTemperatureMin;
    private String mIcon;
    private String mTimezone;

    public long mSunriseTime;
    public long mSunsetTime;

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public int getTemperatureMax() {
        return (int)Math.round(mTemperatureMax);
    }

    public void setTemperatureMax(double temperatureMax) {
        mTemperatureMax = temperatureMax;
    }

    public int getTemperatureMin() {
        return (int)Math.round(mTemperatureMin);
    }

    public void setTemperatureMin(double temperatureMin) {
        mTemperatureMin = temperatureMin;
    }


    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    public String getTimezone() {
        return mTimezone;
    }

    public void setTimezone(String timezone) {
        mTimezone = timezone;
    }
    public int getIconId(){
        return Forecast.getIconId(mIcon, mTime, mSunsetTime);
    }

    public String getDayOfTheWeek(){
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE");
        formatter.setTimeZone(TimeZone.getTimeZone(mTimezone));
        Date dateTime = new Date(mTime * 1000);
        return formatter.format(dateTime);
    }

    public void setDate(long date) {
        mDate = date;
    }

    public String getDate(){
        SimpleDateFormat formatter = new SimpleDateFormat(", dd");
        formatter.setTimeZone(TimeZone.getTimeZone(mTimezone));
        Date dateTime = new Date(mTime * 1000);
        return formatter.format(dateTime);
    }

    public String getMonth(){
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM");
        formatter.setTimeZone(TimeZone.getTimeZone(mTimezone));
        Date dateTime = new Date(mTime * 1000);
        return formatter.format(dateTime);
    }

    @Override
    public int describeContents() {
        return 0; //don't need
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mTime);
        dest.writeLong(mDate);
        dest.writeString(mSummary);
        dest.writeDouble(mTemperatureMax);
        dest.writeDouble(mTemperatureMin);
        dest.writeString(mIcon);
        dest.writeString(mTimezone);
    }

    private Day(Parcel n){
        mTime = n.readLong();
        mDate = n.readLong();
        mSummary = n.readString();
        mTemperatureMax = n.readDouble();
        mTemperatureMin = n.readDouble();
        mIcon = n.readString();
        mTimezone = n.readString();
    }

    public Day(){

    }
    public static final Creator<Day> CREATOR = new Creator<Day>() {
        @Override
        public Day createFromParcel(Parcel source) {
            return new Day(source);
        }

        @Override
        public Day[] newArray(int size) {
            return new Day[size];
        }
    };

    public long getSunriseTime() {
        return mSunriseTime;
    }

    public void setSunriseTime(long sunriseTime) {
        mSunriseTime = sunriseTime;
    }

    public long getSunsetTime() {
        return mSunsetTime;
    }

    public void setSunsetTime(long sunsetTime) {
        mSunsetTime = sunsetTime;
    }
}
