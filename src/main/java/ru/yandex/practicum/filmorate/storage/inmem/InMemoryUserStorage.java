package ru.yandex.practicum.filmorate.storage.inmem;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validator.Validator;

import java.util.*;

@Component("inMemoryUserStorage")
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Integer, User> users = new HashMap<>();
    private int idCounter;

    @Override
    public User addUser(User user) {
        Validator.validate(user);
        if (users.containsValue(user)) {
            log.info("Пользователь с email {} уже существует.", user.getEmail());
            throw new AlreadyExistsException("Пользователь с email " + user.getEmail() + " уже существует.");
        }
        user.setId(idGenerator());
        users.put(user.getId(), user);
        log.info("Создан пользователь [id] {}.", user.getId());
        return user;
    }

    @Override
    public List<User> getUsers() {
        List<User> usersList = new ArrayList<>();
        for(Integer id : users.keySet()) {
            usersList.add(getUser(id).orElse(new User()));
        }
        log.info("Из базы данных выгружен список всех пользователей в количестве {} записей.", usersList.size());
        return usersList;
    }

    @Override
    public Optional<User> getUser(int userId) {
        User user = users.get(userId);
        if (user == null) {
            log.info("Пользователь {} НЕ найден.", userId);
            throw new NotFoundException("Пользователь " + userId + " НЕ найден.");
        }
        log.info("Из базы данных выгружен пользователь {} [id] {}.", user.getName(), user.getId());
        return Optional.of(user);
    }

    @Override
    public void removeUser(int userId) {
        if (users.remove(userId) == null) {
            log.info("Ошибка при удалении. Пользователя с [id] {} не существует.", userId);
            throw new NotFoundException(String.format("Ошибка при удалении. Пользователя с [id] %s не существует.", userId));
        }
        log.info("Удалён пользователь [id] {}.", userId);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        if (!getUser(userId).orElse(new User()).getFriends().add(friendId)) {
            log.info("Пользователи [id] {} и [id] {} уже друзья.", userId, friendId);
            throw new AlreadyExistsException(String.format("Пользователи [id] %s и [id] %s уже друзья.", userId, friendId));
        }
        log.info("Пользователь [id] {} добавил в друзья пользователя [id] {}", userId, friendId);
    }

    @Override
    public Set<Integer> getUserFriends(int userId) {
        Set<Integer> friendsId = getUser(userId).orElse(new User()).getFriends();
        log.info("Выгружен список друзей у пользователя [id] {}.", userId);
        return friendsId;
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        if (!getUser(userId).orElse(new User()).getFriends().remove(friendId)) {
            log.info("Пользователи [id] {} и [id] {} не находятся в списках друзей друг у друга.",
                    friendId, userId);
            throw new NotFoundException(
                    String.format("Пользователи [id] %s и [id] %s не находятся в списках друзей друг у друга.",
                            userId, friendId));
        }
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            log.info("Пользователь [id] {} НЕ найден.", user.getId());
            throw new NotFoundException("Пользователь [id] " + user.getId() + " НЕ найден.");
        }
        Validator.validate(user);
        users.replace(user.getId(), user);
        log.info("Обновлён пользователь [id] {}.", user.getId());
        return user;
    }

    private int idGenerator() {
        idCounter++;
        log.info("Генерируется [id]. Его номер - {}.", idCounter);
        return idCounter;
    }
}
