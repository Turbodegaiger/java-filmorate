package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

@Component
@Slf4j
public class FriendDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public FriendDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void removeFriend(int userId, int friendId) {
        if (userId == friendId) {
            log.info("Ошибка при удалении из друзей. Пользователь [id {}] не может удалить сам себя.", userId);
            throw new AlreadyExistsException(
                    String.format("Ошибка при удалении из друзей. Пользователь [id %s] не может удалить сам себя.", userId));
        }
        int rowsAffected = jdbcTemplate.update("DELETE FROM friends WHERE user_id = ? AND friend_id = ?;",
                userId, friendId);
        if (rowsAffected <= 0) {
            log.info("Ошибка при удалении из друзей. Пользователь [id {}] не имеет в друзьях [id {}].", userId, friendId);
            throw new NotFoundException(
                    String.format("Ошибка при удалении из друзей. Пользователь [id %s] не имеет в друзьях [id %s].", userId, friendId));
        }
        log.info("Пользователь [id {}] удалил из списка друзей пользователя [id {}]", userId, friendId);
    }

    public void addFriend(int userId, int friendId) {
        if (userId == friendId) {
            log.info("Ошибка при удалении из друзей. Пользователь [id {}] не может добавить в друзья сам себя.", userId);
            throw new AlreadyExistsException(
                    String.format("Ошибка при удалении из друзей. Пользователь [id %s] не может добавить в друзья сам себя.", userId));
        }
        String sql = "MERGE INTO friends AS f USING (VALUES (?,?)) s(user_id, friend_id) " +
                "ON f.user_id = s.user_id AND f.friend_id = s.friend_id " +
                "WHEN NOT MATCHED THEN INSERT VALUES (s.user_id, s.friend_id);";
        try {
            int rowsAffected = jdbcTemplate.update(sql, userId, friendId);
            if (rowsAffected <= 0) {
                log.info("Пользователь [id {}] уже имеет в друзьях пользователя [id {}]", userId, friendId);
                throw new AlreadyExistsException(String.format(
                        "Пользователь [id %s] уже имеет в друзьях пользователя [id %s]", userId, friendId));
            }
        } catch (DataIntegrityViolationException exception) {
            log.info("Невозможно выполнить запрос. Пользователь [id {}] и/или пользователь [id {}] не найдены.", userId, friendId);
            throw new NotFoundException(String.format(
                    "Невозможно выполнить запрос. Пользователь [id %s] и/или пользователь [id %s] не найдены.", userId, friendId));
        }
        log.info("Пользователь [id {}] добавил в друзья пользователя [id {}].", userId, friendId);
    }
}
