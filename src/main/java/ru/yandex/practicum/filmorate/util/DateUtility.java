package ru.yandex.practicum.filmorate.util;

import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtility {
    public static DateFormat formatTo = new SimpleDateFormat("yyyy-MM-dd");
    public static Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    public static Date formatToDate(String date) {
        try {
            return formatTo.parse(date);
        } catch (ParseException e) {
            throw new ValidationException("Некорректный формат даты. Ожидается строка формата yyyy-MM-dd.");
        }
    }

    public static String formatToString(Date date) {
        return formatTo.format(date);
    }
}
