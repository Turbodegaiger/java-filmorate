package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dao.FriendDbStorage;
import ru.yandex.practicum.filmorate.validator.Validator;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final FriendDbStorage friendDbStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       FriendDbStorage friendDbStorage) {
        this.userStorage = userStorage;
        this.friendDbStorage = friendDbStorage;
    }

    public User addUser(User user) {
        Validator.validate(user);
        return userStorage.addUser(user);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUser(int id) {
        return userStorage.getUser(id).orElse(new User());
    }

    public User updateUser(User user) {
        Validator.validate(user);
        return userStorage.updateUser(user);
    }

    public void removeUser(int userId) {
        userStorage.removeUser(userId);
    }

    public void addFriend(int userId, int friendId) {
        if (userId <= 0 || friendId <= 0) {
            throw new NotFoundException(
                    String.format("Невозможно добавить в друзья пользователей [id %s] и [id %s], некорректный id.", friendId, userId));
        }
        friendDbStorage.addFriend(userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        friendDbStorage.removeFriend(userId, friendId);
    }

    public List<User> getCommonFriendsList(int userId, int friend) {
        Set<Integer> friends1 = userStorage.getUserFriends(userId);
        Set<Integer> friends2 = userStorage.getUserFriends(friend);
        List<User> friendList = new ArrayList<>();
        if (!friends1.isEmpty() && !friends2.isEmpty()) {
            Set<Integer> commonFriends = friends1.stream()
                    .filter(friends2::contains)
                    .collect(Collectors.toSet());
            if (commonFriends.isEmpty()) {
                log.info("У пользователей [id {}] и [id {}] нет общих друзей.", userId, friend);
                return friendList;
            }
            friendList = getUserListByIds(commonFriends);
            log.info("Список общих друзей пользователей [id {}] и [id {}]: {}", userId, friend, friendList);
        } else {
            log.info("У пользователей [id {}] и [id {}] нет общих друзей.", userId, friend);
        }
        return friendList;
    }

    public List<User> getFriendList(int userId) {
        Set<Integer> friends = userStorage.getUserFriends(userId);
        List<User> friendList = getUserListByIds(friends);
        log.info("Список друзей пользователя [id {}]: {}", userId, friendList);
        return new ArrayList<>(friendList);
    }

    public void removeAllUsers() {
        userStorage.removeAllUsers();
    }

    private List<User> getUserListByIds(Collection<Integer> idList) {
        List<User> userList = new ArrayList<>();
        for (Integer friendId : idList) {
            userList.add(userStorage.getUser(friendId).orElse(new User()));
        }
        return userList;
    }
}
