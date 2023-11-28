package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dao.LikesDbStorage;
import ru.yandex.practicum.filmorate.validator.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikesDbStorage likesDbStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       LikesDbStorage likesDbStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.likesDbStorage = likesDbStorage;
    }

    public Film addFilm(Film film) {
        Validator.validate(film);
        Film newFilm = filmStorage.addFilm(film);
        newFilm.setGenres(filmStorage.getFilmGenres(newFilm.getId()));
        return newFilm;
    }

    public List<Film> getFilms() {
        List<Film> films = filmStorage.getFilms();
        for (Film film : films) {
            film.setGenres(filmStorage.getFilmGenres(film.getId()));
        }
        return films;
    }

    public Film getFilm(int id) {
        Film film = filmStorage.getFilm(id);
        film.setGenres(filmStorage.getFilmGenres(id));
        return film;
    }

    public Film updateFilm(Film film) {
        Validator.validate(film);
        Film updatedFilm = filmStorage.updateFilm(film);
        updatedFilm.setGenres(filmStorage.getFilmGenres(updatedFilm.getId()));
        return updatedFilm;
    }

    public void removeFilm(int filmId) {
        filmStorage.removeFilm(filmId);
    }

    public List<User> getFilmLikes(int filmId) {
        List<User> users = new ArrayList<>();
        Set<Integer> usersId = filmStorage.getFilmLikes(filmId);
        for (Integer id : usersId) {
            users.add(userStorage.getUser(id).orElse(new User()));
        }
        return users;
    }

    public void addLike(int filmId, int userId) {
        likesDbStorage.addLike(filmId, userId);
        log.info("Фильму [id {}] добавлен лайк пользователя [id {}].", filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        likesDbStorage.removeLike(filmId, userId);
        log.info("Лайк фильма [id {}] пользователем [id {}] удалён.", filmId, userId);
    }

    public void removeAllFilms() {
        filmStorage.removeAllFilms();
    }

    public List<Film> getMostlyPopularFilms(int count) {
        List<Film> mostlyPopularFilms = filmStorage.getMostlyPopular(count);
        for (Film film : mostlyPopularFilms) {
            film.setGenres(filmStorage.getFilmGenres(film.getId()));
        }
        log.info("Список {} самых популярных фильмов: {}.", count, mostlyPopularFilms);
        return mostlyPopularFilms;
    }
}
