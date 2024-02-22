package com.mlprograms.rechenmax;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<CustomItems> {

    private int defaultTextColor = Color.BLACK;
    private int dropdownBackgroundColor = Color.WHITE;

    public CustomAdapter(@NonNull Context context, ArrayList<CustomItems> customList) {
        super(context, 0, customList);
    }

    public void setTextColor(int color) {
        defaultTextColor = color;
    }

    public void setBackgroundColor(int color) {
        dropdownBackgroundColor = color;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return customView(position, convertView, parent);
    }

    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.convert_spinner_layout, parent, false);
        }
        CustomItems items = getItem(position);
        ImageView spinnerImage = convertView.findViewById(R.id.ivCustomSpinner);
        TextView spinnerName = convertView.findViewById(R.id.tvCustomSpinner);
        if(items != null) {
            spinnerImage.setImageResource(items.getSpinnerImage());
            spinnerName.setText(items.getSpinnerText());

            spinnerName.setTextColor(defaultTextColor);
            convertView.setBackgroundColor(dropdownBackgroundColor);
        }
        return convertView;
    }

    public View customView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.convert_spinner_layout, parent, false);
        }
        CustomItems items = getItem(position);
        ImageView spinnerImage = convertView.findViewById(R.id.ivCustomSpinner);
        TextView spinnerName = convertView.findViewById(R.id.tvCustomSpinner);
        if(items != null) {
            spinnerImage.setImageResource(items.getSpinnerImage());
            spinnerName.setText(items.getSpinnerText());
            spinnerName.setTextColor(defaultTextColor);
        }
        return convertView;
    }
}
