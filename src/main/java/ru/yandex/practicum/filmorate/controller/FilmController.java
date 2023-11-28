package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addFilm(@RequestBody Film film) {
        log.info("Принят запрос на добавление нового фильма: {}.", film);
        Film response = filmService.addFilm(film);
        log.info("Отправлен ответ на запрос добавления нового фильма: {}.", response);
        return response;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Film> getFilms() {
        log.info("Принят запрос на получение списка фильмов.");
        List<Film> response = filmService.getFilms();
        log.info("Отправлен ответ на запрос получения списка фильмов, размер списка: {}", response.size());
        return response;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Film getFilm(@PathVariable int id) {
        log.info("Принят запрос на получение фильма по [id {}].", id);
        Film response = filmService.getFilm(id);
        log.info("Отправлен ответ на запрос получения фильма по [id {}]: {}.", id, response);
        return response;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Film updateFilm(@RequestBody Film film) {
        log.info("Принят запрос на обновление фильма [id {}].", film.getId());
        Film response = filmService.updateFilm(film);
        log.info("Отправлен ответ на запрос обновления фильма по [id {}]: {}.", film.getId(), response);
        return response;
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void likeFilm(@PathVariable int id, @PathVariable int userId) {
        log.info("Получен запрос на лайк фильма [id] {} пользователем {}.", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void removeFilm(@PathVariable int id) {
        log.info("Получен запрос на удаление фильма [id {}].", id);
        filmService.removeFilm(id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeLikeFilm(@PathVariable int id, @PathVariable int userId) {
        log.info("Получен запрос на удаление лайка фильма [id {}] пользователем [id {}].", id, userId);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public List<Film> getMostPopularFilms(@RequestParam(defaultValue = "10", required = false) int count) {
        log.info("Получен запрос на список {} самых популярных фильмов.", count);
        List<Film> response = filmService.getMostlyPopularFilms(count);
        log.info("Отправлен ответ на запрос списка самых популярных фильмов размером {}: {}.", response.size(), response);
        return response;
    }
}
