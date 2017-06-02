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

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.format.Time;

import com.dtapia.clearskies.R;
import com.dtapia.clearskies.sync.ForecastSyncAdapter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utility {
    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    /**
     * Stores the city name as a string in SharedPreferences.
     * @param context use to get preference key from strings.xml
     * @param zipCode used to get city name from Geocoder
     */
    public static void storeCityName(Context context, String zipCode){

        final Geocoder geocoder = new Geocoder(context);

        try {
            List<Address> addresses = geocoder.getFromLocationName(zipCode, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                // Use the address as needed
                final String cityName = address.getLocality();

                if(cityName != null){
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(context.getString(R.string.pref_city_key), cityName);
                    editor.commit();
                }

            }} catch (IOException e) {
            // handle exception
        }
    }

    public static String getPreferredCityName(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_city_key),
                context.getString(R.string.app_name));
    }

    public static String getPreferredUnits(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    public static String formatTemperature(Context context, double temperature) {
        // Data stored in Celsius by default.  If user prefers to see in Fahrenheit, convert
        // the values here.
        String suffix = "\u00B0";
        if (isMetric(context)) {
            temperature = (temperature - 32) * 5/9;
        }
        // For presentation, assume the user doesn't care about tenths of a degree.
        //String temp = String.format(context.getString(R.string.format_temperature), temperature);
        return String.format(context.getString(R.string.format_temperature), temperature);
    }

    public static int formatPercentage(double value) {
        double percentage = value * 100;

        return (int)(Math.round(percentage));
    }

    static String formatDate(long dateInMilliseconds) {
        Date date = new Date(dateInMilliseconds);
        return DateFormat.getDateInstance().format(date);
    }

    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, long dateInMillis) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Time time = new Time();
        time.setToNow();
        long currentTime = System.currentTimeMillis();
        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (julianDay == currentJulianDay) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return today;
            /*return String.format(context.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(context, dateInMillis)));*/
        } else if ( julianDay < currentJulianDay + 7 ) {
            // If the input date is less than a week in the future, just return the day name.
            return getDayName(context, dateInMillis);
        } else {
            // Otherwise, use the form "Mon Jun 3"
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return
     */
    public static String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if ( julianDay == currentJulianDay +1 ) {
            return context.getString(R.string.tomorrow);
        } else {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     * @param context Context to use for resource localization
     * @param dateInMillis The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, long dateInMillis ) {
        Time time = new Time();
        time.setToNow();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
        String monthDayString = monthDayFormat.format(dateInMillis);
        return monthDayString;
    }

    public static String getFormattedDbDate(Date date) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        String dbDate = dbDateFormat.format(date);
        return dbDate;
    }

    public static String getFormattedWind(Context context, float windSpeed, float degrees) {
        int windFormat;
        if (Utility.isMetric(context)) {
            windFormat = R.string.format_wind_kmh;
        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        // From wind direction in degrees, determine compass direction as a string (e.g NW)
        // You know what's fun, writing really long if/else statements with tons of possible
        // conditions.  Seriously, try it!
        String direction = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
        } else if (degrees >= 292.5 && degrees < 337.5) {
            direction = "NW";
        }
        return String.format(context.getString(windFormat), windSpeed, direction);
    }

    public static String getFormattedHour(Long time){
        SimpleDateFormat formatter = new SimpleDateFormat("hh:00 a");
        Date date = new Date(time);

        String hour = formatter.format(date);
        //return hour;
        return String.format("%5s", hour);
    }

    public static String getFormattedTime(Long time){
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        Date date = new Date(time);
        String formattedTime = formatter.format(date);

        return formattedTime;
    }
    /**
     * Helper method to provide the icon resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param iconString from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getIconResourceForWeatherCondition(String iconString) {
        int iconId = R.drawable.ic_weather_clear;

        if (iconString.equals("clear-day")) {
            iconId = R.drawable.ic_weather_clear;
        }
        else if (iconString.equals("clear-night")) {
            iconId = R.drawable.ic_weather_clear_night;
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

    /**
     * Helper method to provide the art resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param iconString from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getArtResourceForWeatherCondition(String iconString, long currentTime, long sunsetTime) {
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
        else if (iconString.equals("thunderstorm")) {
            iconId = R.drawable.ic_thunderstorm;
        }

        return iconId;
    }

    /**
     * Returns true if network is available or about to become available.
     *
     * @param context used to get ConnectivityManager
     * @return
     */
    static public boolean isNetworkAvailable(Context context){
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     *
     * @param c Context used to get the SharedPreferences
     * @return the location status integer type
     */
    @SuppressWarnings("ResourceType")
    static public @ForecastSyncAdapter.LocationStatus
    int getLocationStatus(Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(c.getString(R.string.pref_location_status_key), ForecastSyncAdapter.LOCATION_STATUS_UNKNOWN);
    }

    /**
     * Resets the location status.  (Sets it to SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN)
     * @param c Context used to get the SharedPreferences
     */
    static public void resetLocationStatus(Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_location_status_key), ForecastSyncAdapter.LOCATION_STATUS_UNKNOWN);
        spe.apply();
    }

    static public String getDailySummary(String iconString){
        String summary;

        if(weatherSummary.containsKey(iconString)){
            summary = weatherSummary.get(iconString);
        }else
        {
            summary = "";
        }

        return summary;
    }


    private static final Map<String, String> weatherSummary;
    static {
        weatherSummary = new HashMap<String, String>();
        weatherSummary.put("clear-day", "Clear");
        weatherSummary.put("clear-night", "Clear");
        weatherSummary.put("rain", "Rain");
        weatherSummary.put("snow", "Snow");
        weatherSummary.put("sleet", "Sleet");
        weatherSummary.put("wind", "Breezy");
        weatherSummary.put("fog", "Foggy");
        weatherSummary.put("cloudy", "Cloudy");
        weatherSummary.put("partly-cloudy-day", "Partly Cloudy");
        weatherSummary.put("partly-cloudy-night", "Partly Cloudy");
        weatherSummary.put("hail", "Hail");
        weatherSummary.put("thunderstorm", "Thunderstorm");
        weatherSummary.put("tornado", "Tornado");
    }

}