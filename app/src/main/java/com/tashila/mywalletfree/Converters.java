package com.tashila.mywalletfree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.room.TypeConverter;

public class Converters {
    @TypeConverter
    public String fromStringArray(List<String> strings) {
        String string = "";
        for(String s : strings)
            string += (s + ",,,");

        return string;
    }

    @TypeConverter
    public List<String> toStringArray(String concatenatedStrings) {
        List<String> myStrings = new ArrayList<>();
        Collections.addAll(myStrings, concatenatedStrings.split(",,,"));

        return myStrings;
    }
}
