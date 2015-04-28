package edu.uic.ibeis_tourist.utils;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTimeUtils {

    public static String calendarToString (Calendar calendar, DateFormat format) {
        return new SimpleDateFormat(format.getValue()).format(calendar.getTime());
    }

    public static Calendar stringToCalendar (String string, DateFormat format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format.getValue());

        GregorianCalendar dateTime = new GregorianCalendar();

        Date d = dateFormat.parse(string, new ParsePosition(0));
        if (d == null) {
            return null;
        }
        else {
            dateTime.setTimeInMillis(d.getTime());
            return dateTime;
        }
    }

    public enum DateFormat {
        DATE_ONLY("LLL dd',' yyyy"), DATETIME("LLL dd',' yyyy 'at' h:mm a"), DATABASE("yyyy-MM-dd HH:mm:ss");

        private String value;

        DateFormat(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
