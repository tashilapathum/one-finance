package com.tantalum.financejournal;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomFilterArrayAdapter extends ArrayAdapter<String> {
    public CustomFilterArrayAdapter(Context context, int textViewResourceId, String[] objects) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (position == 0) {
            TextView tv = new TextView(getContext());
            tv.setVisibility(View.GONE);
            view = tv;
        }
        else {
            view = super.getDropDownView(position, null, parent);
        }
        return view;
    }
}
