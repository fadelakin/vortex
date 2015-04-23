package com.fisheradelakin.vortex.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fisheradelakin.vortex.R;
import com.fisheradelakin.vortex.adapters.HourAdapter;
import com.fisheradelakin.vortex.weather.Hour;

import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;
import jp.wasabeef.recyclerview.animators.adapters.SlideInRightAnimationAdapter;

public class HourlyForecastActivity extends AppCompatActivity {

    private Hour[] mHours;

    int color;

    @InjectView(R.id.recyclerView) RecyclerView mRecyclerView;
    @InjectView(R.id.hourlyLayout) RelativeLayout mHourlyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hourly_forecast);
        ButterKnife.inject(this);

        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.HOURLY_FORECAST);
        mHours = Arrays.copyOf(parcelables, parcelables.length, Hour[].class);

        HourAdapter adapter = new HourAdapter(this, mHours);
        SlideInRightAnimationAdapter slideInRightAnimationAdapter = new SlideInRightAnimationAdapter(adapter);
        slideInRightAnimationAdapter.setDuration(750);
        mRecyclerView.setAdapter(slideInRightAnimationAdapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);

        /* Background and status bar color */
        color = intent.getExtras().getInt("background");
        if(color != -1 && mHourlyLayout != null) {
            mHourlyLayout.setBackgroundColor(color);
        } else {
            Toast.makeText(this, "whoops. something went wrong", Toast.LENGTH_SHORT).show();
        }

        changeStatusBarColor();
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
