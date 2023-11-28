package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.util.DateUtility;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(2)
public class UserServiceTest {
    private final UserService userService;
    List<User> users;

    @BeforeEach
    void setParameters() {
        users = new ArrayList<>();
        loadUsers();
    }

    @Test
    @Order(1)
    void addUserShouldReturnUser() {
        Assertions.assertEquals(userService.addUser(users.get(0)), users.get(0));
    }

    @Test
    @Order(2)
    void addUserShouldThrowExceptionIfAlreadyExists() {
        final AlreadyExistsException exception = assertThrows(
                AlreadyExistsException.class,
                () -> userService.addUser(users.get(0))
        );
        assertEquals("Ошибка при создании пользователя - аккаунт с email aaaa@ya.ru или логином aaaa уже существует.",
                exception.getMessage(),
                "Не возникает исключение при попытке добавления уже существующего пользователя");
    }

    @Test
    @Order(3)
    void getUsersShouldReturnListOfUsers() {
        userService.addUser(users.get(1));
        Assertions.assertEquals(userService.getUsers(), users);
    }

    @Test
    @Order(4)
    void getUserShouldReturnCorrectUser() {
        Assertions.assertEquals(userService.getUser(1), users.get(0));
    }

    @Test
    @Order(5)
    void getUserShouldThrowExceptionIfNotFound() {
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getUser(3)
        );
        assertEquals("Ошибка при выгрузке. Пользователь [id 3] не найден.", exception.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего пользователя");
    }

    @Test
    @Order(6)
    void updateUserShouldReplaceOldUser() {
        User updated = new User();
        updated.setId(1);
        updated.setLogin("eqeqe");
        updated.setEmail("aaaa@ya.ru");
        updated.setBirthday(DateUtility.formatToDate("2000-11-11"));
        Assertions.assertEquals(userService.updateUser(updated), userService.getUser(1));
    }

    @Test
    @Order(7)
    void updateUserShouldThrowExceptionIfNotFound() {
        User updated = new User();
        updated.setId(4);
        updated.setLogin("eqeqe");
        updated.setEmail("bugi-wugi@ya.ru");
        updated.setBirthday(DateUtility.formatToDate("2000-11-11"));
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.updateUser(updated)
        );
        assertEquals("Ошибка при обновлении. Пользователь [id 4] не найден.", exception.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего пользователя");
    }

    @Test
    @Order(8)
    void addFriendShouldAddUserIdToFriendsSet() {
        userService.addFriend(1, 2);
        Assertions.assertEquals(List.of(userService.getUser(2)), userService.getFriendList(1));
        Assertions.assertEquals(List.of(), userService.getFriendList(2));
    }

    @Test
    @Order(9)
    void addFriendShouldThrowExceptionIfNotFound() {
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.addFriend(4, 1)
        );
        assertEquals("Невозможно выполнить запрос. Пользователь [id 4] и/или пользователь [id 1] не найдены.",
                exception.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего пользователя");
    }

    @Test
    @Order(10)
    void addFriendShouldThrowExceptionIfAlreadyExists() {
        final AlreadyExistsException exception = assertThrows(
                AlreadyExistsException.class,
                () -> userService.addFriend(1, 2)
        );
        assertEquals("Пользователь [id 1] уже имеет в друзьях пользователя [id 2]", exception.getMessage(),
                "Не возникает исключение при попытке повторного добавления в друзья");
    }

    @Test
    @Order(11)
    void removeFriendShouldRemoveUserIdFromFriendsSet() {
        userService.removeFriend(1,2);
        Assertions.assertEquals(userService.getUser(1).getFriends(), Set.of());
        Assertions.assertEquals(userService.getUser(2).getFriends(), Set.of());
    }

    @Test
    @Order(12)
    void removeFriendShouldThrowExceptionIfNotFound() {
        User user3 = new User();
        user3.setEmail("eeee@ya.ru");
        user3.setLogin("eeee");
        user3.setBirthday(DateUtility.formatToDate("2000-11-10"));
        userService.addUser(user3);
        userService.addFriend(1, 2);
        final NotFoundException exception1 = assertThrows(
                NotFoundException.class,
                () -> userService.removeFriend(4, 1)
        );
        assertEquals("Ошибка при удалении из друзей. Пользователь [id 4] не имеет в друзьях [id 1].",
                exception1.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего пользователя");
        final NotFoundException exception2 = assertThrows(
                NotFoundException.class,
                () -> userService.removeFriend(3, 1)
        );
        assertEquals("Ошибка при удалении из друзей. Пользователь [id 3] не имеет в друзьях [id 1].",
                exception2.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего пользователя");

    }

    @Test
    @Order(13)
    void getMutualFriendsListShouldReturnListOfMutualFriends() {
        userService.addFriend(1,3);
        userService.addFriend(3,2);
        Assertions.assertEquals(userService.getCommonFriendsList(1,3), List.of(users.get(1)));
        Assertions.assertEquals(userService.getCommonFriendsList(1,3), List.of(users.get(1)));
    }

    @Test
    @Order(14)
    void getMutualFriendsListShouldReturnEmptySetIfNoSuchFriends() {
        Assertions.assertEquals(List.of(), userService.getCommonFriendsList(5,1));
    }

    @Test
    @Order(15)
    void getFriendListShouldReturnListOfFriendUsers() {
        userService.addFriend(3, 1);
        Assertions.assertEquals(users, userService.getFriendList(3));
    }

    @Test
    @Order(16)
    void getFriendListShouldThrowExceptionIfNotFound() {
        Assertions.assertEquals(List.of(), userService.getFriendList(4));
        userService.removeAllUsers();
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
