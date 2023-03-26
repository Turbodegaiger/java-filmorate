package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    final UserStorage userStorage;
    final UserService userService;

    @Autowired
    public UserController(UserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@RequestBody User user) {
        return userStorage.addUser(user);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public User getUser(@PathVariable int id) {
        return userStorage.getUser(id);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User updateUser(@RequestBody User user) {
        return userStorage.updateUser(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public boolean addToFriends(@PathVariable int id, @PathVariable int friendId) {
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public boolean removeFriend(@PathVariable int id, @PathVariable int friendId) {
        return userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<User> getFriendList(@PathVariable Integer id) {
        return userService.getFriendList(id);
    }

    @GetMapping("{id}/friends/common/{otherId}")
    @ResponseStatus(HttpStatus.OK)
    public List<User> getMutualFriendsList(@PathVariable Integer id, @PathVariable Integer otherId) {
        return userService.getMutualFriendsList(id, otherId);
    }
}
