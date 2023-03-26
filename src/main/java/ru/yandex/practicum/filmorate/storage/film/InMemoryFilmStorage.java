package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validator.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage{
    private final HashMap<Integer, Film> films = new HashMap<>();
    private int idCounter = 0;

    public Film addFilm(Film film) {
        log.info("Принят запрос на добавление нового фильма.");
        Validator.validate(film);
        film.setId(idGenerator());
        films.put(film.getId(), film);
        log.info("Фильм добавлен в коллекцию {}.", film);
        return film;
    }

    public List<Film> getFilms() {
        log.info("Принят запрос списка фильмов: {}", films.values());
        return new ArrayList<>(films.values());
    }

    public Film getFilm(int filmId) {
        log.info("Принят запрос на получение фильма по id: {}.", filmId);
        checkContains(filmId);
        return films.get(filmId);
    }

    public Film updateFilm(Film film) {
        log.info("Принят запрос на обновление фильма id {}.", film.getId());
        checkContains(film);
        Validator.validate(film);
        films.replace(film.getId(), film);
        log.info("Фильм {} обновлён.", film.getId());
        return film;
    }

    public boolean checkContains(Film film) {
        if (!films.containsKey(film.getId())) {
            log.info("Фильм {} НЕ найден.", film.getId());
            throw new NotFoundException("Фильм " + film.getId() + " НЕ найден.");
        }
        return true;
    }

    public boolean checkContains(int id) {
        if (!films.containsKey(id)) {
            log.info("Фильм {} НЕ найден.", id);
            throw new NotFoundException("Фильм " + id + " НЕ найден.");
        }
        return true;
    }

    private int idGenerator() {
        idCounter++;
        log.info("Сгенерирован id с номером: {}.", idCounter);
        return idCounter;
    }
}
