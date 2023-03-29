package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilm(int id) {
        return filmStorage.getFilm(id);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public void addLike(int filmId, int userId) {
        if (userStorage.getUser(userId) == null) {
            throw new NotFoundException("Пользователь " + userId + " НЕ найден.");
        }
        boolean isAdded = filmStorage.getFilm(filmId).getUsersLiked().add(userId);
        if (!isAdded) {
            log.info("Лайк пользователя {} уже существует на фильме {}.", userId, filmId);
            throw new AlreadyExistsException(String.format("Пользователь %s уже ставил лайк фильму %s", userId, filmId));
        }
        log.info("Фильму {} добавлен лайк пользователя {}.", filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        if (userStorage.getUser(userId) == null) {
            throw new NotFoundException("Пользователь " + userId + " НЕ найден.");
        }
        boolean isRemoved = filmStorage.getFilm(filmId).getUsersLiked().remove(userId);
        if (!isRemoved) {
            log.info("Не найден лайк пользователя {} на фильме {}", userId, filmId);
            throw new NotFoundException(String.format("Пользователь %s не ставил лайк фильму %s", userId, filmId));
        }
        log.info("Лайк фильма {} пользователем {} удалён.", filmId, userId);
    }

    public List<Film> getMostlyPopularFilms(int count) {
        List<Film> mostlyPopularFilms = filmStorage.getFilms().stream()
                .sorted(Comparator.comparing(Film::getLikesCount).reversed())
                .limit(count)
                .collect(Collectors.toList());
        log.info("Список {} самых популярных фильмов: {}.", count, mostlyPopularFilms);
        return mostlyPopularFilms;
    }
}
