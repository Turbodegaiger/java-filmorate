package ru.yandex.practicum.filmorate.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

@Slf4j
public class Checker {

    public static void checkFilmExistence(int filmId, JdbcTemplate jdbcTemplate) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT film_id FROM films WHERE film_id = ?",
                filmId);
        if (!filmRows.next()) {
            log.info("Произошла ошибка. Фильма с [id {}] не существует.", filmId);
            throw new NotFoundException(
                    String.format("Произошла ошибка. Фильма с [id %s] не существует.", filmId));
        }
    }

    public static void checkUserExistence(int userId, JdbcTemplate jdbcTemplate) {
        SqlRowSet idRows = jdbcTemplate.queryForRowSet("SELECT user_id FROM users WHERE user_id = ?", userId);
        if (!idRows.next()) {
            log.info("Произошла ошибка. Пользователя с [id {}] не существует.", userId);
            throw new NotFoundException(
                    String.format("Произошла ошибка. Пользователя с [id %s] не существует.", userId));
        }
    }
}
