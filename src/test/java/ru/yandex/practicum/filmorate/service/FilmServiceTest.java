package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Order;
import ru.yandex.practicum.filmorate.util.DateUtility;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FilmServiceTest {
    private final FilmService filmService;
    private final UserService userService;

    List<Film> films;
    List<User> users;

    @BeforeEach
    void loadFilms() {
        films = new ArrayList<>();
        users = new ArrayList<>();
        Film film1 = new Film();
        film1.setName("Evil buba in da forest");
        film1.setReleaseDate(DateUtility.formatToDate("1999-11-12"));
        film1.setDescription("Evil buba is behind you. Always.");
        film1.setDuration(90L);
        film1.setGenres(List.of(new Genre(4,"Триллер")));
        film1.setMpa(new Mpa(4,"R"));
        Film film2 = new Film();
        film2.setName("aboba");
        film2.setReleaseDate(DateUtility.formatToDate("1999-12-11"));
        film2.setDuration(60L);
        film2.setDescription("222");
        film2.setMpa(new Mpa(4,"R"));
        films.add(film1);
        films.add(film2);
    }

    @Test
    @Order(1)
    void addFilmShouldReturnFilm() {
        Assertions.assertEquals(filmService.addFilm(films.get(0)), films.get(0));
    }

    @Test
    @Order(2)
    void addFilmShouldThrowExceptionIfAlreadyExists() {
        final AlreadyExistsException exception = assertThrows(
                AlreadyExistsException.class,
                () -> filmService.addFilm(films.get(0))
        );
        assertEquals("Ошибка при создании фильма - Evil buba in da forest с датой релиза 1999-11-12 уже существует.", exception.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего фильма");
    }

    @Test
    @Order(3)
    void getFilmsShouldReturnListOfFilms() {
        filmService.addFilm(films.get(1));
        Assertions.assertEquals(filmService.getFilms(), films);
    }

    @Test
    @Order(4)
    void getFilmShouldReturnCorrectFilm() {
        Assertions.assertEquals(filmService.getFilm(1), films.get(0));
    }

    @Test
    @Order(5)
    void getFilmShouldThrowExceptionIfNotFound() {
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmService.getFilm(3)
        );
        assertEquals("Ошибка при выгрузке фильма. Фильм [id 3] не найден.", exception.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего фильма");
    }

    @Test
    @Order(6)
    void updateFilmShouldReplaceOldFilm() {
        Film updated = new Film();
        updated.setId(1);
        updated.setName("Evil buba in da forest");
        updated.setReleaseDate(DateUtility.formatToDate("1992-11-12"));
        updated.setDescription("19");
        updated.setMpa(new Mpa(4, "R"));
        updated.setDuration(91L);
        Assertions.assertEquals(filmService.updateFilm(updated), filmService.getFilm(1));
    }

    @Test
    @Order(7)
    void updateFilmShouldThrowExceptionIfNotFound() {
        Film updated = new Film();
        updated.setId(3);
        updated.setName("eqeqe");
        updated.setReleaseDate(DateUtility.formatToDate("1992-11-12"));
        updated.setDescription("19");
        updated.setMpa(new Mpa(4, "R"));
        updated.setDuration(91L);
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmService.updateFilm(updated)
        );
        assertEquals("При обновлении фильма eqeqe произошла ошибка - [id 3] не найден.", exception.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего фильма");
    }

    @Test
    @Order(8)
    void addLikeShouldIncreaseLikesAndSaveUserToUsersLiked() {
        loadUsers();
        int expectedId = userService.addUser(users.get(0)).getId();
        filmService.addLike(1,expectedId);
        Assertions.assertEquals(filmService.getFilmLikes(1), List.of(users.get(0)));
    }

    @Test
    @Order(9)
    void addLikeShouldThrowExceptionIfNotFoundOrAlreadyExists() {
        loadUsers();
        final NotFoundException exception1 = assertThrows(
                NotFoundException.class,
                () -> filmService.addLike(0,1)
        );
        assertEquals("Невозможно выполнить запрос. Фильм [id 0] и/или пользователь [id 1] не найдены.", exception1.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего фильма или пользователя");
        final NotFoundException exception2 = assertThrows(
                NotFoundException.class,
                () -> filmService.addLike(2,5)
        );
        assertEquals("Невозможно выполнить запрос. Фильм [id 2] и/или пользователь [id 5] не найдены.", exception2.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего фильма или пользователя");
        final AlreadyExistsException exception3 = assertThrows(
                AlreadyExistsException.class,
                () -> filmService.addLike(1,1)
        );
        assertEquals("Лайк фильму [id 1] от пользователя [id 1] уже существует.", exception3.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего фильма или пользователя");
    }

    @Test
    @Order(10)
    void removeLikeShouldRemoveCorrectUsersLike() {
        loadUsers();
        filmService.removeLike(1,1);
        Assertions.assertEquals(List.of(), filmService.getFilmLikes(1));
    }

    @Test
    @Order(11)
    void removeLikeShouldThrowExceptionIfNotFound() {
        loadUsers();
        final NotFoundException exception1 = assertThrows(
                NotFoundException.class,
                () -> filmService.removeLike(1,3)
        );
        assertEquals("Ошибка при удалении лайка. Лайк фильму [id 1] от пользователя [id 3] не найден.", exception1.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего фильма или пользователя");

        final NotFoundException exception2 = assertThrows(
                NotFoundException.class,
                () -> filmService.removeLike(2,1)
        );
        assertEquals("Ошибка при удалении лайка. Лайк фильму [id 2] от пользователя [id 1] не найден.", exception2.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего фильма или пользователя");

        final NotFoundException exception3 = assertThrows(
                NotFoundException.class,
                () -> filmService.removeLike(1,1)
        );
        assertEquals("Ошибка при удалении лайка. Лайк фильму [id 1] от пользователя [id 1] не найден.", exception3.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего фильма или пользователя");
    }

    @Test
    @Order(12)
    void getMostlyPopularFilmsShouldReturnListOfFilmsSortedByLikes() {
        loadUsers();
        Film film3 = new Film();
        film3.setName("aboba111");
        film3.setReleaseDate(DateUtility.formatToDate("2000-11-12"));
        film3.setDuration(65L);
        film3.setMpa(new Mpa(4, "R"));
        film3.setDescription("333");
        filmService.addFilm(film3);
        userService.addUser(users.get(1));
        List<Film> sorted = new ArrayList<>();
        sorted.add(filmService.getFilm(3));
        sorted.add(filmService.getFilm(1));
        sorted.add(filmService.getFilm(2));
        filmService.addLike(3, 1);
        filmService.addLike(1, 1);
        filmService.addLike(3, 2);
        Assertions.assertEquals(sorted, filmService.getMostlyPopularFilms(3));
        filmService.removeAllFilms();
        userService.removeAllUsers();
    }

    private void loadUsers() {
        User user1 = new User();
        user1.setEmail("aaaa1@ya.ru");
        user1.setBirthday(DateUtility.formatToDate("2000-11-11"));
        user1.setLogin("aaaa1");
        User user2 = new User();
        user2.setEmail("bbbb@ya.ru");
        user2.setLogin("bbbb");
        user2.setBirthday(DateUtility.formatToDate("2000-11-11"));
        users.add(user1);
        users.add(user2);
    }
}
