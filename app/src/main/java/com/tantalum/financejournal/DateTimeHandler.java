package com.tantalum.financejournal;

import android.content.Context;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class DateTimeHandler {
    public static final String TAG = "DateTimeHandler";
    private LocalDateTime localDateTime;
    private LocalDate localDate;

    public DateTimeHandler() {
        localDateTime = LocalDateTime.now();
    }

    public DateTimeHandler(String millis) {
        localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(millis)), ZoneId.systemDefault());
    }

    public DateTimeHandler(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public DateTimeHandler(LocalDate localDate) {
        this.localDate = localDate;
    }

    public long getInMillis() {
        if (localDateTime != null)
            return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return 0;
    }

    public String getTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        return formatter.format(localDateTime);
    }

    public String getDateStamp() {
        return localDateTime.toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));
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

    public LocalDate getLocalDate() {
        return localDateTime.toLocalDate();
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

    public String getPassedTime(Context context) {
        String passedTime;
        Period period = Period.between(LocalDate.of(getYear(), getMonthValue(), getDayOfMonth()), LocalDate.now());
        if (period.getYears() > 0)
            passedTime = period.getYears() + context.getString(R.string.years);
        else if (period.getMonths() > 0)
            passedTime = period.getMonths() + context.getString(R.string.months);
        else
            passedTime = period.getDays() + context.getString(R.string.days);

        return passedTime;
    }
}
