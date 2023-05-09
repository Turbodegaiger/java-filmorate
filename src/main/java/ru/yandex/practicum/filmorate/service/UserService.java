package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
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
        return userStorage.getUser(id).orElse(new User());
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public void removeUser(int userId) {
        userStorage.removeUser(userId);
    }

    public void addFriend(int userId, int friendId) {
        if (userId <= 0 || friendId <= 0) {
            throw new NotFoundException(
                    String.format("Невозможно добавить в друзья пользователей %s и %s, некорректный id.", friendId, userId));
        }
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        userStorage.removeFriend(userId, friendId);
        log.info("Пользователи {} и {} больше не друзья.", friendId, userId);
    }

    public List<User> getCommonFriendsList(int userId, int friend) {
        Set<Integer> friends1 = userStorage.getUserFriends(userId);
        Set<Integer> friends2 = userStorage.getUserFriends(friend);
        List<User> friendList = new ArrayList<>();
        if (!friends1.isEmpty() && !friends2.isEmpty()) {
            Set<Integer> mutualFriends = friends1.stream()
                    .filter(friends2::contains)
                    .collect(Collectors.toSet());
            if (mutualFriends.isEmpty()) {
                log.info("У пользователей {} и {} нет общих друзей.", userId, friend);
                return friendList;
            }
            friendList = getUserListByIds(mutualFriends);
            log.info("Список общих друзей пользователей {} и {}: {}", userId, friend, friendList);
        } else {
            log.info("У пользователей {} и {} нет общих друзей.", userId, friend);
        }
        return friendList;
    }

    public List<User> getFriendList(int userId) {
        Set<Integer> friends = userStorage.getUserFriends(userId);
        List<User> friendList = getUserListByIds(friends);
        log.info("Список друзей пользователя {}: {}", userId, friendList);
        return friendList;
    }

    private List<User> getUserListByIds(Collection<Integer> idList) {
        List<User> userList = new ArrayList<>();
        for (Integer friendId : idList) {
            userList.add(userStorage.getUser(friendId).orElse(new User()));
        }
        return userList;
    }
}
