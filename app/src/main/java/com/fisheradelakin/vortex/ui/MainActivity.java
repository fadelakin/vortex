package com.fisheradelakin.vortex.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fisheradelakin.vortex.R;
import com.fisheradelakin.vortex.utils.Colors;
import com.fisheradelakin.vortex.weather.Current;
import com.fisheradelakin.vortex.weather.Day;
import com.fisheradelakin.vortex.weather.Forecast;
import com.fisheradelakin.vortex.weather.Hour;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static android.view.WindowManager.LayoutParams;

public class MainActivity extends AppCompatActivity {

    String apiKey;

    double mLatitude;
    double mLongitude;

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String DAILY_FORECAST = "DAILY_FORECAST";
    public static final String HOURLY_FORECAST = "HOURLY_FORECAST";

    private Forecast mForecast;
    private Colors mColors = new Colors();

    // Butter Knife view injections
    @InjectView(R.id.timeLabel) TextView mTimeLabel;
    @InjectView(R.id.temperatureLabel) TextView mTemperatureLabel;
    @InjectView(R.id.humidityValue) TextView mHumidityValue;
    @InjectView(R.id.precipValue) TextView mPrecipValue;
    @InjectView(R.id.summaryLabel) TextView mSummaryLabel;
    @InjectView(R.id.iconImageView) ImageView mIconImageView;
    @InjectView(R.id.locationLabel) TextView mLocality;
    @InjectView(R.id.parentLayout) RelativeLayout mRelativeLayout;
    @InjectView(R.id.refreshImageView) ImageView mRefreshImageView;
    @InjectView(R.id.tempVariation) TextView mTempVariation;
    @InjectView(R.id.activity_main_swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @InjectView(R.id.changeUnits) ImageButton mChangeUnits;

    public static final String MyPREFERENCES = "MyPrefs";
    public static final String fKey = "F";
    public static final String cKey = "C";

    SharedPreferences mSharedPreferences;

    int color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        try {
            start();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        mChangeUnits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeUnits();
            }
        });

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    start();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    start();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // this should fix all problems
        try {
            start();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void getPreferences() {
        mSharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);

        if (mSharedPreferences.contains(fKey))
        {
            changeToFahrenheit();
        }
        if (mSharedPreferences.contains(cKey))
        {
            changeToCelsius();
        }
    }

    private String getAPIKey() throws IOException, JSONException {
        String jsonData = loadJSONFromAsset();
        JSONObject key = new JSONObject(jsonData);
        apiKey = key.getString("api_key");
        return apiKey;
    }

    public String loadJSONFromAsset() {
        String json;
        try {
            AssetManager mngr = this.getAssets();
            InputStream is = mngr.open("apikey.json");
            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }

    private void start() throws IOException, JSONException {
        getAPIKey();
        getLocation();

        // make call to server
        // get your own API KEY from developer.forecast.io and fill it in.
        final String forecastUrl = "https://api.forecast.io/forecast/" + apiKey + "/" + mLatitude + "," + mLongitude;

        if(isNetworkAvailable()) {
            getForecast(forecastUrl);
        } else {
            alertUserAboutNetwork();
        }

        color = mColors.getColor();
        mRelativeLayout.setBackgroundColor(color);
        mTempVariation.setText(getString(R.string.farenheit_string));

        // generate a new color based on the background color for the status bar
        // using hsv because it makes it super easy bruh.
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.75f; // value component
        int statusBarColor = Color.HSVToColor(hsv);

        // set status bar to a darker color of the background
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow();
            w.addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            w.clearFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);
            w.setStatusBarColor(statusBarColor);
        }
    }

    private void getLocation() {

        // Get Location through the network
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false, network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!gps_enabled && !network_enabled) {
            alertUserAboutLocation();
        } else {
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if(location != null) {
                        mLatitude = location.getLatitude();
                        mLongitude = location.getLongitude();

                        float accuracyf = location.getAccuracy();
                        String providerShown = location.getProvider();
                        Log.i(TAG, "Provider: " + providerShown + ", Accuracy: " + accuracyf);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            String provider = lm.getBestProvider(criteria, true);
            Location mostRecentLocation = lm.getLastKnownLocation(provider);
            if (mostRecentLocation != null) {
                mLatitude = mostRecentLocation.getLatitude();
                mLongitude = mostRecentLocation.getLongitude();
            }

            lm.requestLocationUpdates(provider, 2000, 10, locationListener);
        }

        // Get current city that user is in.
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(mLatitude, mLongitude, 1);
            if (addresses.size() > 0) {
                // change location label to user's current location
                String getLocality = addresses.get(0).getLocality();
                mLocality.setText(getLocality);
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception caught: ", e);
        }

        /*boolean isEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if(isEnabled) {
            Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();

            // Async updates of location through Network
            LocationListener locationListenerNetwork = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mLatitude = location.getLatitude();
                    mLongitude = location.getLongitude();
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, locationListenerNetwork);

            // Get current city that user is in.
            Geocoder gcd = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = gcd.getFromLocation(mLatitude, mLongitude, 1);
                if (addresses.size() > 0) {
                    // change location label to user's current location
                    String getLocality = addresses.get(0).getLocality();
                    mLocality.setText(getLocality);
                }
            } catch (IOException e) {
                Log.e(TAG, "Exception caught: ", e);
            }
        } else {
            // alert user using dialog fragment to turn on their location
            alertUserAboutLocation();
        }*/
    }

    private void getForecast(String url) {
        // OkHttp stuff
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                alertUserAboutError();
                Log.e(TAG, "Exception caught", e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    Log.v(TAG, jsonData);
                    if (response.isSuccessful()) {
                        mForecast = parseForecastDetails(jsonData);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateDisplay();
                                mSwipeRefreshLayout.setRefreshing(false);
                                getPreferences();
                            }
                        });
                    } else {
                        alertUserAboutError();
                    }
                } catch (IOException | JSONException e) {
                    Log.e(TAG, "Exception caught: ", e);
                }
            }
        });
    }

    private void updateDisplay() {
        Current current = mForecast.getCurrent();
        mTemperatureLabel.setText(current.getTemperature() + "");
        mTimeLabel.setText(current.getFormattedTime());
        mHumidityValue.setText(current.getHumidity() + "");
        mPrecipValue.setText(current.getPrecipChance() + "%");
        mSummaryLabel.setText(current.getSummary());

        Drawable drawable = getResources().getDrawable(current.getIconId());
        mIconImageView.setImageDrawable(drawable);
    }

    private Forecast parseForecastDetails(String jsonData) throws JSONException {
        Forecast forecast = new Forecast();

        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setHourlyForecast(getHourlyForecast(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));

        return forecast;
    }

    private Day[] getDailyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");

        Day[] days = new Day[data.length()];

        for(int i = 0; i < data.length(); i++) {
            JSONObject jsonDay = data.getJSONObject(i);
            Day day = new Day();

            day.setSummary(jsonDay.getString("summary"));
            day.setIcon(jsonDay.getString("icon"));
            day.setTemperatureMax(jsonDay.getDouble("temperatureMax"));
            day.setTime(jsonDay.getLong("time"));
            day.setTimeZone(timezone);

            days[i] = day;
        }

        return days;
    }

    private Hour[] getHourlyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");

        Hour[] hours = new Hour[data.length()];

        for(int i = 0; i < data.length(); i++) {
            JSONObject jsonHour = data.getJSONObject(i);
            Hour hour = new Hour();

            hour.setSummary(jsonHour.getString("summary"));
            hour.setTemperature(jsonHour.getDouble("temperature"));
            hour.setIcon(jsonHour.getString("icon"));
            hour.setTime(jsonHour.getLong("time"));
            hour.setTimeZone(timezone);

            hours[i] = hour;
        }

        return hours;
    }

    private Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");

        JSONObject currently = forecast.getJSONObject("currently");

        Current current = new Current();
        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setIcon(currently.getString("icon"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setSummary(currently.getString("summary"));
        current.setTemperature(currently.getDouble("temperature"));
        current.setTimeZone(timezone);

        return current;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

    private void alertUserAboutLocation() {
        LocationDialogFragment dialogFragment = new LocationDialogFragment();
        dialogFragment.show(getFragmentManager(), "location_dialog");
    }

    private void alertUserAboutNetwork() {
        NetworkDialogFragment networkDialogFragment = new NetworkDialogFragment();
        networkDialogFragment.show(getFragmentManager(), "network_dialog");
    }

    // TODO: look into moving this to the Current class
    // TODO: pass celsius values to daily and hourly screens
    private void changeToCelsius() {
        Current current = mForecast.getCurrent();
        mTempVariation.setText(getString(R.string.celsius));
        mTemperatureLabel.setText((((current.getTemperature() - 32) * 5) / 9) + "");
    }

    private int changeToFahrenheit() {
        Current current = mForecast.getCurrent();
        mTempVariation.setText(getString(R.string.farenheit_string));
        mTemperatureLabel.setText(current.getTemperature() + "");
        return current.getTemperature();
    }

    // TODO: shared preferences into daily and hourly views

    private void changeUnits() {
        final CharSequence units[] = new CharSequence[] {"Fahrenheit", "Celsius"};

        final SharedPreferences.Editor editor = mSharedPreferences.edit();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a temperature unit");
        builder.setItems(units, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on colors[units]
                if(units[which].toString() == units[1]) {
                    changeToCelsius();
                    editor.clear();
                    editor.putString(cKey, units[1].toString());
                    editor.apply();
                } else if (units[which].toString() == units[0]) {
                    changeToFahrenheit();
                    editor.clear();
                    editor.putString(fKey, units[0].toString());
                    editor.apply();
                }
            }
        });
        editor.apply();
        builder.show();
    }

    @OnClick (R.id.dailyButton)
    public void startDailyActivity(View view) {
        Intent intent = new Intent(this, DailyForecastActivity.class);
        intent.putExtra(DAILY_FORECAST, mForecast.getDailyForecast());
        intent.putExtra("background", color);
        intent.putExtra("locality", mLocality.getText().toString());
        startActivity(intent);
    }

    @OnClick (R.id.hourlyButton)
    public void startHourlyActivity(View view) {
        Intent intent = new Intent(this, HourlyForecastActivity.class);
        intent.putExtra(HOURLY_FORECAST, mForecast.getHourlyForecast());
        intent.putExtra("background", color);
        startActivity(intent);
    }
}
