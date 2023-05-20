package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@RequestBody User user) {
        log.info("Принят запрос на добавление нового пользователя {}.", user);
        User response = userService.addUser(user);
        log.info("Отправлен ответ на запрос добавления нового пользователя {}.", response);
        return response;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<User> getUsers() {
        log.info("Принят запрос на получение списка пользователей");
        List<User> response = userService.getUsers();
        log.info("Отправлен ответ на запрос получения списка пользователей, размером {} записей.", response.size());
        return response;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public User getUser(@PathVariable int id) {
        log.info("Принят запрос на получение пользователя [id {}].", id);
        User response = userService.getUser(id);
        log.info("Отправлен ответ на запрос получения пользователя [id {}]: {}.", id, response);
        return response;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User updateUser(@RequestBody User user) {
        log.info("Принят запрос на обновление пользователя [id {}].", user.getId());
        User response = userService.updateUser(user);
        log.info("Отправлен ответ на запрос обновления пользователя [id {}]: {}.", response.getId(), response);
        return response;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void removeUser(@PathVariable int id) {
        log.info("Принят запрос на удаление пользователя [id {}].", id);
        userService.removeUser(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public void addToFriends(@PathVariable int id, @PathVariable int friendId) {
        log.info("Получен запрос на добавление пользователя [id {}] в друзья к пользователю [id {}] .", friendId, id);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeFriend(@PathVariable int id, @PathVariable int friendId) {
        log.info("Получен запрос на удаление пользователя [id {}] из списка друзей пользователя [id {}].", friendId, id);
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<User> getFriendList(@PathVariable int id) {
        log.info("Получен запрос на получение списка друзей у пользователя [id {}].", id);
        List<User> response = userService.getFriendList(id);
        log.info("Отправлен ответ на запрос получения списка друзей у пользователя [id {}] размером {} записей: {}.",
                id, response.size(),  response);
        return response;
    }

    @GetMapping("{id}/friends/common/{otherId}")
    @ResponseStatus(HttpStatus.OK)
    public List<User> getCommonFriendsList(@PathVariable int id, @PathVariable int otherId) {
        log.info("Получен запрос на получение списка общих друзей у пользователей [id {}] и [id {}].", id, otherId);
        List<User> response = userService.getCommonFriendsList(id, otherId);
        log.info("Отправлен ответ на запрос получения списка общих друзей у пользователей " +
                "[id {}] и [id {}] размером {} записей: {}.", id, otherId, response.size(), response);
        return response;
    }
}
