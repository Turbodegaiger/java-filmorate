package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.date.DateUtility;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTests {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private List<Film> testFilms;
    private List<User> testUsers;
    private List<Film> testUpdateFilms;

    @BeforeEach
    public void loadFilms() {
        testFilms = new ArrayList<>();
        testUsers = new ArrayList<>();
        testUpdateFilms = new ArrayList<>();
        Film film1 = new Film();
        film1.setName("aaa");
        film1.setDescription("aaa1");
        film1.setReleaseDate(DateUtility.formatToDate("1981-03-08"));
        film1.setDuration(80L);
        film1.setGenres(List.of(new Genre(1, "Комедия"), new Genre(3, "Мультфильм")));
        film1.setMpa(new Mpa(1, "G"));
        Film film2 = new Film();
        film2.setName("bbb");
        film2.setDescription("bbb1");
        film2.setReleaseDate(DateUtility.formatToDate("1988-03-10"));
        film2.setDuration(70L);
        film2.setGenres(List.of(new Genre(1, "Комедия"), new Genre(6, "Боевик")));
        film2.setMpa(new Mpa(2, "PG"));
        testFilms.add(film1);
        testFilms.add(film2);
        Film film3 = new Film();
        film3.setId(1);
        film3.setName("aaa");
        film3.setDescription("aaa12345");
        film3.setReleaseDate(DateUtility.formatToDate("1988-03-10"));
        film3.setDuration(100L);
        film3.setGenres(List.of(new Genre(1, "Комедия"), new Genre(6, "Боевик")));
        film3.setMpa(new Mpa(2, "PG"));
        Film film4 = new Film();
        film4.setId(6);
        film4.setName("ccc");
        film4.setDescription("ccc12345");
        film4.setReleaseDate(DateUtility.formatToDate("2000-03-10"));
        film4.setDuration(110L);
        film4.setGenres(List.of(new Genre(1, "Комедия"), new Genre(6, "Боевик")));
        film4.setMpa(new Mpa(2, "PG"));
        Film film5 = new Film();
        film5.setId(3);
        film5.setName("ddd");
        film5.setDescription("ddd12345");
        film5.setReleaseDate(DateUtility.formatToDate("2010-03-10"));
        film5.setDuration(110L);
        film5.setGenres(List.of(new Genre(1, "Комедия"), new Genre(6, "Боевик")));
        film5.setMpa(new Mpa(2, "PG"));
        testUpdateFilms.add(film3);
        testUpdateFilms.add(film4);
        testUpdateFilms.add(film5);
    }

    @Test
    public void testAddFilmAndGetFilm() {
        Film newFilm = new Film(
                "adada", "addad11", DateUtility.formatToDate("1995-05-05"), 75L, new Mpa(4, "R"));
        Film addedFilm = filmStorage.addFilm(newFilm);
        assertThat(filmStorage.getFilm(addedFilm.getId())).isEqualTo(newFilm);
    }

    @Test
    public void testAddAlreadyExistFilm() {
        assertThatExceptionOfType(AlreadyExistsException.class).isThrownBy(() -> filmStorage.addFilm(testFilms.get(1)));
    }

    @Test
    public void testWrongGetFilm() {
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> filmStorage.getFilm(0));
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> filmStorage.getFilm(5));
    }

    @Test
    public void testGetFilms() {
        filmStorage.addFilm(testFilms.get(0));
        filmStorage.addFilm(testFilms.get(1));
        assertThat(filmStorage.getFilms()).containsAll(testFilms);
    }

    @Test
    public void testUpdateFilm() {
        assertThat(filmStorage.updateFilm(testUpdateFilms.get(0))).isEqualTo(testUpdateFilms.get(0));
        assertThat(filmStorage.getFilm(1)).isEqualTo(testUpdateFilms.get(0));
    }

    @Test
    public void testWrongUpdateFilm() {
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> filmStorage.updateFilm(testUpdateFilms.get(1)));
    }

    @Test
    public void testRemoveFilm() {
        filmStorage.addFilm(testUpdateFilms.get(2));
        assertThat(filmStorage.getFilm(3)).isEqualTo(testUpdateFilms.get(2));
        filmStorage.removeFilm(3);
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> filmStorage.getFilm(3));
    }

    @Test
    public void testWrongRemoveFilm() {
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> filmStorage.removeFilm(3));
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> filmStorage.removeFilm(0));
    }

    @Test
    public void testAddGetAndRemoveFilmLikes() {
        loadUsers();
        userStorage.addUser(testUsers.get(0));
        userStorage.addUser(testUsers.get(1));
        assertThat(filmStorage.getFilmLikes(1)).isEqualTo(Set.of());
        filmStorage.addLike(1, 1);
        assertThat(filmStorage.getFilmLikes(1)).isEqualTo(Set.of(1));
        filmStorage.addLike(1, 2);
        assertThat(filmStorage.getFilmLikes(1)).isEqualTo(Set.of(1,2));
        filmStorage.removeLike(1,1);
        filmStorage.removeLike(1,2);
        assertThat(filmStorage.getFilmLikes(1)).isEqualTo(Set.of());
    }

    @Test
    public void testWrongAddGetAndRemoveFilmLikes() {
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> filmStorage.addLike(1, 3));
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> filmStorage.addLike(1, 0));
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> filmStorage.addLike(0, 1));
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> filmStorage.addLike(5, 1));
        filmStorage.addLike(1, 1);
        assertThatExceptionOfType(AlreadyExistsException.class).isThrownBy(() -> filmStorage.addLike(1, 1));
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> filmStorage.removeLike(1, 0));
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> filmStorage.removeLike(1, 5));
        filmStorage.removeLike(1,1);
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> filmStorage.removeLike(1, 1));
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
        testUsers.add(user1);
        testUsers.add(user2);
    }
}
