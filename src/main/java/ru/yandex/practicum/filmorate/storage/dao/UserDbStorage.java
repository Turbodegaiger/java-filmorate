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
        SqlRowSet idRows = jdbcTemplate.queryForRowSet("SELECT email, login FROM users WHERE email = ? OR login = ?;", user.getEmail(), user.getLogin());
        if (!idRows.next()) {
            Validator.validate(user);
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
        if (checkUserExistence(user.getId())) {
            Validator.validate(user);
            String sql = "UPDATE users SET name = ?, email = ?, login = ?, birthday = ? WHERE user_id=?";
            jdbcTemplate.update(sql, user.getName(), user.getEmail(), user.getLogin(), user.getBirthday(), user.getId());
            return user;
        } else {
            log.info("Ошибка при обновлении. Пользователя с [id {}] не существует.", user.getId());
            throw new NotFoundException(
                    String.format("Ошибка при обновлении. Пользователя с [id %s] не существует.", user.getId()));
        }
    }

    @Override
    public Optional<User> getUser(int userId) {
        if (checkUserExistence(userId)) {
            String sql = "SELECT * FROM users WHERE user_id=?";
            List<User> user = jdbcTemplate.query(sql, (rs, rowNum) -> Mapper.user.mapRow(rs, rowNum), userId);
            log.info("Из базы данных выгружен пользователь {} [id {}].", user.get(0).getName(), user.get(0).getId());
            return Optional.of(user.get(0));
        } else {
            log.info("Ошибка при выгрузке. Пользователя с [id {}] не существует.", userId);
            throw new NotFoundException(
                    String.format("Ошибка при выгрузке. Пользователя с [id %s] не существует.", userId));
        }
    }

    @Override
    public void removeUser(int userId) {
        if (checkUserExistence(userId)) {
            String sql = "DELETE FROM users WHERE user_id = ?;";
            jdbcTemplate.update(sql, userId);
            log.info("Пользователь с [id {}] удалён.", userId);
        } else {
            log.info("Ошибка при удалении. Пользователя с [id {}] не существует.", userId);
            throw new NotFoundException(
                    String.format("Ошибка при удалении. Пользователя с [id %s] не существует.", userId));
        }
    }

    @Override
    public void addFriend(int userId, int friendId) {
        if (userId == friendId) {
            log.info("Пользователь не может добавлять самого себя в друзья.");
            throw new AlreadyExistsException("Пользователь не может добавлять самого себя в друзья.");
        }
        if (checkUserExistence(userId) && checkUserExistence(friendId)) {
            Set<Integer> friends = getUserFriends(userId);
            if (!friends.contains(friendId)) {
                Set<Integer> friendsOfFriend = getUserFriends(friendId);
                if (friendsOfFriend.contains(userId)) {
                    jdbcTemplate.update("INSERT INTO friends_confirmed (user_id, friend_id) VALUES (?,?);", userId, friendId);
                }
                jdbcTemplate.update("INSERT INTO friends_not_confirmed (user_id, friend_id) VALUES (?,?);", userId, friendId);
                log.info("Пользователь [id {}] добавил в друзья пользователя [id {}]", userId, friendId);
            } else {
                log.info("Пользователь [id {}] уже имеет в друзьях пользователя [id {}]", userId, friendId);
                throw new AlreadyExistsException(String.format(
                        "Пользователь [id %s] уже имеет в друзьях пользователя [id %s]", userId, friendId));
            }
        } else {
            log.info("Ошибка при удалении. Пользователя с [id {}] не существует.", userId);
            throw new NotFoundException(
                    String.format("Ошибка при удалении. Пользователя с [id %s] не существует.", userId));
        }
    }

    @Override
    public Set<Integer> getUserFriends(int userId) {
        if (checkUserExistence(userId)) {
            String sql = "SELECT friend_id FROM friends_not_confirmed WHERE user_id = ?;";
            SqlRowSet idRows = jdbcTemplate.queryForRowSet(sql, userId);
            Set<Integer> friends = new HashSet<>();
            while (idRows.next()) {
                friends.add(idRows.getInt("friend_id"));
            }
            log.info("Из базы данных выгружен список друзей пользователя [id {}]", userId);
            return friends;
        } else {
            log.info("Ошибка при получении списка друзей пользователя [id {}]. " +
                    "Пользователя с [id {}] не существует.", userId, userId);
            throw new NotFoundException(
                    String.format("Ошибка при получении списка друзей пользователя [id %s]. " +
                            "Пользователя с [id %s] не существует.", userId, userId));
        }
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        if (userId == friendId) {
            log.info("Пользователь не может удалять самого себя из друзей.");
            throw new AlreadyExistsException("Пользователь не может удалять самого себя из друзей.");
        }
        if (checkUserExistence(userId)) {
            Set<Integer> friends = getUserFriends(userId);
            if (friends.contains(friendId)) {
                jdbcTemplate.update("DELETE FROM friends_not_confirmed WHERE user_id=? AND friend_id=?;", userId, friendId);
                jdbcTemplate.update("DELETE FROM friends_confirmed WHERE user_id=? AND friend_id=?;", userId, friendId);
                log.info("Пользователь [id {}] удалил из списка друзей пользователя [id {}]", userId, friendId);
            }
        } else {
            log.info("Ошибка при удалении из друзей. Пользователя с [id {}] не существует.", userId);
            throw new NotFoundException(
                    String.format("Ошибка при удалении из друзей. Пользователя с [id %s] не существует.", userId));
        }
    }

    private boolean checkUserExistence(int userId) {
        SqlRowSet idRows = jdbcTemplate.queryForRowSet("SELECT user_id FROM users WHERE user_id = ?", userId);
        return idRows.next() && idRows.getInt("user_id") == userId;
    }
}
