package com.londonappbrewery.clima_completed;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;



public class WeatherController extends AppCompatActivity {

    // Request Codes:
    final int REQUEST_CODE = 123; // Request Code for permission request callback
    final int NEW_CITY_CODE = 456; // Request code for starting new activity for result callback

    // Base URL for the OpenWeatherMap API. More secure https is a premium feature =(
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";

    // App ID to use OpenWeather data
    final String APP_ID = "e72ca729af228beabd5d20e3b7749713";

    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;

    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // Don't want to type 'Clima' in all the logs, so putting this in a constant here.
    final String LOGCAT_TAG = "Clima";

    // Set LOCATION_PROVIDER here. Using GPS_Provider for Fine Location (good for emulator):
    // Recommend using LocationManager.NETWORK_PROVIDER on physical devices (reliable & fast!)
    final String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;

    // Member Variables:
    boolean mUseLocation = true;
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;
    TextView mTimeLabel;
    long timeFromComp;
    int timesCityChanged = 0;
    int startUpCount = 0;
    Timer timer;
    TimerTask timerTask;
    double lonOfCity;
    double lonOfCity1;
    double lonOfCity2;
    double lonOfCity3;

    TextView mCityLabel1;
    ImageView mWeatherImage1;
    TextView mTemperatureLabel1;
    TextView mTimeLabel1;

    TextView mCityLabel2;
    ImageView mWeatherImage2;
    TextView mTemperatureLabel2;
    TextView mTimeLabel2;

    TextView mCityLabel3;
    ImageView mWeatherImage3;
    TextView mTemperatureLabel3;
    TextView mTimeLabel3;

    // Declaring a LocationManager and a LocationListener here:
    LocationManager mLocationManager;
    LocationListener mLocationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code.
        // API 26 and above does not require casting anymore.
        // Can write: mCityLabel = findViewById(R.id.locationTV);
        // Instead of: mCityLabel = (TextView) findViewById(R.id.locationTV);

        mCityLabel = findViewById(R.id.locationTV);
        mWeatherImage = findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = findViewById(R.id.tempTV);
        mTimeLabel = findViewById(R.id.timeTV);
        ImageButton changeCityButton = findViewById(R.id.changeCityButton);

        mCityLabel1 = findViewById(R.id.locationTV1);
        mWeatherImage1= findViewById(R.id.weatherSymbolIV1);
        mTemperatureLabel1 = findViewById(R.id.tempTV1);
        mTimeLabel1 = findViewById(R.id.timeTV1);

        mCityLabel2 = findViewById(R.id.locationTV2);
        mWeatherImage2 = findViewById(R.id.weatherSymbolIV2);
        mTemperatureLabel2 = findViewById(R.id.tempTV2);
        mTimeLabel2 = findViewById(R.id.timeTV2);

        mCityLabel3 = findViewById(R.id.locationTV3);
        mWeatherImage3 = findViewById(R.id.weatherSymbolIV3);
        mTemperatureLabel3 = findViewById(R.id.tempTV3);
        mTimeLabel3 = findViewById(R.id.timeTV3);



        // Add an OnClickListener to the changeCityButton here:
        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(WeatherController.this, ChangeCityController.class);

                // Using startActivityForResult since we just get back the city name.
                // Providing an arbitrary request code to check against later.
                startActivityForResult(myIntent, NEW_CITY_CODE);
            }
        });
    }


    // onResume() life cycle callback:
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOGCAT_TAG, "onResume() called");
        if(mUseLocation) getWeatherForCurrentLocation();
    }

    // Callback received when a new city name is entered on the second screen.
    // Checking request code and if result is OK before making the API call.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(LOGCAT_TAG, "onActivityResult() called");

        if (requestCode == NEW_CITY_CODE) {
            if (resultCode == RESULT_OK) {
                String city = data.getStringExtra("City");
                Log.d(LOGCAT_TAG, "New city is " + city);

                mUseLocation = false;
                getWeatherForNewCity(city);
            }
        }
    }

    // Configuring the parameters when a new city has been entered:
    private void getWeatherForNewCity(String city) {
        Log.d(LOGCAT_TAG, "Getting weather for new city");
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);

        letsDoSomeNetworking(params);
    }


    // Location Listener callbacks here, when the location has changed.
    private void getWeatherForCurrentLocation() {

        Log.d(LOGCAT_TAG, "Getting weather for current location");
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Log.d(LOGCAT_TAG, "onLocationChanged() callback received");
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());

                // Gets time from the computer for later use
                timeFromComp = location.getTime();


                Log.d(LOGCAT_TAG, "longitude is: " + longitude);
                Log.d(LOGCAT_TAG, "latitude is: " + latitude);

                // Providing 'lat' and 'lon' (spelling: Not 'long') parameter values
                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", APP_ID);



                letsDoSomeNetworking(params);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // Log statements to help you debug your app.
                Log.d(LOGCAT_TAG, "onStatusChanged() callback received. Status: " + status);
                Log.d(LOGCAT_TAG, "2 means AVAILABLE, 1: TEMPORARILY_UNAVAILABLE, 0: OUT_OF_SERVICE");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d(LOGCAT_TAG, "onProviderEnabled() callback received. Provider: " + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(LOGCAT_TAG, "onProviderDisabled() callback received. Provider: " + provider);
            }
        };

        // This is the permission check to access (fine) location.

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }

        // Some additional log statements to help you debug
        Log.d(LOGCAT_TAG, "Location Provider used: "
                + mLocationManager.getProvider(LOCATION_PROVIDER).getName());
        Log.d(LOGCAT_TAG, "Location Provider is enabled: "
                + mLocationManager.isProviderEnabled(LOCATION_PROVIDER));
        Log.d(LOGCAT_TAG, "Last known location (if any): "
                + mLocationManager.getLastKnownLocation(LOCATION_PROVIDER));
        Log.d(LOGCAT_TAG, "Requesting location updates");


        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);

    }

    // This is the callback that's received when the permission is granted (or denied)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Checking against the request code we specified earlier.
        if (requestCode == REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(LOGCAT_TAG, "onRequestPermissionsResult(): Permission granted!");

                // Getting weather only if we were granted permission.
                getWeatherForCurrentLocation();
            } else {
                Log.d(LOGCAT_TAG, "Permission denied =( ");
            }
        }

    }


    // This is the actual networking code. Parameters are already configured.
    private void letsDoSomeNetworking(RequestParams params) {

        // AsyncHttpClient belongs to the loopj dependency.
        AsyncHttpClient client = new AsyncHttpClient();

        // Making an HTTP GET request by providing a URL and the parameters.
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Log.d(LOGCAT_TAG, "Success! JSON: " + response.toString());
                WeatherDataModel weatherData = WeatherDataModel.fromJson(response);
                updateUI(weatherData);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {

                Log.e(LOGCAT_TAG, "Fail " + e.toString());
                Toast.makeText(WeatherController.this, "Request Failed", Toast.LENGTH_SHORT).show();

                Log.d(LOGCAT_TAG, "Status code " + statusCode);
                Log.d(LOGCAT_TAG, "Here's what we got instead " + response.toString());
            }

        });
    }

    // Updates the Local time
    private String setTime(Double lon,Long time){
        SimpleDateFormat timeAtLoc = new SimpleDateFormat("hh:mm a");

        // Check to see if the computer time is past 9:30
        // If past 9:30 don't add time to get to 0 longitude
        if(time < 77400000){
            // Sets time to the time at 0 longitude
            time += 12600000;

            // Sets time zone for each location
            int timeZoneNum = (int) (lon / 7.5);

            // Checks to see if we add or subtract time zone
            if(timeZoneNum < 0){
                time = time + (timeZoneNum * 1800000);
            }
            else{
                time = time - (timeZoneNum * 1800000);
            }

            // Sets the new time to a readable format
            String timeZone = timeAtLoc.format(time);
            startTimer();
            return timeZone;
        }
        else{
            // Sets time zone for each location
            int timeZoneNum = (int) (lon / 7.5);

            // Checks to see if we add or subtract time zone
            if(timeZoneNum < 0){
                time = time + (timeZoneNum * 1800000);
            }
            else{
                time = time - (timeZoneNum * 1800000);
            }

            // Sets the new time to a readable format
            String timeZone = timeAtLoc.format(time);
            startTimer();
            return timeZone;
        }
    }


    // Updates the information shown on screen.
    private void updateUI(WeatherDataModel weather) {
        // Check to see which labels to change
        if(timesCityChanged == 0){
            // Sets each Label with appropriate text
            mTemperatureLabel.setText(weather.getTemperature());
            mCityLabel.setText(weather.getCity());
            // Update the icon based on the resource id of the image in the drawable folder.
            int resourceID = getResources().getIdentifier(weather.getIconName(), "drawable", getPackageName());
            mWeatherImage.setImageResource(resourceID);

            // Sets the longitude for the timer function
            lonOfCity = weather.getLongitude();
            mTimeLabel.setText(setTime(weather.getLongitude(), timeFromComp));

            // Sets the variable to check which label to change
            timesCityChanged = 1;
            // Checks to see if the app just started
            if(startUpCount == 0 ){
                getWeatherForNewCity("Corner Brook");
            }

        }
        // Check to see which labels to change
        else if(timesCityChanged == 1){
            // Sets each Label with appropriate text
            mTemperatureLabel1.setText(weather.getTemperature());
            mCityLabel1.setText(weather.getCity());
            // Update the icon based on the resource id of the image in the drawable folder.
            int resourceID = getResources().getIdentifier(weather.getIconName(), "drawable", getPackageName());
            mWeatherImage1.setImageResource(resourceID);

            // Sets the longitude for the timer function
            lonOfCity1 = weather.getLongitude();
            mTimeLabel1.setText(setTime(weather.getLongitude(), timeFromComp));

            // Sets the variable to check which label to change
            timesCityChanged = 2;
            // Checks to see if the app just started
            if(startUpCount == 0){
                getWeatherForNewCity("Brampton");
            }

        }
        // Check to see which labels to change
        else if(timesCityChanged == 2){
            // Sets each Label with appropriate text
            mTemperatureLabel2.setText(weather.getTemperature());
            mCityLabel2.setText(weather.getCity());
            // Update the icon based on the resource id of the image in the drawable folder.
            int resourceID = getResources().getIdentifier(weather.getIconName(), "drawable", getPackageName());
            mWeatherImage2.setImageResource(resourceID);

            // Sets the longitude for the timer function
            lonOfCity2 = weather.getLongitude();
            mTimeLabel2.setText(setTime(weather.getLongitude(), timeFromComp));

            // Sets the variable to check which label to change
            timesCityChanged = 3;
            // Checks to see if the app just started
            if(startUpCount == 0){
                getWeatherForNewCity("London");
                startUpCount += 1;
            }

        }
        // Check to see which labels to change
        else if(timesCityChanged == 3){
            // Sets each Label with appropriate text
            mTemperatureLabel3.setText(weather.getTemperature());
            mCityLabel3.setText(weather.getCity());
            // Update the icon based on the resource id of the image in the drawable folder.
            int resourceID = getResources().getIdentifier(weather.getIconName(), "drawable", getPackageName());
            mWeatherImage3.setImageResource(resourceID);

            // Sets the longitude for the timer function
            lonOfCity3 = weather.getLongitude();
            mTimeLabel3.setText(setTime(weather.getLongitude(), timeFromComp));

            // Sets the variable to check which label to change
            timesCityChanged = 1;
        }

    }

    // Freeing up resources when the app enters the paused state.
    @Override
    protected void onPause() {
        super.onPause();

        if (mLocationManager != null) mLocationManager.removeUpdates(mLocationListener);
    }

    public void startTimer(){
        // Set a new Timer
        timer = new Timer();

        // Initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 600000ms the TimerTask will run every 600000ms
        timer.schedule(timerTask, 60000, 60000);

    }

    public void initializeTimerTask(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                // Adds a 1/4 of a minute to the time from the computer because it gets called 4 times
                timeFromComp += 15000;
                // Calls to change the time fields
                mTimeLabel.setText(setTime(lonOfCity,timeFromComp));
                mTimeLabel1.setText(setTime(lonOfCity1,timeFromComp));
                mTimeLabel2.setText(setTime(lonOfCity2,timeFromComp));
                mTimeLabel3.setText(setTime(lonOfCity3,timeFromComp));


            }
        };
    }




    }











