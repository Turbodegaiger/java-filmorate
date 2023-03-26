package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public boolean addLike(int filmId, int userId) {
        log.info("Получен запрос на лайк фильма {} пользователем {}.", filmId, userId);
        boolean isAdded = filmStorage.getFilm(filmId).getUsersLiked().add(userId);
        if (!isAdded) {
            log.info("Лайк пользователя {} уже существует на фильме {}.", userId, filmId);
            throw new AlreadyExistsException(String.format("Пользователь %s уже ставил лайк фильму %s", userId, filmId));
        }
        log.info("Фильму {} добавлен лайк пользователя {}.", filmId, userId);
        return isAdded;
    }

    public boolean removeLike(int filmId, int userId) {
        log.info("Получен запрос на удаление лайка фильма {} пользователем {}.", filmId, userId);
        boolean isRemoved = filmStorage.getFilm(filmId).getUsersLiked().remove(userId);
        if (!isRemoved) {
            log.info("Не найден лайк пользователя {} на фильме {}", userId, filmId);
            throw new NotFoundException(String.format("Пользователь %s не ставил лайк фильму %s", userId, filmId));
        }
        log.info("Лайк фильма {} пользователем {} удалён.", filmId, userId);
        return isRemoved;
    }

    public List<Film> getMostlyPopularFilms(int count) {
        log.info("Получен запрос на список самых популярных фильмов.");
        List<Film> mostlyPopularFilms = filmStorage.getFilms().stream()
                .sorted(Comparator.comparing(Film::getLikesCount).reversed())
                .limit(count)
                .collect(Collectors.toList());
        log.info("Список {} самых популярных фильмов: {}.", count, mostlyPopularFilms);
        return mostlyPopularFilms;
    }
}
