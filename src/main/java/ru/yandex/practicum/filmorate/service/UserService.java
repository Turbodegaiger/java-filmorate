package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUser(int id) {
        return userStorage.getUser(id);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public void addFriend(int userId, int friend) {
        if (userId <= 0 || friend <= 0) {
            throw new NotFoundException(
                    String.format("Невозможно добавить в друзья пользователей %s и %s, некорректный id.", friend, userId));
        }
        if (!userStorage.getUser(userId).getFriends().add(friend) ||
            !userStorage.getUser(friend).getFriends().add(userId)) {
            throw new AlreadyExistsException(String.format("Пользователи %s и %s уже друзья.", userId, friend));
        }
        log.info("Пользователи {} и {} теперь друзья.", friend, userId);
    }

    public void removeFriend(int userId, int friend) {
        if (!userStorage.getUser(userId).getFriends().remove(friend) ||
            !userStorage.getUser(friend).getFriends().remove(userId)) {
            log.info("Не удалось удалить пользователей {} и {} из списков друзей друг у друга, они не друзья.", friend, userId);
            throw new NotFoundException(
                    String.format("Пользователи %s и %s не находятся в списках друзей друг у друга.", userId, friend));
        }
        log.info("Пользователи {} и {} больше не друзья.", friend, userId);
    }

    public List<User> getMutualFriendsList(int userId, int friend) {
        Set<Integer> friends1 = userStorage.getUser(userId).getFriends();
        Set<Integer> friends2 = userStorage.getUser(friend).getFriends();
        Set<Integer> mutualFriends = friends1.stream()
                .filter(friends2::contains)
                .collect(Collectors.toSet());
        List<User> friendList = getUserListByIds(mutualFriends);
        log.info("Список общих друзей пользователей {} и {}: {}", userId, friend, friendList);
        return friendList;
    }

    public List<User> getFriendList(int userId) {
        Set<Integer> friends = userStorage.getUser(userId).getFriends();
        List<User> friendList = getUserListByIds(friends);
        log.info("Список друзей пользователя {}: {}", userId, friendList);
        return friendList;
    }

    private List<User> getUserListByIds(Collection<Integer> idList) {
        List<User> userList = new ArrayList<>();
        for (Integer friendId : idList) {
            userList.add(userStorage.getUser(friendId));
        }
        return userList;
    }
}
