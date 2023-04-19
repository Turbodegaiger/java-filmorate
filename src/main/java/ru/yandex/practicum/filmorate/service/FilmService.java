package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.date.DateUtility;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage, JdbcTemplate jdbcTemplate) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
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

    public void removeFilm(int filmId) {filmStorage.removeFilm(filmId);}

    public void addLike(int filmId, int userId) {
        userStorage.getUser(userId);
        boolean isLiked = filmStorage.getFilmLikes(filmId).contains(userId);
        if (isLiked) {
            log.info("Лайк пользователя {} уже существует на фильме {}.", userId, filmId);
            throw new AlreadyExistsException(String.format("Пользователь %s уже ставил лайк фильму %s", userId, filmId));
        }
        filmStorage.addLike(filmId, userId);
        log.info("Фильму {} добавлен лайк пользователя {}.", filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        userStorage.getUser(userId);
        boolean isLiked = filmStorage.getFilmLikes(filmId).contains(userId);
        if (!isLiked) {
            log.info("Не найден лайк пользователя {} на фильме {}", userId, filmId);
            throw new NotFoundException(String.format("Пользователь %s не ставил лайк фильму %s", userId, filmId));
        }
        filmStorage.removeLike(filmId, userId);
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

    public Film createSampleFilm() {
        Film film1 = new Film();
        film1.setName("Evil buba in da forest");
        film1.setReleaseDate(DateUtility.formatToDate("1999-11-12"));
        film1.setDescription("Evil buba is behind you. Always.");
        film1.setDuration(90L);
        film1.setGenre("Триллер");
        film1.setRating("R");
        addFilm(film1);
        log.info("Создан фильм-образец.");
        return film1;
    }
}
