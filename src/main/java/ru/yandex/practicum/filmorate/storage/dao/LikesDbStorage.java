package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

@Component
@Slf4j
public class LikesDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public LikesDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addLike(int filmId, int userId) {
        String sql = "MERGE INTO users_liked AS ul USING (VALUES (?,?)) s(film_id, user_id) " +
                "ON ul.film_id = s.film_id AND ul.user_id = s.user_id " +
                "WHEN NOT MATCHED THEN INSERT VALUES (s.film_id, s.user_id);";
        try {
            int rowsAffected = jdbcTemplate.update(sql, filmId, userId);
            updateRate(filmId);
            if (rowsAffected <= 0) {
                log.info("Лайк фильму [id {}] от пользователя [id {}] уже существует.", filmId, userId);
                throw new AlreadyExistsException(
                        String.format("Лайк фильму [id %s] от пользователя [id %s] уже существует.", filmId, userId));
            }
        } catch (DataIntegrityViolationException exception) {
            log.info("Невозможно выполнить запрос. Фильм [id {}] и/или пользователь [id {}] не найдены.", filmId, userId);
            throw new NotFoundException(String.format(
                    "Невозможно выполнить запрос. Фильм [id %s] и/или пользователь [id %s] не найдены.", filmId, userId));
        }
        log.info("В базу данных добавлен лайк фильму [id {}] от пользователя [id {}]", filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        int rowsAffected = jdbcTemplate.update("DELETE FROM users_liked WHERE film_id=? AND user_id=?;",
                filmId, userId);
        updateRate(filmId);
        if (rowsAffected <= 0) {
            log.info("Ошибка при удалении лайка. Лайк фильму [id {}] от пользователя [id {}] не найден.", filmId, userId);
            throw new NotFoundException(
                    String.format("Ошибка при удалении лайка. Лайк фильму [id %s] от пользователя [id %s] не найден.", filmId, userId));
        }
        log.info("Из базы данных удалён лайк фильму [id {}] от пользователя [id {}]", filmId, userId);
    }

    private void updateRate(int filmId) {
        String sql = "UPDATE films f SET rate = (SELECT COUNT(l.user_id) FROM users_liked l WHERE l.film_id = f.film_id) WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }
}
