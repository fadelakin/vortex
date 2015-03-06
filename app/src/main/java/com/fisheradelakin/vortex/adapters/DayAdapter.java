package com.fisheradelakin.vortex.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fisheradelakin.vortex.R;
import com.fisheradelakin.vortex.weather.Day;

/**
 * Created by Fisher on 3/5/15.
 */
public class DayAdapter extends BaseAdapter {

    private Context mContext;
    private Day[] mDays;

    public static final String MyPREFERENCES = "MyPrefs";
    public static final String fKey = "F";
    public static final String cKey = "C";

    Day day;

    public DayAdapter(Context context, Day[] days) {
        mContext = context;
        mDays = days;
    }

    @Override
    public int getCount() {
        return mDays.length;
    }

    @Override
    public Object getItem(int position) {
        return mDays[position];
    }

    @Override
    public long getItemId(int position) {
        return 0; // not using this. used to tag items for easy reference
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            // brand new view
            convertView = LayoutInflater.from(mContext).inflate(R.layout.daily_list_item, null);
            holder = new ViewHolder();
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.iconImageView);
            holder.temperatureLabel = (TextView) convertView.findViewById(R.id.temperatureLabel);
            holder.dayLabel = (TextView) convertView.findViewById(R.id.dayNameLabel);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        day = mDays[position];

        holder.iconImageView.setImageResource(day.getIconId());
        holder.temperatureLabel.setText(day.getTemperatureMax() + "");

        if(position == 0) {
            holder.dayLabel.setText("Today");
        } else {
            holder.dayLabel.setText(day.getDayOfTheWeek());
        }

        SharedPreferences temps = mContext.getSharedPreferences(MyPREFERENCES, 0);
        if(temps.contains(cKey)) {
            holder.temperatureLabel.setText(changeToCelsius() + "");
        } else if(temps.contains(fKey)) {
            holder.temperatureLabel.setText(changeToFahrenheit() + "");
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageView iconImageView;
        TextView temperatureLabel;
        TextView dayLabel;
    }

    private int changeToCelsius() {
        return ((((day.getTemperatureMax() - 32) * 5) / 9));
    }

    private int changeToFahrenheit() {
        return day.getTemperatureMax();
    }
}
