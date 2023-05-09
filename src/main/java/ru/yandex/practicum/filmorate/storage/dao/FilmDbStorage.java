package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.Mapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.validator.Validator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component("filmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film addFilm(Film film) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT name FROM films WHERE name = ? AND release_date = ?",
                film.getName(), film.getReleaseDate());
        if (filmRows.next()) {
            log.info("Ошибка при создании фильма - {} с датой релиза {} уже существует.", film.getName(), film.getReleaseDate());
            throw new AlreadyExistsException(
                    String.format("Ошибка при создании фильма - %s с датой релиза %s уже существует.",
                            film.getName(), film.getReleaseDate()));
        }
        Validator.validate(film);
        String sql = "INSERT INTO films (name, description, release_date, duration) " +
                "VALUES (?, ?, ?, ?);";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration());
        SqlRowSet filmIdRows = jdbcTemplate.queryForRowSet("SELECT film_id FROM films WHERE name = ? AND release_date = ?;",
                film.getName(), film.getReleaseDate());
        Integer filmId = 0;
        if (filmIdRows.next()) {
            filmId = filmIdRows.getInt("film_id");
        }
        sql = "INSERT INTO film_genres (film_id, genre_id) " +
                "VALUES (?, (SELECT genre_id FROM genres WHERE genre_id = ?));";
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(sql,
                        filmId,
                        genre.getId());
            }
        }
        sql = "INSERT INTO film_mpa (film_id, mpa_id) " +
                "VALUES (?, (SELECT mpa_id FROM mpa WHERE mpa_id = ?));";
        jdbcTemplate.update(sql,
                filmId,
                film.getMpa().getId());
        log.info("Фильм {} c датой выхода {} добавлен в базу данных.", film.getName(), film.getReleaseDate());
        return getFilm(filmId);
    }

    @Override
    public List<Film> getFilms() {
        List<Film> filmList = new ArrayList<>();
        String sql = "SELECT film_id FROM films;";
        SqlRowSet filmIdRows = jdbcTemplate.queryForRowSet("SELECT film_id FROM films;");
        while (filmIdRows.next()) {
            filmList.add(getFilm(filmIdRows.getInt("film_id")));
        }
        log.info("Из базы данных выгружен список всех фильмов размером {} записей.", filmList.size());
        return filmList;
    }

    @Override
    public Film updateFilm(Film film) {
        checkFilmExistence(film.getId());
        Validator.validate(film);
        String sql = "UPDATE films " +
                "SET name = ?, description = ?, release_date = ?, duration = ? " +
                "WHERE film_id = ?; " +
                "DELETE FROM film_genres " +
                "WHERE film_id = ?; " +
                "DELETE FROM film_mpa " +
                "WHERE film_id = ?;";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getId(), film.getId(), film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?,?);";
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(sql,
                        film.getId(),
                        genre.getId());
            }
        }
        if (film.getMpa() != null) {
            sql = "INSERT INTO film_mpa (film_id, mpa_id) VALUES (?,?);";
            jdbcTemplate.update(sql,
                    film.getId(),
                    film.getMpa().getId());
        }
        log.info("Фильм {} с [id {}] обновлён.", film.getName(), film.getId());
        return getFilm(film.getId());
    }

    @Override
    public Film getFilm(int filmId) {
        checkFilmExistence(filmId);
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
                "(SELECT genre_id FROM genres AS g WHERE g.genre_id = fg.genre_id) AS genre_id, " +
                "(SELECT name FROM genres AS g WHERE g.genre_id = fg.genre_id) AS genres, " +
                "(SELECT mpa_id FROM mpa AS r WHERE r.mpa_id = fm.mpa_id) AS mpa_id," +
                "(SELECT name FROM mpa AS r WHERE r.mpa_id = fm.mpa_id) AS mpa " +
                "FROM films AS f " +
                "LEFT JOIN film_genres AS fg ON f.film_id=fg.film_id " +
                "LEFT JOIN film_mpa AS fm ON f.film_id=fm.film_id " +
                "WHERE f.film_id=?;";
        List<Film> film = jdbcTemplate.query(sql, (rs, rowNum) -> Mapper.film.mapRow(rs, rowNum), filmId);
        log.info("Из базы данных выгружен фильм {} [id] {}.", film.get(0).getName(), film.get(0).getId());
        return film.get(0);
    }

    @Override
    public void removeFilm(int filmId) {
        checkFilmExistence(filmId);
        String sql = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
        log.info("Из базы данных удалён фильм [id] {}.", filmId);
    }

    @Override
    public Set<Integer> getFilmLikes(int filmId) {
        checkFilmExistence(filmId);
        Set<Integer> usersLiked = new HashSet<>();
        String sql = "SELECT user_id FROM users_liked WHERE film_id = ?";
        SqlRowSet usersLikedRows = jdbcTemplate.queryForRowSet(sql,
                filmId);
        for (int i = 1; usersLikedRows.next(); i++) {
            usersLiked.add(usersLikedRows.getInt(i));
        }
        log.info("Из базы данных выгружены лайки фильму [id] {}, всего {} лайков.", filmId, usersLiked.size());
        return usersLiked;
    }

    @Override
    public void addLike(int filmId, int userId) {
        checkFilmExistence(filmId);
        String sql = "INSERT INTO users_liked (film_id, user_id) VALUES (?,?);";
        jdbcTemplate.update(sql, filmId, userId);
        log.info("В базу данных добавлен лайк фильму [id {}] от пользователя [id] {}", filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        checkFilmExistence(filmId);
        String sql = "DELETE FROM users_liked WHERE film_id=? AND user_id=?;";
        jdbcTemplate.update(sql, filmId, userId);
        log.info("Из базы данных удалён лайк фильму [id {}] от пользователя [id] {}", filmId, userId);
    }

    private void checkFilmExistence(int filmId) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT film_id FROM films WHERE film_id = ?",
                filmId);
        if (!filmRows.next()) {
            log.info("Произошла ошибка. Фильма с [id {}] не существует.", filmId);
            throw new NotFoundException(
                    String.format("Произошла ошибка. Фильма с [id %s] не существует.", filmId));
        }
    }
}
