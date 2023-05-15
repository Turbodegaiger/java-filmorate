package ru.yandex.practicum.filmorate.date;

import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtility {
    public static DateFormat formatTo = new SimpleDateFormat("yyyy-MM-dd");

    public static Date formatToDate(String date) {
        try {
            return formatTo.parse(date);
        } catch (ParseException e) {
            throw new ValidationException("Некорректный формат даты. Ожидается строка формата yyyy-MM-dd.");
        }
    }

    public static String formatToString(Date date) {
        return DateFormat.getDateInstance().format(date);
    }
}
