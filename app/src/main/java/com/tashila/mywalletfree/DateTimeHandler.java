package com.tashila.mywalletfree;

import android.content.Context;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;
import org.threeten.bp.temporal.TemporalField;
import org.threeten.bp.temporal.WeekFields;

import java.util.Locale;

public class DateTimeHandler {
    private LocalDateTime localDateTime;

    public DateTimeHandler(String millis) {
        localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(millis)), ZoneId.systemDefault());
    }

    String getTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        return formatter.format(localDateTime);
    }

    String getMillis(String timestamp) {
        return null;
    }

    int getDayOfYear() {
        return localDateTime.getDayOfYear();
    }

    int getWeek() {
        TemporalField weekOfYear = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
        return localDateTime.get(weekOfYear);
    }

    int getWeek(LocalDateTime localDateTime) {
        TemporalField weekOfYear = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
        return localDateTime.get(weekOfYear);
    }

    LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

}
