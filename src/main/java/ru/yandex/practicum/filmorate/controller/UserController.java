package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validator.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@Slf4j
public class UserController {
    private final HashMap<Integer, User> users = new HashMap<>();
    private int idCounter;

    @PostMapping(value = "/users")
    public User addUser(@RequestBody User user) {
        log.info("Принят запрос на добавление нового пользователя {}.", user);
        Validator.validate(user);
        user.setId(idGenerator());
        users.put(user.getId(), user);
        log.info("Создан пользователь с id {}.", user.getId());
        return user;
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        log.info("Принят запрос на получение списка пользователей: {}.", users.values());
        return new ArrayList<>(users.values());
    }

    @PutMapping(value = "/users")
    public User updateUser(@RequestBody User user) {
        log.info("Принят запрос на обновление пользователя.");
        if (!users.containsKey(user.getId())) {
            log.info("Обновляемый пользователь НЕ найден.");
            throw new NotFoundException();
        }
        Validator.validate(user);
        users.replace(user.getId(), user);
        log.info("Обновлён пользователь с id {}.", user.getId());
        return user;
    }

    private int idGenerator() {
        log.info("Генерируется id. Его номер - {}.", idCounter+1);
        return ++idCounter;
    }
}
