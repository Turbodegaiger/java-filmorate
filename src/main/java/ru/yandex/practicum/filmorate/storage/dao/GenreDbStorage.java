package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.Mapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> getAllGenres() {
        String sql = "SELECT genre_id, genre_name FROM genres;";
        List<Genre> genresList = jdbcTemplate.query(sql, (rs, rowNum) -> Mapper.genre.mapRow(rs,rowNum));
        genresList = genresList.stream().distinct().sorted(Comparator.comparing(Genre::getId)).collect(Collectors.toList());
        return genresList;
    }

    public Genre getGenreById(int id) {
        String sql = "SELECT genre_id, genre_name FROM genres WHERE genre_id = ?;";
        List<Genre> genresList = jdbcTemplate.query(sql, (rs, rowNum) -> Mapper.genre.mapRow(rs, rowNum), id);
        if (genresList.isEmpty()) {
            log.info("Запрашиваемого GENRE [id {}] не существует.", id);
            throw new NotFoundException("Запрашиваемого GENRE [id " + id + "] не существует.");
        }
        log.info("Выгружен GENRE {} [id {}]", genresList.get(0).getName(), genresList.get(0).getId());
        return genresList.get(0);
    }
}
