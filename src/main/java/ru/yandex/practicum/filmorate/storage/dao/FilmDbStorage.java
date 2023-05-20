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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
            log.info("Ошибка при создании фильма - {} с датой релиза {} уже существует.",
                    film.getName(), film.getReleaseDate());
            throw new AlreadyExistsException(
                    String.format("Ошибка при создании фильма - %s с датой релиза %s уже существует.",
                            film.getName(), film.getReleaseDate()));
        }
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?);";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId());
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
        log.info("Фильм {} c датой выхода {} добавлен в базу данных.", film.getName(), film.getReleaseDate());
        return getFilm(filmId);
    }

    @Override
    public List<Film> getFilms() {
        String sql = "SELECT * FROM films f, mpa m WHERE f.mpa_id = m.mpa_id;";
        List<Film> filmList = jdbcTemplate.query(sql, (rs, rowNum) -> Mapper.film.mapRow(rs, rowNum));
        log.info("Из базы данных выгружен список всех фильмов размером {} записей.", filmList.size());
        return filmList;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films " +
                "SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                "WHERE film_id = ?; " +
                "DELETE FROM film_genres " +
                "WHERE film_id = ?; ";
        int rowsAffected = jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId(), film.getId());
        if (rowsAffected <= 0) {
            log.info("При обновлении фильма {} произошла ошибка - [id {}] не найден.", film.getName(), film.getId());
            throw new NotFoundException(String.format("При обновлении фильма %s произошла ошибка - [id %s] не найден.",
                    film.getName(), film.getId()));
        }
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?,?);";
            for (Genre genre : film.getGenres().stream().distinct().collect(Collectors.toList())) {
                jdbcTemplate.update(sql,
                        film.getId(),
                        genre.getId());
            }
        }
        log.info("Фильм {} с [id {}] обновлён.", film.getName(), film.getId());
        return getFilm(film.getId());
    }

    @Override
    public Film getFilm(int filmId) {
        String sql = "SELECT * FROM films f, mpa m WHERE f.film_id = ? AND m.mpa_id = f.mpa_id;";
        List<Film> film = jdbcTemplate.query(sql, (rs, rowNum) -> Mapper.film.mapRow(rs, rowNum), filmId);
        if (film.size() != 1) {
            log.info("Ошибка при выгрузке фильма. Фильм [id {}] не найден.", filmId);
            throw new NotFoundException(String.format("Ошибка при выгрузке фильма. Фильм [id %s] не найден.", filmId));
        }
        log.info("Из базы данных выгружен фильм {} [id {}].", film.get(0).getName(), film.get(0).getId());
        return film.get(0);
    }

    @Override
    public void removeFilm(int filmId) {
        String sql = "DELETE FROM films WHERE film_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, filmId);
        if (rowsAffected <= 0) {
            log.info("Ошибка при удалении. Фильм [id {}] не найден.", filmId);
            throw new NotFoundException(String.format("Ошибка при удалении. Фильм [id %s] не найден.", filmId));
        }
        log.info("Из базы данных удалён фильм [id {}].", filmId);
    }

    @Override
    public Set<Integer> getFilmLikes(int filmId) {
        Set<Integer> usersLiked = new HashSet<>();
        String sql = "SELECT user_id FROM users_liked WHERE film_id = ?";
        SqlRowSet usersLikedRows = jdbcTemplate.queryForRowSet(sql, filmId);
        if (usersLikedRows.next()) {
            do {
                usersLiked.add(usersLikedRows.getInt("user_id"));
            } while (usersLikedRows.next());
        }
        log.info("Из базы данных выгружены лайки фильму [id {}], всего {} лайков.", filmId, usersLiked.size());
        return usersLiked;
    }

    @Override
    public List<Film> getMostlyPopular(int count) {
        String sql = "SELECT * FROM films f, mpa m WHERE m.mpa_id = f.mpa_id ORDER BY rate DESC LIMIT ?";
        List<Film> popularFilms = jdbcTemplate.query(sql, (rs, rowNum) -> Mapper.film.mapRow(rs, rowNum), count);
        log.info("Из базы данных выгружен список популярных фильмов размером {} записей.", popularFilms.size());
        return popularFilms;
    }

    public List<Genre> getFilmGenres(int filmId) {
        String sql = "SELECT * FROM film_genres fg, genres g WHERE fg.film_id = ? AND g.genre_id = fg.genre_id;";
        List<Genre> filmGenres = jdbcTemplate.query(sql, (rs, rowNum) -> Mapper.genre.mapRow(rs, rowNum), filmId);
        log.info("Из базы данных выгружен список жанров фильма [id {}] размером {} записей.", filmId, filmGenres.size());
        return filmGenres;
    }

    @Override
    public void removeAllFilms() {
        jdbcTemplate.update("DELETE FROM films;");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1;");
    }
}
