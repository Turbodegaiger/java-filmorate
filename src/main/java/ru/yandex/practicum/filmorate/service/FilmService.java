package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
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
        userStorage.getUser(userId);
        boolean isLiked = filmStorage.getFilmLikes(filmId).contains(userId);
        if (isLiked) {
            log.info("Лайк пользователя [id {}] уже существует на фильме [id {}].", userId, filmId);
            throw new AlreadyExistsException(String.format("Пользователь [id %s] уже ставил лайк фильму [id %s]", userId, filmId));
        }
        filmStorage.addLike(filmId, userId);
        log.info("Фильму [id {}] добавлен лайк пользователя [id {}].", filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        userStorage.getUser(userId);
        boolean isLiked = filmStorage.getFilmLikes(filmId).contains(userId);
        if (!isLiked) {
            log.info("Не найден лайк пользователя [id {}] на фильме [id {}]", userId, filmId);
            throw new NotFoundException(String.format("Пользователь [id %s] не ставил лайк фильму [id %s]", userId, filmId));
        }
        filmStorage.removeLike(filmId, userId);
        log.info("Лайк фильма [id {}] пользователем [id {}] удалён.", filmId, userId);
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
