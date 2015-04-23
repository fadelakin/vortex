package com.fisheradelakin.vortex.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
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

    @InjectView(R.id.dailyLayout) RelativeLayout mLayout;
    @InjectView(R.id.locationLabel) TextView mLocation;

    int color;

    private Day[] mDays;
    private SharedPreferences mSharedPreferences;
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String fKey = "F";
    public static final String cKey = "C";

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
        } else {
            Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show();
        }

        mSharedPreferences = getSharedPreferences(MyPREFERENCES, 0);

        changeStatusBarColor();
        /* END BACKGROUND COLOR STUFF */

        // get array of items from parcelable extra
        mDays = Arrays.copyOf(parcelables, parcelables.length, Day[].class);

        DayAdapter adapter = new DayAdapter(this, mDays);
        setListAdapter(adapter);

        mLocation.setText(locality);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String dayOfTheWeek = mDays[position].getDayOfTheWeek();
        String conditions = mDays[position].getSummary();
        String highTemp = "";
        if(mSharedPreferences.contains(cKey)) {
            //mTemperatureLabel.setText((((hour.getTemperature() - 32) * 5) / 9) + "");
            highTemp = (((mDays[position].getTemperatureMax() -32) * 5) / 9) + "";
        } else if(mSharedPreferences.contains(fKey)) {
            //mTemperatureLabel.setText(hour.getTemperature() + "");
            highTemp = mDays[position].getTemperatureMax() + "";
        }
        String message = String.format("On %s the high will be %s and it will be %s", dayOfTheWeek, highTemp, conditions);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    public void changeStatusBarColor() {
        // generate a new color based on the background color for the status bar
        // using hsv because it makes it super easy bruh.
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.75f; // value component
        int statusBarColor = Color.HSVToColor(hsv);

        // set status bar to a darker color of the background
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow();
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            w.setStatusBarColor(statusBarColor);
        }
    }
}
