package com.tashila.mywalletfree;

import android.util.Log;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class DateTimeHandler {
    public static final String TAG = "DateTimeHandler";
    private LocalDateTime localDateTime;

    public DateTimeHandler() {
        localDateTime = LocalDateTime.now();
    }

    public DateTimeHandler(String millis) {
        localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(millis)), ZoneId.systemDefault());
    }

    public DateTimeHandler(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public String getTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        return formatter.format(localDateTime);
    }

    public int getDayOfYear() {
        return localDateTime.getDayOfYear();
    }

    public int getWeekOfYear() {
        TemporalField weekOfYear = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
        return localDateTime.get(weekOfYear);
    }

    public int getWeekOfYear(LocalDateTime localDateTime) {
        TemporalField weekOfYear = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
        return localDateTime.get(weekOfYear);
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public int getDayOfWeek() {
        return localDateTime.getDayOfWeek().getValue();
    }

    public int getMonthValue() {
        return localDateTime.getMonthValue();
    }

    public int getYear() {
        return localDateTime.getYear();
    }

    public int getDayOfMonth(){
        return localDateTime.getDayOfMonth();
    }
}
