package com.tashila.mywalletfree;

import android.util.Log;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;
import org.threeten.bp.temporal.TemporalField;
import org.threeten.bp.temporal.WeekFields;

import java.util.Locale;

public class DateTimeHandler {
    public static final String TAG = "DateTimeHandler";
    private LocalDateTime localDateTime;

    public DateTimeHandler(String millis) {
        localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(millis)), ZoneId.systemDefault());
    }

    String getTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        return formatter.format(localDateTime);
    }

    int getDayOfYear() {
        return localDateTime.getDayOfYear();
    }

    int getWeekOfYear() {
        TemporalField weekOfYear = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
        return localDateTime.get(weekOfYear);
    }

    int getWeekOfYear(LocalDateTime localDateTime) {
        TemporalField weekOfYear = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
        return localDateTime.get(weekOfYear);
    }

    LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    int getDayOfWeek() {
        return localDateTime.getDayOfWeek().getValue();
    }

    int getMonthValue() {
        return localDateTime.getMonthValue();
    }

    int getYear() {
        return localDateTime.getYear();
    }

    int getDayOfMonth(){
        return localDateTime.getDayOfMonth();
    }
}
