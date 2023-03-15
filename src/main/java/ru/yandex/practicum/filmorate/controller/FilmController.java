package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validator.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@Slf4j
public class FilmController {
    private final HashMap<Integer, Film> films = new HashMap<>();
    private int idCounter = 0;

    @PostMapping(value = "/films")
    public Film addFilm (@RequestBody Film film) {
        log.info("Принят запрос на добавление нового фильма.");
        Validator.validate(film);
        film.setId(idGenerator());
        films.put(film.getId(), film);
        log.info("Фильм добавлен в коллекцию {}.", film);
        return film;
    }

    @GetMapping("/films")
    public List<Film> getFilms() {
        log.info("Принят запрос списка фильмов: {}", films.values());
        return new ArrayList<>(films.values());
    }

    @PutMapping(value = "/films")
    public Film updateFilm(@RequestBody Film film) {
        log.info("Принят запрос на обновление фильма id {}.", film.getId());
        if (!films.containsKey(film.getId())) {
            log.info("Обновляемый пользователь НЕ найден.");
            throw new NotFoundException();
        }
        Validator.validate(film);
        films.replace(film.getId(), film);
        log.info("Фильм {} обновлён.", film.getId());
        return film;
    }

    private int idGenerator() {
        log.info("Генерируется id. Его номер - {}.", idCounter+1);
        return ++idCounter;
    }
}
