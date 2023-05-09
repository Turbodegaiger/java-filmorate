package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class FilmMapper implements RowMapper<Film> {
    JdbcTemplate jdbcTemplate;

    public FilmMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa = Mapper.mpa.mapRow(rs, rowNum);
        Film film = new Film(
                rs.getInt("film_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_date"),
                rs.getLong("duration"),
                getGenreList(rs, rowNum),
                mpa);
        String sql = "SELECT user_id FROM users_liked WHERE film_id=?;";
        SqlRowSet usersLikedRows = jdbcTemplate.queryForRowSet(sql,
                film.getId());
        Set<Integer> usersLiked = new HashSet<>();
        for (int i = 0; usersLikedRows.next(); i++) {
            usersLiked.add(usersLikedRows.getInt("user_id"));
        }
        film.setUsersLiked(usersLiked);
        return film;
    }

    private List<Genre> getGenreList(ResultSet rs, int rowNum) throws SQLException {
        List<Genre> genres = new ArrayList<>();
        if (rs.getString("genres") == null) {
            return genres;
        }
        do {
            genres.add(Mapper.genre.mapRow(rs, rowNum));
        } while (rs.next());
        genres = genres.stream().distinct().sorted(Comparator.comparing(Genre::getId)).collect(Collectors.toList());
        return genres;
    }
}