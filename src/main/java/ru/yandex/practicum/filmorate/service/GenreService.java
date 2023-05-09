package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.Mapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GenreService {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> getAllGenres() {
        String sql = "SELECT genre_id, name AS genres FROM genres;";
        List<Genre> genresList = jdbcTemplate.query(sql, (rs, rowNum) -> Mapper.genre.mapRow(rs,rowNum));
        genresList = genresList.stream().distinct().sorted(Comparator.comparing(Genre::getId)).collect(Collectors.toList());
        return genresList;
    }

    public Genre getGenreById(int id) {
        String sql = "SELECT genre_id, name AS genres FROM genres WHERE genre_id = ?;";
        List<Genre> genresList = jdbcTemplate.query(sql, (rs, rowNum) -> Mapper.genre.mapRow(rs, rowNum), id);
        if (genresList.isEmpty()) {
            log.info("Запрашиваемого GENRE с id = {} не существует.", id);
            throw new NotFoundException("Запрашиваемого GENRE с id = " + id + " не существует.");
        }
        log.info("Выгружен GENRE {} с id = {}", genresList.get(0).getName(), genresList.get(0).getId());
        return genresList.get(0);
    }
}
