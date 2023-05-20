package ru.yandex.practicum.filmorate.validator;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.util.DateUtility;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Date;

@Slf4j
public class Validator {
    private static final Date EARLIEST_RELEASE = DateUtility.formatToDate("1895-11-28");

    public static boolean validate(Film film) throws ValidationException {
        try {
            if (film == null ||
                    film.getName().isBlank() ||
                    film.getDescription().length() > 200 ||
                    DateUtility.formatToDate(film.getReleaseDate()).before(EARLIEST_RELEASE) ||
                    film.getDuration() < 0) {
                log.info("Параметры фильма НЕ прошли валидацию: некорректно задано одно или несколько значений.");
                throw new ValidationException();
            }
        } catch (NullPointerException exception) {
            log.info("Параметры фильма НЕ прошли валидацию: одно из значений null.");
            throw new ValidationException();
        }
        log.info("Параметры фильма прошли валидацию.");
        return true;
    }

    public static boolean validate(User user) throws ValidationException {
        try {
            if (user == null ||
                    !user.getEmail().contains("@") ||
                    user.getLogin().contains(" ") || user.getLogin().isBlank() ||
                    user.getBirthday().after(new Date())) {
                log.info("Параметры пользователя не прошли валидацию.");
                throw new ValidationException();
            }
        } catch (NullPointerException exception) {
            log.info("Параметры пользователя НЕ прошли валидацию: одно из значений null.");
            throw new ValidationException();
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя не задано. Принимается значение login в качестве имени.");
            user.setName(user.getLogin());
        }
        log.info("Параметры пользователя прошли валидацию");
        return true;
    }
}
