package ru.yandex.practicum.filmorate.storage;

import org.springframework.data.relational.core.sql.In;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {

    User addUser(User user);

    List<User> getUsers();

    User updateUser(User user);

    Optional<User> getUser(int userId);

    void removeUser(int userId);

    void addFriend(int userId, int friendId);

    Set<Integer> getUserFriends(int userId);

    void removeFriend(int userId, int friendId);
}