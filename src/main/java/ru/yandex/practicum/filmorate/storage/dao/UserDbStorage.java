package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.Mapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validator.Validator;

import java.util.*;

@Component("userDbStorage")
@Slf4j
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User addUser(User user) {
        Validator.validate(user);
        SqlRowSet idRows = jdbcTemplate.queryForRowSet("SELECT email, login FROM users WHERE email = ? OR login = ?;", user.getEmail(), user.getLogin());
        if (!idRows.next()) {
            String sql = "INSERT INTO users (name, email, login, birthday) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                    user.getName(),
                    user.getEmail(),
                    user.getLogin(),
                    user.getBirthday());
            log.info("Пользователь {} с email {} добавлен в базу данных.", user.getName(), user.getEmail());
            sql = "SELECT user_id FROM users WHERE email=?";
            SqlRowSet emails = jdbcTemplate.queryForRowSet(sql, user.getEmail());
            emails.next();
            user.setId(emails.getInt("user_id"));
            return user;
        } else {
            log.info("Ошибка при создании пользователя - аккаунт с email {} или логином {} уже существует.",
                    user.getEmail(), user.getLogin());
            throw new AlreadyExistsException(String.format(
                    "Ошибка при создании пользователя - аккаунт с email %s или логином %s уже существует.",
                    user.getEmail(), user.getLogin()));
        }
    }

    @Override
    public List<User> getUsers() {
        String sql = "SELECT * FROM users ORDER BY name ASC";
        List<User> userList = jdbcTemplate.query(sql, (rs, rowNum) -> Mapper.user.mapRow(rs, rowNum));
        log.info("Из базы данных выгружен список всех пользователей размером {} записей.", userList.size());
        return userList;
    }

    @Override
    public User updateUser(User user) {
        Validator.validate(user);
        String sql = "UPDATE users SET name = ?, email = ?, login = ?, birthday = ? WHERE user_id=?";
        int rowsAffected = jdbcTemplate.update(sql, user.getName(), user.getEmail(), user.getLogin(), user.getBirthday(), user.getId());
        if (rowsAffected <= 0) {
            log.info("Ошибка при обновлении. Пользователь [id {}] не найден.", user.getId());
            throw new NotFoundException(String.format("Ошибка при обновлении. Пользователь [id %s] не найден.", user.getId()));
        }
        return user;
    }

    @Override
    public Optional<User> getUser(int userId) {
        String sql = "SELECT * FROM users WHERE user_id=?";
        List<User> user = jdbcTemplate.query(sql, (rs, rowNum) -> Mapper.user.mapRow(rs, rowNum), userId);
        if (user.isEmpty()) {
            log.info("Ошибка при выгрузке. Пользователь [id {}] не найден.", userId);
            throw new NotFoundException(String.format("Ошибка при выгрузке. Пользователь [id %s] не найден.", userId));
        }
        log.info("Из базы данных выгружен пользователь {} [id {}].", user.get(0).getName(), user.get(0).getId());
        return Optional.of(user.get(0));
    }

    @Override
    public void removeUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?;";
        int rowsAffected = jdbcTemplate.update(sql, userId);
        if (rowsAffected <= 0) {
            log.info("Ошибка при удалении. Пользователь [id {}] не найден.", userId);
            throw new NotFoundException(String.format("Ошибка при удалении. Пользователь [id %s] не найден.", userId));
        }
        log.info("Пользователь с [id {}] удалён.", userId);
    }

    @Override
    public Set<Integer> getUserFriends(int userId) {
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?;";
        SqlRowSet idRows = jdbcTemplate.queryForRowSet(sql, userId);
        Set<Integer> friends = new HashSet<>();
        if (idRows.next()) {
            do {
                friends.add(idRows.getInt("friend_id"));
            } while (idRows.next());
        }
        log.info("Из базы данных выгружен список друзей пользователя [id {}]", userId);
        return friends;
    }

    @Override
    public void removeAllUsers() {
        jdbcTemplate.update("DELETE FROM users;");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1;");
    }
}
