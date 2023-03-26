package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User addUser(User user);
    List<User> getUsers();
    User updateUser(User user);
    User getUser(int userId);
    boolean checkContains(User user);

    boolean checkContains(int id);
}
