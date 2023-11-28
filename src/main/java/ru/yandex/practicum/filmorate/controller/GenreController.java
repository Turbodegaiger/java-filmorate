package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@RestController
@RequestMapping("/genres")
@Slf4j
public class GenreController {
    private final GenreService genreService;

    @Autowired
    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable int id) {
        log.info("Принят запрос на получение жанра [id {}].", id);
        Genre response = genreService.getGenreById(id);
        log.info("Отправлен ответ на запрос получения жанра [id {}]: {}.", id, response);
        return response;
    }

    @GetMapping
    public List<Genre> getAllGenres() {
        log.info("Принят запрос на получение списка жанров.");
        List<Genre> response = genreService.getAllGenres();
        log.info("Отправлен ответ на запрос получения списка жанров размером {} записей: {}.", response.size(), response);
        return response;
    }
}
