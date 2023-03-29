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
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Integer, User> users = new HashMap<>();
    private int idCounter;

    @Override
    public User addUser(User user) {
        Validator.validate(user);
        if (users.containsValue(user)) {
            throw new AlreadyExistsException("Пользователь " + user.getEmail() + " уже существует.");
        }
        user.setId(idGenerator());
        users.put(user.getId(), user);
        log.info("Создан пользователь с id {}.", user.getId());
        return user;
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUser(int userId) {
        User user = users.get(userId);
        if (user == null) {
            log.info("Пользователь {} НЕ найден.", userId);
            throw new NotFoundException("Пользователь " + userId + " НЕ найден.");
        }
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            log.info("Пользователь {} НЕ найден.", user.getId());
            throw new NotFoundException("Пользователь " + user.getId() + " НЕ найден.");
        }
        Validator.validate(user);
        users.replace(user.getId(), user);
        log.info("Обновлён пользователь с id {}.", user.getId());
        return user;
    }

    private int idGenerator() {
        idCounter++;
        log.info("Генерируется id. Его номер - {}.", idCounter);
        return idCounter;
    }
}
