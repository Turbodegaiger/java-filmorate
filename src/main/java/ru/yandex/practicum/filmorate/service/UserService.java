package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public boolean addFriend(int userId, int friend) {
        log.info("Получен запрос на добавление пользователей {} и {} в друзья друг к другу.", friend, userId);
        if (userId <= 0 || friend <= 0) {
            throw new NotFoundException(String.format("Невозможно добавить в друзья пользователей %s и %s, некорректный id.", friend, userId));
        }
        userStorage.checkContains(userId);
        userStorage.checkContains(friend);
        userStorage.getUser(userId).getFriends().add(friend);
        userStorage.getUser(friend).getFriends().add(userId);
        log.info("Пользователи {} и {} теперь друзья.", friend, userId);
        return true;
    }

    public boolean removeFriend(int userId, int friend) {
        log.info("Получен запрос на удаление пользователей {} и {} из списка друзей друг друга.", friend, userId);
        userStorage.checkContains(userId);
        userStorage.checkContains(friend);
        if (!userStorage.getUser(userId).getFriends().remove(friend) ||
            !userStorage.getUser(friend).getFriends().remove(userId)) {
            log.info("Не удалось удалить пользователей {} и {} из списков друзей друг у друга.", friend, userId);
            throw new NotFoundException("Пользователи не находятся в списках друзей друг у друга.");
        };
        log.info("Пользователи {} и {} больше не друзья.", friend, userId);
        return true;
    }

    public List<User> getMutualFriendsList(int friendId1, int friendId2) {
        log.info("Получен запрос на получение списка общих друзей у пользователей {} и {}.", friendId1, friendId2);
        userStorage.checkContains(friendId1);
        userStorage.checkContains(friendId2);
        Set<Integer> friends1 = userStorage.getUser(friendId1).getFriends();
        Set<Integer> friends2 = userStorage.getUser(friendId2).getFriends();
        Set<Integer> mutualFriends = friends1.stream()
                .filter(friends2::contains)
                .collect(Collectors.toSet());
        List<User> friendList = getUserListByIds(mutualFriends);
        log.info("Список общих друзей пользователей {} и {}: {}", friendId1, friendId2, friendList);
        return friendList;
    }

    public List<User> getFriendList(int userId) {
        log.info("Получен запрос на получение списка друзей у пользователя {}.", userId);
        Set<Integer> friends = userStorage.getUser(userId).getFriends();
        List<User> friendList = getUserListByIds(friends);
        log.info("Список друзей пользователя {}: {}", userId, friendList);
        return friendList;
    }

    private List<User> getUserListByIds(Collection<Integer> idList) {
        List<User> userList = new ArrayList<>();
        for(Integer friendId : idList) {
            userList.add(userStorage.getUser(friendId));
        }
        return userList;
    }
}
