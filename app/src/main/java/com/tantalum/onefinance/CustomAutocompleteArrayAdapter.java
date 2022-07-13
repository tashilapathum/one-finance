package com.tantalum.onefinance;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class CustomAutocompleteArrayAdapter extends ArrayAdapter implements Filterable {

    List<String> allCodes;
    List<String> originalCodes;
    StringFilter filter;

    public CustomAutocompleteArrayAdapter(Context context, int resource, List<String> keys) {
        super(context, resource, keys);
        allCodes = keys;
        originalCodes = keys;
    }

    public int getCount() {
        return allCodes.size();
    }

    public Object getItem(int position) {
        return allCodes.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    private class StringFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = "";
            if (constraint != null)
                filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            final List<String> list = originalCodes;

            int count = list.size();
            final ArrayList<String> nlist = new ArrayList<>(count);
            String filterableString;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i);
                if (filterableString.toLowerCase().contains(filterString)) {
                    nlist.add(filterableString);
                }
            }

            results.values = nlist;
            results.count = nlist.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            allCodes = (ArrayList<String>) results.values;
            notifyDataSetChanged();
        }

    }


    @NonNull
    @Override
    public Filter getFilter() {
        return new StringFilter();
    }
}
