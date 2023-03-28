package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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

    @Override
    public Film addFilm(Film film) {
        Validator.validate(film);
        if (films.containsValue(film)) {
            log.info("Фильм {} уже существует", film);
            throw new AlreadyExistsException("Фильм " + film.getName() + " уже существует.");
        }
        film.setId(idGenerator());
        films.put(film.getId(), film);
        log.info("Фильм добавлен в коллекцию {}.", film);
        return film;
    }

    @Override
    public List<Film> getFilms() {
        log.info("Список фильмов: {}", films.values());
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilm(int filmId) {
        Film film = films.get(filmId);
        if (film == null) {
            log.info("Фильм {} НЕ найден.", filmId);
            throw new NotFoundException("Фильм " + filmId + " НЕ найден.");
        }
        log.info("Возвращаем запрошенный фильм {}.", film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            log.info("Фильм {} НЕ найден.", film.getId());
            throw new NotFoundException("Фильм " + film.getId() + " НЕ найден.");
        }
        Validator.validate(film);
        films.replace(film.getId(), film);
        log.info("Фильм {} обновлён.", film.getId());
        return film;
    }

    private int idGenerator() {
        idCounter++;
        log.info("Сгенерирован id с номером: {}.", idCounter);
        return idCounter;
    }
}
