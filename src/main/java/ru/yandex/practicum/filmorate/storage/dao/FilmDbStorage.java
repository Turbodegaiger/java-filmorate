package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.validator.Validator;

import java.sql.ResultSet;
import java.sql.SQLException;
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
        Validator.validate(film);
        String name = jdbcTemplate.query("SELECT name FROM films WHERE name = ? AND release_date = ?",
                this::makeString, film.getName(), film.getReleaseDate());
        if (name != null) {
            log.info("Ошибка при создании фильма - {} с датой релиза {} уже существует.", film.getName(), film.getReleaseDate());
            throw new AlreadyExistsException(
                    String.format("Ошибка при создании фильма - %s с датой релиза %s уже существует.",
                            film.getName(), film.getReleaseDate()));
        }
        String sql = "INSERT INTO films (name, description, release_date, duration, genre_id, mpa_id) " +
                "VALUES (?, ?, ?, ?, (SELECT genre_id FROM genre WHERE name=?), (SELECT mpa_id FROM mpa WHERE name=?));";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getGenre(),
                film.getMpa());
        log.info("Фильм {} c датой выхода {} добавлен в базу данных.", film.getName(), film.getReleaseDate());
        return film;
    }

    @Override
    public List<Film> getFilms() {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
                "(SELECT name FROM genre AS g WHERE f.genre_id = g.genre_id) AS genre, " +
                "(SELECT name FROM mpa AS r WHERE f.mpa_id = r.mpa_id) AS mpa," +
                "(SELECT COUNT(user_id) FROM users_liked AS ul WHERE f.film_id = ul.film_id) AS likesCount, " +
                "FROM films AS f " +
                "ORDER BY f.name ASC;";
        List<Film> filmList = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
        log.info("Из базы данных выгружен список всех фильмов размером {} записей.", filmList.size());
        return filmList;
    }

    @Override
    public Film updateFilm(Film film) {
        Validator.validate(film);
        Integer expectedId = jdbcTemplate.query(
                "SELECT film_id FROM films WHERE film_id = ?", this::makeInteger, film.getId());
        if (expectedId == null || expectedId != film.getId()) {
            log.info("Ошибка при обновлении. Фильма с [id {}] не существует.", film.getId());
            throw new NotFoundException(
                    String.format("Ошибка при обновлении. Фильма с [id %s] не существует.", film.getId()));
        }
        String sql = "UPDATE films " +
                "SET name = ?, description = ?, release_date = ?, duration = ?, " +
                "genre_id = (SELECT genre_id FROM genre WHERE name = ?), " +
                "mpa_id = (SELECT mpa_id FROM mpa WHERE name = ?)" +
                "WHERE film_id = ?;";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getGenre(), film.getMpa(), film.getId());
        log.info("Фильм {} с [id {}] обновлён.", film.getName(), film.getId());
        return film;
    }

    @Override
    public Film getFilm(int filmId) {
        Integer expectedId = jdbcTemplate.query(
                "SELECT film_id FROM films WHERE film_id = ?", this::makeInteger, filmId);
        if (expectedId == null || expectedId != filmId) {
            log.info("Ошибка при выгрузке. Фильма с [id {}] не существует.", filmId);
            throw new NotFoundException(
                    String.format("Ошибка при выгрузке. Фильма с [id %s] не существует.", filmId));
        }
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
                "(SELECT name FROM genre AS g WHERE f.genre_id = g.genre_id) AS genre, " +
                "(SELECT name FROM mpa AS r WHERE f.mpa_id = r.mpa_id) AS mpa, " +
                "FROM films AS f " +
                "WHERE f.film_id=?;";
        List<Film> film = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), filmId);
        log.info("Из базы данных выгружен фильм {} [id] {}.", film.get(0).getName(), film.get(0).getId());
        return film.get(0);
    }

    @Override
    public void removeFilm(int filmId) {
        Integer expectedId = jdbcTemplate.query(
                "SELECT film_id FROM films WHERE film_id = ?", this::makeInteger, filmId);
        if (expectedId == null || expectedId != filmId) {
            log.info("Ошибка при удалении. Фильма с [id {}] не существует.", filmId);
            throw new NotFoundException(
                    String.format("Ошибка при удалении. Фильма с [id %s] не существует.", filmId));
        }
        String sql = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(sql);
        log.info("Из базы данных удалён фильм [id] {}.", filmId);
    }

    @Override
    public Set<Integer> getFilmLikes(int filmId) {
        Integer expectedId = jdbcTemplate.query(
                "SELECT film_id FROM films WHERE film_id = ?", this::makeInteger, filmId);
        if (expectedId == null || expectedId != filmId) {
            log.info("Ошибка при выгрузке. Фильма с [id {}] не существует.", filmId);
            throw new NotFoundException(
                    String.format("Ошибка при выгрузке. Фильма с [id %s] не существует.", filmId));
        }
        String sql = "SELECT user_id FROM users_liked WHERE film_id = ?";
        Set<Integer> usersLiked = new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> makeInteger(rs), filmId));
        log.info("Из базы данных выгружены лайки фильму [id] {}, всего {} лайков.", filmId, usersLiked.size());
        return usersLiked;
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO users_liked (film_id, user_id) VALUES (?,?);";
        jdbcTemplate.update(sql, filmId, userId);
        log.info("В базу данных добавлен лайк фильму [id {}] от пользователя [id] {}", filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM users_liked WHERE film_id=? AND user_id=?;";
        jdbcTemplate.update(sql, filmId, userId);
        log.info("Из базы данных удалён лайк фильму [id {}] от пользователя [id] {}", filmId, userId);
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        Film film = new Film(
                rs.getInt("film_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_date"),
                rs.getLong("duration"),
                new Genre(s.getArray("genre")),
                rs.getArray("mpa"));
        String sql = "SELECT user_id FROM users_liked WHERE film_id=?;";
        Set<Integer> usersLiked = new HashSet<>(jdbcTemplate.query(sql, (rs1, rowNum) -> makeInteger(rs1), film.getId()));
        film.setUsersLiked(usersLiked);
        return film;
    }

    private String makeString(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return rs.getString(1);
        } else {
            return null;
        }
    }

    private Integer makeInteger(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return rs.getInt(1);
        } else {
            return null;
        }
    }
}
