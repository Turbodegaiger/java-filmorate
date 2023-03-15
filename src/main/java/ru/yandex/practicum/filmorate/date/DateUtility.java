package ru.yandex.practicum.filmorate.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtility {
    public static SimpleDateFormat formatTo = new SimpleDateFormat("yyyy-MM-dd");

    public static Date formatter(String date) {
        try {
            return formatTo.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
