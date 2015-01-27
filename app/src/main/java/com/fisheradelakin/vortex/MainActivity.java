package com.fisheradelakin.vortex;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

// TODO: Change icon

public class MainActivity extends ActionBarActivity {

    double mLatitude;
    double mLongitude;

    public static final String TAG = MainActivity.class.getSimpleName();

    private CurrentWeather mCurrentWeather;
    private Colors mColors = new Colors();

    // Butter Knife view injections
    @InjectView(R.id.timeLabel) TextView mTimeLabel;
    @InjectView(R.id.temperatureLabel) TextView mTemperatureLabel;
    @InjectView(R.id.humidityValue) TextView mHumidityValue;
    @InjectView(R.id.precipValue) TextView mPrecipValue;
    @InjectView(R.id.summaryLabel) TextView mSummaryLabel;
    @InjectView(R.id.iconImageView) ImageView mIconImageView;
    @InjectView(R.id.locationLabel) TextView locality;
    @InjectView(R.id.parentLayout) RelativeLayout mRelativeLayout;
    @InjectView(R.id.refreshImageView) ImageView mRefreshImageView;
    @InjectView(R.id.tempVariation) TextView mTempVariation;
    @InjectView(R.id.changeTempButton) Button changeTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        start();

        changeTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeUnits();
            }
        });

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // this should fix all problems
        start();
    }

    private void start() {
        getLocation();

        // make call to server
        String apiKey = "ba71a57df25168e291029d6b1547c643";
        final String forecastUrl = "https://api.forecast.io/forecast/" + apiKey + "/" + mLatitude + "," + mLongitude;

        if(isNetworkAvailable()) {
            getForecast(forecastUrl);
        } else {
            alertUserAboutNetwork();
        }

        int color = mColors.getColor();
        mRelativeLayout.setBackgroundColor(color);
        mTempVariation.setText(getString(R.string.farenheit_string));
    }

    private void getLocation() {

        // Get Location through the network
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
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
                    locality.setText(getLocality);
                }
            } catch (IOException e) {
                Log.e(TAG, "Exception caught: ", e);
            }
        } else {
            // alert user using dialog fragment to turn on their location
            alertUserAboutLocation();
        }
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
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    Log.v(TAG, jsonData);
                    if (response.isSuccessful()) {
                        mCurrentWeather = getCurrentDetails(jsonData);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateDisplay();
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
        mTemperatureLabel.setText(mCurrentWeather.getTemperature() + "");
        mTimeLabel.setText(mCurrentWeather.getFormattedTime());
        mHumidityValue.setText(mCurrentWeather.getHumidity() + "");
        mPrecipValue.setText(mCurrentWeather.getPrecipChance() + "%");
        mSummaryLabel.setText(mCurrentWeather.getSummary());

        Drawable drawable = getResources().getDrawable(mCurrentWeather.getIconId());
        mIconImageView.setImageDrawable(drawable);
    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");

        JSONObject currently = forecast.getJSONObject("currently");

        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTimeZone(timezone);

        return currentWeather;
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

    // look into moving this to the CurrentWeather class
    private void changeToCelsius() {
        mTempVariation.setText(getString(R.string.celsius));

        double f = mCurrentWeather.getTemperature();
        int c = (int) Math.round(((f - 32) * 5) / 9);

        mTemperatureLabel.setText(c + "");
    }

    private int changeToFahrenheit() {
        mTempVariation.setText(getString(R.string.farenheit_string));
        mTemperatureLabel.setText(mCurrentWeather.getTemperature() + "");
        return mCurrentWeather.getTemperature();
    }

    private void changeUnits() {
        final CharSequence units[] = new CharSequence[] {"fahrenheit", "celsius"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a temperature unit");
        builder.setItems(units, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on colors[units]
                if(units[which].toString() == units[1]) {
                    changeToCelsius();
                } else if (units[which].toString() == units[0]) {
                    changeToFahrenheit();
                }
            }
        });
        builder.show();
    }
}
