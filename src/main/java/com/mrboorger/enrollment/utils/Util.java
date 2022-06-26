package com.mrboorger.enrollment.utils;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Util {

    // ISO 8601 BASIC is used by the API signature
    // "2022-02-02T12:00:00.000Z"
    public static String ISO_8601BASIC_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.sss'Z'";  // ?

    public static boolean isIsoTimestamp(String s) {
        return s.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}Z");
    }

    public static Date parseIsoDateTime(String s) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_8601BASIC_DATE_PATTERN);
        dateFormat.setLenient(false);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date result = dateFormat.parse(s, new ParsePosition(0));
        return result;
    }

    public static String IsoDateTimeToString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_8601BASIC_DATE_PATTERN);
        dateFormat.setLenient(false);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return dateFormat.format(date);
    }
}