package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validator.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage{
    private final HashMap<Integer, User> users = new HashMap<>();
    private int idCounter;

    public User addUser(User user) {
        log.info("Принят запрос на добавление нового пользователя {}.", user);
        Validator.validate(user);
        user.setId(idGenerator());
        users.put(user.getId(), user);
        log.info("Создан пользователь с id {}.", user.getId());
        return user;
    }

    public List<User> getUsers() {
        log.info("Принят запрос на получение списка пользователей: {}.", users.values());
        return new ArrayList<>(users.values());
    }

    public User getUser(int userId) {
        log.info("Принят запрос на получение пользователя по id: {}.", userId);
        checkContains(userId);
        return users.get(userId);
    }

    public User updateUser(User user) {
        log.info("Принят запрос на обновление пользователя {}.", user.getId());
        checkContains(user);
        Validator.validate(user);
        users.replace(user.getId(), user);
        log.info("Обновлён пользователь с id {}.", user.getId());
        return user;
    }

    public boolean checkContains(int id) {
        if (!users.containsKey(id)) {
            log.info("Пользователь {} НЕ найден.", id);
            throw new NotFoundException("Пользователь " + id + " не найден.");
        }
        return true;
    }

    public boolean checkContains(User user) {
        if (!users.containsKey(user.getId())) {
            log.info("Пользователь {} НЕ найден.", user.getId());
            throw new NotFoundException("Пользователь " + user.getId() + " не найден.");
        }
        return true;
    }

    private int idGenerator() {
        idCounter++;
        log.info("Генерируется id. Его номер - {}.", idCounter);
        return idCounter;
    }
}
