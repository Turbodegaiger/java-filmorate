package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.date.DateUtility;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.inmem.InMemoryUserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserServiceTest {
    UserService userService;
    List<User> users;

    @BeforeEach
    void setParameters() {
        userService = new UserService(new InMemoryUserStorage());
        users = new ArrayList<>();
        loadUsers();
    }

    @Test
    void addUserShouldReturnUser() {
        Assertions.assertEquals(userService.addUser(users.get(0)), users.get(0));
    }

    @Test
    void addUserShouldThrowExceptionIfAlreadyExists() {
        userService.addUser(users.get(0));
        final AlreadyExistsException exception = assertThrows(
                AlreadyExistsException.class,
                () -> userService.addUser(users.get(0))
        );
        assertEquals("Пользователь aaaa@ya.ru уже существует.", exception.getMessage(),
                "Не возникает исключение при попытке добавления уже существующего пользователя");
    }

    @Test
    void getUsersShouldReturnListOfUsers() {
        userService.addUser(users.get(0));
        userService.addUser(users.get(1));
        Assertions.assertEquals(userService.getUsers(), users);
    }

    @Test
    void getUserShouldReturnCorrectUser() {
        userService.addUser(users.get(0));
        Assertions.assertEquals(userService.getUser(1), users.get(0));
    }

    @Test
    void getUserShouldThrowExceptionIfNotFound() {
        userService.addUser(users.get(0));
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getUser(2)
        );
        assertEquals("Пользователь 2 НЕ найден.", exception.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего пользователя");
    }

    @Test
    void updateUserShouldReplaceOldUser() {
        userService.addUser(users.get(0));
        User updated = new User();
        updated.setId(1);
        updated.setLogin("eqeqe");
        updated.setEmail("bugi-wugi@ya.ru");
        updated.setBirthday(DateUtility.formatToDate("2000-11-11"));
        Assertions.assertEquals(userService.updateUser(updated), userService.getUser(1));
    }

    @Test
    void updateUserShouldThrowExceptionIfNotFound() {
        userService.addUser(users.get(0));
        User updated = new User();
        updated.setId(4);
        updated.setLogin("eqeqe");
        updated.setEmail("bugi-wugi@ya.ru");
        updated.setBirthday(DateUtility.formatToDate("2000-11-11"));
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.updateUser(updated)
        );
        assertEquals("Пользователь 4 НЕ найден.", exception.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего пользователя");
    }

    @Test
    void addFriendShouldAddUserIdToFriendsSet() {
        userService.addUser(users.get(0));
        userService.addUser(users.get(1));
        userService.addFriend(1, 2);
        Assertions.assertEquals(userService.getUser(1).getFriends(), Set.of(users.get(1).getId()));
        Assertions.assertEquals(userService.getUser(2).getFriends(), Set.of(users.get(0).getId()));
    }

    @Test
    void addFriendShouldThrowExceptionIfNotFound() {
        userService.addUser(users.get(0));
        userService.addUser(users.get(1));
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.addFriend(4, 1)
        );
        assertEquals("Пользователь 4 НЕ найден.", exception.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего пользователя");
    }

    @Test
    void addFriendShouldThrowExceptionIfAlreadyExists() {
        userService.addUser(users.get(0));
        userService.addUser(users.get(1));
        userService.addFriend(1, 2);
        final AlreadyExistsException exception = assertThrows(
                AlreadyExistsException.class,
                () -> userService.addFriend(1, 2)
        );
        assertEquals("Пользователи 1 и 2 уже друзья.", exception.getMessage(),
                "Не возникает исключение при попытке повторного добавления в друзья");
    }

    @Test
    void removeFriendShouldRemoveUserIdFromFriendsSet() {
        userService.addUser(users.get(0));
        userService.addUser(users.get(1));
        userService.addFriend(1, 2);
        userService.removeFriend(1,2);
        Assertions.assertEquals(userService.getUser(1).getFriends(), Set.of());
        Assertions.assertEquals(userService.getUser(2).getFriends(), Set.of());
    }

    @Test
    void removeFriendShouldThrowExceptionIfNotFound() {
        User user3 = new User();
        user3.setEmail("eeee@ya.ru");
        user3.setLogin("eeee");
        user3.setBirthday(DateUtility.formatToDate("2000-11-10"));
        userService.addUser(users.get(0));
        userService.addUser(users.get(1));
        userService.addUser(user3);
        userService.addFriend(1, 2);
        final NotFoundException exception1 = assertThrows(
                NotFoundException.class,
                () -> userService.removeFriend(4, 1)
        );
        assertEquals("Пользователь 4 НЕ найден.", exception1.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего пользователя");
        final NotFoundException exception2 = assertThrows(
                NotFoundException.class,
                () -> userService.removeFriend(3, 1)
        );
        assertEquals("Пользователи 3 и 1 не находятся в списках друзей друг у друга.", exception2.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего пользователя");

    }

    @Test
    void getMutualFriendsListShouldReturnListOfMutualFriends() {
        User user3 = new User();
        user3.setEmail("eeee@ya.ru");
        user3.setLogin("eeee");
        user3.setBirthday(DateUtility.formatToDate("2000-11-10"));
        userService.addUser(users.get(0));
        userService.addUser(users.get(1));
        userService.addUser(user3);
        userService.addFriend(1,2);
        userService.addFriend(1,3);
        userService.addFriend(3,2);
        Assertions.assertEquals(userService.getCommonFriendsList(1,3), List.of(users.get(1)));
        Assertions.assertEquals(userService.getCommonFriendsList(1,3), List.of(users.get(1)));
    }

    @Test
    void getMutualFriendsListShouldThrowExceptionIfNotFound() {
        User user3 = new User();
        user3.setEmail("eeee@ya.ru");
        user3.setLogin("eeee");
        user3.setBirthday(DateUtility.formatToDate("2000-11-10"));
        userService.addUser(users.get(0));
        userService.addUser(users.get(1));
        userService.addUser(user3);
        userService.addFriend(1,2);
        userService.addFriend(1,3);
        userService.addFriend(3,2);
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getCommonFriendsList(4,1)
        );
        assertEquals("Пользователь 4 НЕ найден.", exception.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего пользователя");
    }

    @Test
    void getFriendListShouldReturnListOfFriendUsers() {
        User user3 = new User();
        user3.setEmail("eeee@ya.ru");
        user3.setLogin("eeee");
        user3.setBirthday(DateUtility.formatToDate("2000-11-10"));
        userService.addUser(users.get(0));
        userService.addUser(users.get(1));
        userService.addUser(user3);
        userService.addFriend(3, 1);
        userService.addFriend(3,2);
        Assertions.assertEquals(userService.getFriendList(3), users);
    }

    @Test
    void getFriendListShouldThrowExceptionIfNotFound() {
        User user3 = new User();
        user3.setEmail("eeee@ya.ru");
        user3.setLogin("eeee");
        user3.setBirthday(DateUtility.formatToDate("2000-11-10"));
        userService.addUser(users.get(0));
        userService.addUser(users.get(1));
        userService.addUser(user3);
        userService.addFriend(3, 1);
        userService.addFriend(3,2);
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getFriendList(4)
        );
        assertEquals("Пользователь 4 НЕ найден.", exception.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего пользователя");
    }

    void loadUsers() {
        User user1 = new User();
        user1.setEmail("aaaa@ya.ru");
        user1.setBirthday(DateUtility.formatToDate("2000-11-11"));
        user1.setLogin("aaaa");
        User user2 = new User();
        user2.setEmail("bbbb@ya.ru");
        user2.setLogin("bbbb");
        user2.setBirthday(DateUtility.formatToDate("2000-11-11"));
        users.add(user1);
        users.add(user2);
    }
}
