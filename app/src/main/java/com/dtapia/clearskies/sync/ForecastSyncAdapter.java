package com.dtapia.clearskies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.util.Log;
import android.widget.Toast;

import com.dtapia.clearskies.BuildConfig;
import com.dtapia.clearskies.R;
import com.dtapia.clearskies.data.WeatherContract;
import com.dtapia.clearskies.ui.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;


public class ForecastSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = ForecastSyncAdapter.class.getSimpleName();
    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;

    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[]{
            WeatherContract.WeatherEntry.COLUMN_ICON_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
    };

    // these indices must match the projection
    private static final int INDEX_ICON_ID = 0;
    private static final int INDEX_MAX_TEMP = 0;
    private static final int INDEX_MIN_TEMP = 1;
    private static final int INDEX_SHORT_DESC = 2;

    private static double LATITUDE;
    private static double LONGITUDE;
    private static String COORDINATES = "";
    private static String CITY_NAME = "";

    // These are the names of the JSON objects that need to be extracted.
    public static final String CURRENTLY = "currently";
    private static final String TIME = "time";
    private static final String TEMPERATURE = "temperature";
    private static final String APPARENT_TEMPERATURE = "apparentTemperature";
    private static final String SUMMARY = "summary";
    private static final String PRECIPITATION = "precipProbability";
    private static final String HUMIDITY = "humidity";
    private static final String WINDSPEED = "windSpeed";
    private static final String WIND_DIRECTION = "windBearing";
    private static final String PRESSURE = "pressure";
    private static final String ICON = "icon";

    public static final String DAILY = "daily";
    private static final String TEMPERATURE_MIN = "temperatureMin";
    private static final String TEMPERATURE_MAX = "temperatureMax";
    private static final String SUNRISE = "sunriseTime";
    private static final String SUNSET = "sunsetTime";

    public static final String HOURLY = "hourly";
    private static final int HOURS = 24;

    private static final String WEATHER_ARRAY = "data";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LOCATION_STATUS_OK, LOCATION_STATUS_SERVER_DOWN, LOCATION_STATUS_SERVER_INVALID,
            LOCATION_STATUS_UNKNOWN, LOCATION_STATUS_INVALID})
    public @interface LocationStatus {}

    public static final int LOCATION_STATUS_OK = 0;
    public static final int LOCATION_STATUS_SERVER_DOWN = 1;
    public static final int LOCATION_STATUS_SERVER_INVALID = 2;
    public static final int LOCATION_STATUS_UNKNOWN = 3;
    public static final int LOCATION_STATUS_INVALID = 4;

    public ForecastSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        String locationQuery = Utility.getPreferredLocation(getContext());

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {

            final Geocoder geocoder = new Geocoder(getContext());

            List<Address> addresses = geocoder.getFromLocationName(locationQuery, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                // Use the address as needed
                String message = String.format("Latitude: %f, Longitude: %f",
                        address.getLatitude(), address.getLongitude());
                LATITUDE = address.getLatitude();
                LONGITUDE = address.getLongitude();
                COORDINATES = LATITUDE + "," + LONGITUDE;
                CITY_NAME = address.getLocality();

                Log.d(LOG_TAG, "latitude: " + LATITUDE + " longitude: " + LONGITUDE);
                if(CITY_NAME != null){
                    Utility.storeCityName(getContext(),CITY_NAME );
                }else{
                    throw new IOException(getContext().getResources().getString(R.string.city_not_found));
                }
            } else {
                // Display appropriate message when Geocoder services are not available
                Toast.makeText(getContext(), "Unable to geocode zipcode", Toast.LENGTH_LONG).show();
            }

            String excludeBlock = "minutely";

            final String FORECAST_BASE_URL =
                    "https://api.forecast.io/forecast/";
            final String EXCLUDE = "exclude";


            Uri builtUri = Uri.parse(FORECAST_BASE_URL)
                    .buildUpon()
                    .appendPath(BuildConfig.FORECAST_API_KEY)
                    .appendPath(COORDINATES)
                    .appendQueryParameter(EXCLUDE, excludeBlock)
                    .build();


            /*Uri builtUri = Uri.parse(FORECAST_BASE_URL);*/
            URL url = new URL(builtUri.toString());
            Log.d(LOG_TAG, url.toString());

            // Create the request to Forecast API, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                setLocationStatus(getContext(), LOCATION_STATUS_SERVER_DOWN);
                return;
            }
            forecastJsonStr = buffer.toString();
            getWeatherDataFromJson(forecastJsonStr, locationQuery);
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            if(e.getMessage().equals(getContext().getResources().getString(R.string.city_not_found))){
                setLocationStatus(getContext(), LOCATION_STATUS_INVALID);
            }else{
                setLocationStatus(getContext(), LOCATION_STATUS_SERVER_DOWN);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            setLocationStatus(getContext(), LOCATION_STATUS_SERVER_INVALID);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getWeatherDataFromJson(String forecastJsonStr,
                                        String locationSetting) throws JSONException {

        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.
        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONObject currentObject = forecastJson.getJSONObject(CURRENTLY);

            JSONObject hourlyObject = forecastJson.getJSONObject(HOURLY);
            JSONArray hourlyArray = hourlyObject.getJSONArray(WEATHER_ARRAY);

            JSONObject dayObject = forecastJson.getJSONObject(DAILY);
            JSONArray dayArray = dayObject.getJSONArray(WEATHER_ARRAY);

            JSONObject currentDay = dayArray.getJSONObject(0);
            long sunriseTime = currentDay.getLong(SUNRISE);
            long sunsetTime = currentDay.getLong(SUNSET);
            long locationId = addLocation(locationSetting, CITY_NAME, LATITUDE, LONGITUDE);

            Date date = new Date();
            String dateString = Utility.getFormattedDbDate(date);

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Long timeInMillis = cal.getTimeInMillis();

            //Long timeInMillis = System.currentTimeMillis();
            String dateString2 = String.valueOf(timeInMillis);

            // Insert current weather information into the database
            saveCurrentForecast(currentObject, currentDay, locationId, dateString);

            // Insert the hourly weather information into the database
            Vector<ContentValues> hourlyVector = new Vector<ContentValues>(hourlyArray.length());
            saveHourlyForecast(hourlyArray, hourlyVector, locationId, dateString);
            insertArrayData(hourlyVector, dateString2, HOURLY);


            // Insert the daily weather information into the database
            Vector<ContentValues> dayVector = new Vector<ContentValues>(dayArray.length());
            saveDailyForecast(dayArray, dayVector, locationId, dateString);
            insertArrayData(dayVector, dateString2, DAILY);

            Log.d(LOG_TAG, "Sync Complete.");
            setLocationStatus(getContext(), LOCATION_STATUS_OK);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void insertArrayData(Vector<ContentValues> vector, String dateString2, String weatherType) {
        if (vector.size() > 0) {

            /*getContext().getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI,
                    null,
                    null);*/
            ContentValues[] cvArray = new ContentValues[vector.size()];
            vector.toArray(cvArray);
            getContext().getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);

            // delete old data so we don't build up an endless history
            getContext().getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI,
                    WeatherContract.WeatherEntry.COLUMN_CURRENT_TIME + " < ? AND " +
                            WeatherContract.WeatherEntry.COLUMN_WEATHER_TYPE + " = ? ",
                    new String[]{dateString2, weatherType});
            //notifyWeather();
        }

        Log.d(LOG_TAG, "Sync Complete. " + vector.size() + " Inserted");
    }


    private void saveCurrentForecast(JSONObject currentObject, JSONObject dayObject,
                                     long locationId, String date) throws JSONException {

        long sunriseTime = dayObject.getLong(SUNRISE);
        long sunsetTime = dayObject.getLong(SUNSET);
        double highTemperature = dayObject.getDouble(TEMPERATURE_MAX);
        double lowTemperature = dayObject.getDouble(TEMPERATURE_MIN);

        long time = currentObject.getLong(TIME);
        double temperature = currentObject.getDouble(TEMPERATURE);
        double apparentTemperature = currentObject.getDouble(APPARENT_TEMPERATURE);
        String currentSummary = currentObject.getString(SUMMARY);
        double pressure = currentObject.getDouble(PRESSURE);
        double precipitation = currentObject.getDouble(PRECIPITATION);
        double currentHumidity = currentObject.getDouble(HUMIDITY);
        double currentWindspeed = currentObject.getDouble(WINDSPEED);
        double currentWindDirection = currentObject.getDouble(WIND_DIRECTION);
        String iconId = currentObject.getString(ICON);

        ContentValues weatherValues = new ContentValues();

        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_CURRENT_TEMP, temperature);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_APPARENT_TEMP, apparentTemperature);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, highTemperature);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, lowTemperature);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, currentSummary);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRECIPITATION, precipitation);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, currentHumidity);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, currentWindspeed);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, currentWindDirection);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_ICON_ID, iconId);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_CURRENT_TIME, time);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SUNRISE_TIME, sunriseTime);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SUNSET_TIME, sunsetTime);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_TYPE, CURRENTLY);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_INSERT_DATE, date);

        insertOrUpdateCurrentData(weatherValues, locationId);

        Log.d(LOG_TAG, "Sync Complete. Current data inserted");
        setLocationStatus(getContext(), LOCATION_STATUS_OK);
    }

    private void insertOrUpdateCurrentData(ContentValues weatherValues, Long locationId) {
        Cursor currentCursor = getContext().getContentResolver().query(
                WeatherContract.WeatherEntry.CONTENT_URI,
                null,
                WeatherContract.WeatherEntry.COLUMN_WEATHER_TYPE + " = ? AND " +
                WeatherContract.WeatherEntry.COLUMN_LOC_KEY + " = ?",
                new String[]{CURRENTLY, String.valueOf(locationId)},
                null);

        if (currentCursor.moveToFirst()) {
            getContext().getContentResolver().update(
                    WeatherContract.WeatherEntry.CONTENT_URI,
                    weatherValues,
                    WeatherContract.WeatherEntry.COLUMN_WEATHER_TYPE + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_LOC_KEY + " = ?",
                    new String[]{CURRENTLY, String.valueOf(locationId)});

        }else{
            getContext().getContentResolver().insert(
                    WeatherContract.WeatherEntry.CONTENT_URI,
                    weatherValues);
        }
    }


    private void saveHourlyForecast(JSONArray hours, Vector<ContentValues> hVector,
                                    long locationId, String date) throws JSONException {

        for(int i = 0; i <= HOURS; i++) {
            // These are the values that will be collected.
            long time;
            String hourlySummary;
            String icon;
            Double temperature;

            // Get the JSON object representing the hour
            JSONObject hourForecast = hours.getJSONObject(i);

            time = hourForecast.getLong(TIME);
            hourlySummary = hourForecast.getString(SUMMARY);
            icon = hourForecast.getString(ICON);
            temperature = hourForecast.getDouble(TEMPERATURE);

            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_CURRENT_TIME, time);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, hourlySummary);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_ICON_ID, icon);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_CURRENT_TEMP, temperature);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_TYPE, HOURLY);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_INSERT_DATE, date);

            hVector.add(weatherValues);
        }

    }

    private void saveDailyForecast(JSONArray days, Vector<ContentValues> dVector,
                                   long locationId, String date) throws JSONException {

        for(int i = 1; i < days.length(); i++) {
            // These are the values that will be collected.
            long dateTime;
            double pressure;
            double windDirection;
            String dailySummary;
            String dayIcon;
            Double dayTemperatureMin;
            Double dayTemperatureMax;
            double precipitation;
            double humidity;
            double windSpeed;
            long sunriseTime;
            long sunsetTime;

            // Get the JSON object representing the day
            JSONObject dayForecast = days.getJSONObject(i);

            dateTime = dayForecast.getLong(TIME);
            dailySummary = dayForecast.getString(SUMMARY);
            dayIcon = dayForecast.getString(ICON);
            dayTemperatureMin = dayForecast.getDouble(TEMPERATURE_MIN);
            dayTemperatureMax = dayForecast.getDouble(TEMPERATURE_MAX);
            precipitation = dayForecast.getDouble(PRECIPITATION);
            humidity = dayForecast.getDouble(HUMIDITY);
            windSpeed = dayForecast.getDouble(WINDSPEED);
            windDirection = dayForecast.getDouble(WIND_DIRECTION);
            sunriseTime = dayForecast.getLong(SUNRISE);
            sunsetTime = dayForecast.getLong(SUNSET);
            pressure = dayForecast.getDouble(PRESSURE);

            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_CURRENT_TIME, dateTime);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRECIPITATION, precipitation);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, dayTemperatureMax);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, dayTemperatureMin);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SUNRISE_TIME, sunriseTime);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SUNSET_TIME, sunsetTime);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, dailySummary);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_ICON_ID, dayIcon);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_TYPE, DAILY);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_INSERT_DATE, date);

            dVector.add(weatherValues);
        }

    }


    /*private void notifyWeather() {
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if ( displayNotifications ) {

            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);

            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
                // Last sync was more than 1 day ago, let's send a notification with the weather.
                String locationQuery = Utility.getPreferredLocation(context);

                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());

                // we'll query our contentProvider, as always
                Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

                if (cursor.moveToFirst()) {
                    String icon = cursor.getString(INDEX_ICON_ID);
                    double high = cursor.getDouble(INDEX_MAX_TEMP);
                    double low = cursor.getDouble(INDEX_MIN_TEMP);
                    String desc = cursor.getString(INDEX_SHORT_DESC);

                    //int iconId = Utility.getIconResourceForWeatherCondition(icon);
                    Resources resources = context.getResources();
                    *//*Bitmap largeIcon = BitmapFactory.decodeResource(resources,
                            Utility.getArtResourceForWeatherCondition(icon));*//*
                    String title = context.getString(R.string.app_name);

                    // Define the text of the forecast.
                    String contentText = String.format(context.getString(R.string.format_notification),
                            desc,
                            Utility.formatTemperature(context, high),
                            Utility.formatTemperature(context, low));

                    // NotificationCompatBuilder is a very convenient way to build backward-compatible
                    // notifications.  Just throw in some data.
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getContext())
                                    .setColor(resources.getColor(R.color.sunshine_light_blue))
                                    .setSmallIcon(0)
                                    //.setLargeIcon(largeIcon)
                                    .setContentTitle(title)
                                    .setContentText(contentText);

                    // Make something interesting happen when the user clicks on the notification.
                    // In this case, opening the app is sufficient.
                    Intent resultIntent = new Intent(context, MainActivity.class);

                    // The stack builder object will contain an artificial back stack for the
                    // started Activity.
                    // This ensures that navigating backward from the Activity leads out of
                    // your application to the Home screen.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager =
                            (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    // WEATHER_NOTIFICATION_ID allows you to update the notification later on.
                    mNotificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());

                    //refreshing last sync
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(lastNotificationKey, System.currentTimeMillis());
                    editor.commit();
                }
                cursor.close();
            }
        }
    }*/

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param cityName A human-readable city name, e.g "Mountain View"
     * @param lat the latitude of the city
     * @param lon the longitude of the city
     * @return the row ID of the added location.
     */
    long addLocation(String locationSetting, String cityName, double lat, double lon) {
        long locationId;

        // First, check if the location with this city name exists in the db
        Cursor locationCursor = getContext().getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null);

        if (locationCursor.moveToFirst()) {
            int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = locationCursor.getLong(locationIdIndex);
        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues locationValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

            // Finally, insert location data into the database.
            Uri insertedUri = getContext().getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    locationValues
            );

            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            locationId = ContentUris.parseId(insertedUri);
        }

        locationCursor.close();
        // Wait, that worked?  Yes!
        return locationId;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Log.d("LOGTAG", "Syncing immediately");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        ForecastSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    /**
     * Sets the location status into shared preference.  This function should not be called from
     * the UI thread because it uses commit to write to the shared preferences.
     * @param c Context to get the PreferenceManager from.
     * @param locationStatus The IntDef value to set
     */
    static private void setLocationStatus(Context c, @LocationStatus int locationStatus){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_location_status_key), locationStatus);
        spe.commit();
    }
}