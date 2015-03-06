package com.fisheradelakin.vortex.ui;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fisheradelakin.vortex.R;
import com.fisheradelakin.vortex.adapters.DayAdapter;
import com.fisheradelakin.vortex.weather.Day;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DailyForecastActivity extends ListActivity {

    // TODO: refactor code for location

    @InjectView(R.id.dailyLayout) RelativeLayout mLayout;
    @InjectView(R.id.locationLabel) TextView mLocation;

    int color;

    double mLatitude;
    double mLongitude;

    private Day[] mDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);
        ButterKnife.inject(this);

        /* BACKGROUND COLOR STUFF */
        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST);
        color = intent.getExtras().getInt("background");

        if(color != -1 && mLayout != null) {
            mLayout.setBackgroundColor(color);
            Log.i("TAG", color + "");
        } else {
            Toast.makeText(this, "something went wrong and is null", Toast.LENGTH_SHORT).show();
            Log.i("TAG", color + "");
        }

        changeStatusBarColor();
        /* END BACKGROUND COLOR STUFF */

        // get array of items from parcelable extra
        mDays = Arrays.copyOf(parcelables, parcelables.length, Day[].class);

        DayAdapter adapter = new DayAdapter(this, mDays);
        setListAdapter(adapter);

        getLocation();



    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void changeStatusBarColor() {
        // generate a new color based on the background color for the status bar
        // using hsv because it makes it super easy bruh.
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.75f; // value component
        int statusBarColor = Color.HSVToColor(hsv);

        // set status bar to a darker color of the background
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            w.setStatusBarColor(statusBarColor);
        }
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
                    mLocation.setText(getLocality);
                }
            } catch (IOException e) {
                Log.e("EXCEPTION", "Exception caught: ", e);
            }
        } else {
            // alert user using dialog fragment to turn on their location
            alertUserAboutLocation();
        }
    }

    private void alertUserAboutLocation() {
        LocationDialogFragment dialogFragment = new LocationDialogFragment();
        dialogFragment.show(getFragmentManager(), "location_dialog");
    }
}
