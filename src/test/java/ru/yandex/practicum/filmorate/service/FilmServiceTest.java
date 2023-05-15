package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.date.DateUtility;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.inmem.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.inmem.InMemoryUserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilmServiceTest {
    UserService userService;
    FilmService filmService;
    List<Film> films = new ArrayList<>();
    List<User> users = new ArrayList<>();

    @BeforeEach
    void setParameters() {
        UserStorage storage = new InMemoryUserStorage();
        userService = new UserService(storage);
        filmService = new FilmService(
                new InMemoryFilmStorage(),
                storage);
        films = new ArrayList<>();
        users = new ArrayList<>();
    }

    @BeforeEach
    void loadFilms() {
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
        films.add(film1);
        films.add(film2);
    }

    @Test
    void addFilmShouldReturnFilm() {
        Assertions.assertEquals(filmService.addFilm(films.get(0)), films.get(0));
    }

    @Test
    void addFilmShouldThrowExceptionIfAlreadyExists() {
        filmService.addFilm(films.get(0));
        final AlreadyExistsException exception = assertThrows(
                AlreadyExistsException.class,
                () -> filmService.addFilm(films.get(0))
        );
        assertEquals("Фильм Evil buba in da forest уже существует.", exception.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего фильма");
    }

    @Test
    void getFilmsShouldReturnListOfFilms() {
        filmService.addFilm(films.get(0));
        filmService.addFilm(films.get(1));
        Assertions.assertEquals(filmService.getFilms(), films);
    }

    @Test
    void getFilmShouldReturnCorrectFilm() {
        filmService.addFilm(films.get(0));
        Assertions.assertEquals(filmService.getFilm(1), films.get(0));
    }

    @Test
    void getFilmShouldThrowExceptionIfNotFound() {
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmService.getFilm(1)
        );
        assertEquals("Фильм [id 1] НЕ найден.", exception.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего фильма");
    }

    @Test
    void updateFilmShouldReplaceOldFilm() {
        filmService.addFilm(films.get(0));
        Film updated = new Film();
        updated.setId(1);
        updated.setName("eqeqe");
        updated.setReleaseDate(DateUtility.formatToDate("1992-11-12"));
        updated.setDescription("19");
        updated.setDuration(91L);
        Assertions.assertEquals(filmService.updateFilm(updated), filmService.getFilm(1));
    }

    @Test
    void updateFilmShouldThrowExceptionIfNotFound() {
        filmService.addFilm(films.get(0));
        Film updated = new Film();
        updated.setId(2);
        updated.setName("eqeqe");
        updated.setReleaseDate(DateUtility.formatToDate("1992-11-12"));
        updated.setDescription("19");
        updated.setDuration(91L);
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmService.updateFilm(updated)
        );
        assertEquals("Фильм [id 2] НЕ найден.", exception.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего фильма");
    }

    @Test
    void addLikeShouldIncreaseLikesAndSaveUserToUsersLiked() {
        loadUsers();
        filmService.addFilm(films.get(0));
        userService.addUser(users.get(0));
        filmService.addLike(1,1);
        Assertions.assertEquals(filmService.getFilm(1).getUsersLiked(), Set.of(users.get(0).getId()));
    }

    @Test
    void addLikeShouldThrowExceptionIfNotFoundOrAlreadyExists() {
        loadUsers();
        filmService.addFilm(films.get(0));
        final NotFoundException exception1 = assertThrows(
                NotFoundException.class,
                () -> filmService.addLike(1,1)
        );
        assertEquals("Пользователь [id 1] НЕ найден.", exception1.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего фильма или пользователя");
        userService.addUser(users.get(0));
        User user = userService.getUser(1);
        final NotFoundException exception2 = assertThrows(
                NotFoundException.class,
                () -> filmService.addLike(2,1)
        );
        assertEquals("Фильм [id 2] НЕ найден.", exception2.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего фильма или пользователя");
        filmService.addLike(1,1);
        final AlreadyExistsException exception3 = assertThrows(
                AlreadyExistsException.class,
                () -> filmService.addLike(1,1)
        );
        assertEquals("Пользователь [id 1] уже ставил лайк фильму [id 1]", exception3.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего фильма или пользователя");
    }

    @Test
    void removeLikeShouldRemoveCorrectUsersLike() {
        loadUsers();
        userService.addUser(users.get(0));
        userService.addUser(users.get(1));
        filmService.addFilm(films.get(0));
        filmService.addLike(1,1);
        filmService.addLike(1,2);
        filmService.removeLike(1,1);
        Assertions.assertEquals(filmService.getFilm(1).getUsersLiked(), Set.of(users.get(1).getId()));
    }

    @Test
    void removeLikeShouldThrowExceptionIfNotFound() {
        loadUsers();
        userService.addUser(users.get(0));
        userService.addUser(users.get(1));
        filmService.addFilm(films.get(0));
        filmService.addLike(1,1);
        final NotFoundException exception1 = assertThrows(
                NotFoundException.class,
                () -> filmService.removeLike(1,3)
        );
        assertEquals("Пользователь [id 3] НЕ найден.", exception1.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего фильма или пользователя");

        final NotFoundException exception2 = assertThrows(
                NotFoundException.class,
                () -> filmService.removeLike(2,1)
        );
        assertEquals("Фильм [id 2] НЕ найден.", exception2.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего фильма или пользователя");

        final NotFoundException exception3 = assertThrows(
                NotFoundException.class,
                () -> filmService.removeLike(1,2)
        );
        assertEquals("Пользователь [id 2] не ставил лайк фильму [id 1]", exception3.getMessage(),
                "Не возникает исключение при попытке нахождения несуществующего фильма или пользователя");
    }

    @Test
    void getMostlyPopularFilmsShouldReturnListOfFilmsSortedByLikes() {
        loadUsers();
        Film film3 = new Film();
        film3.setName("aboba111");
        film3.setReleaseDate(DateUtility.formatToDate("2000-11-12"));
        film3.setDuration(65L);
        film3.setDescription("333");
        List<Film> sorted = new ArrayList<>();
        sorted.add(film3);
        sorted.add(films.get(0));
        sorted.add(films.get(1));
        userService.addUser(users.get(0));
        userService.addUser(users.get(1));
        filmService.addFilm(films.get(0));
        filmService.addFilm(films.get(1));
        filmService.addFilm(film3);
        filmService.addLike(3, 1);
        filmService.addLike(1, 1);
        filmService.addLike(3, 2);
        Assertions.assertEquals(filmService.getMostlyPopularFilms(3), sorted);
    }

    private void loadUsers() {
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
