package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validator.Validator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
        String email = jdbcTemplate.query("SELECT email FROM users WHERE email = ?", this::makeString, user.getEmail());
        if (user.getEmail().equals(email)) {
            log.info("Ошибка при создании пользователя - email {} уже существует.", email);
            throw new AlreadyExistsException(String.format("Пользователь с email %s уже существует", email));
        }
        String sql = "INSERT INTO users (name, email, login, birthday)" +
                "values (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getBirthday());
        log.info("Пользователь {} с email {} добавлен в базу данных.", user.getName(), user.getEmail());
        return user;
    }

    @Override
    public List<User> getUsers() {
        String sql = "SELECT * FROM users ORDER BY name ASC";
        List<User> userList = jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs));
        log.info("Из базы данных выгружен список всех пользователей размером {} записей.", userList.size());
        return userList;
    }

    @Override
    public User updateUser(User user) {
        Validator.validate(user);
        Integer expectedId = jdbcTemplate.query(
                "SELECT user_id FROM users WHERE user_id = ?", this::makeInteger, user.getId());
        if (expectedId == null || expectedId != user.getId()) {
            log.info("Ошибка при обновлении. Пользователя с id {} не существует.", user.getId());
            throw new NotFoundException(
                    String.format("Ошибка при обновлении. Пользователя с id %s не существует.", user.getId()));
        }
        String sql = "UPDATE users SET name = ?, email = ?, login = ?, birthday = ? WHERE user_id=?";
        jdbcTemplate.update(sql, user.getName(), user.getEmail(), user.getLogin(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public User getUser(int userId) {
        Integer expectedId = jdbcTemplate.query(
                "SELECT user_id FROM users WHERE user_id = ?", this::makeInteger, userId);
        if (expectedId == null || expectedId != userId) {
            log.info("Ошибка при выгрузке. Пользователя с id {} не существует.", userId);
            throw new NotFoundException(
                    String.format("Ошибка при выгрузке. Пользователя с id %s не существует.", userId));
        }
        String sql = "SELECT * FROM users WHERE user_id=?";
        List<User> user = jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs), userId);
        log.info("Из базы данных выгружен пользователь {} id {}.", user.get(0).getName(), user.get(0).getId());
        return user.get(0);
    }

    private User makeUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getDate("birthday"));
    }

    private String makeString(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return rs.getString(1);
        } else {
            return null;
        }
    }

    private Integer makeInteger(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return rs.getInt(1);
        } else {
            return null;
        }
    }
}
