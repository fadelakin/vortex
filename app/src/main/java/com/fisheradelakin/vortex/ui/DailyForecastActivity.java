package com.fisheradelakin.vortex.ui;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fisheradelakin.vortex.R;
import com.fisheradelakin.vortex.adapters.DayAdapter;
import com.fisheradelakin.vortex.weather.Day;

import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DailyForecastActivity extends ListActivity {

    // TODO: refactor code for location

    @InjectView(R.id.dailyLayout) RelativeLayout mLayout;
    @InjectView(R.id.locationLabel) TextView mLocation;

    int color;

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
        String locality = intent.getExtras().getString("locality");

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

        mLocation.setText(locality);

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
}
