package com.jupiter.tusa.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {
    public static Date fromString(String dateString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z (zzzz)", Locale.US);
        return dateFormat.parse(dateString);
    }

    public static TimeBetween between(Date startDate, Date endDate) {
        long startTimeMillis = startDate.getTime();
        long endTimeMillis = endDate.getTime();

        long durationMillis = endTimeMillis - startTimeMillis;
        long hours = durationMillis / (1000 * 60 * 60);
        long minutes = (durationMillis % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = ((durationMillis % (1000 * 60 * 60)) % (1000 * 60)) / 1000;
        return new TimeBetween(hours, minutes, seconds);
    }
}
