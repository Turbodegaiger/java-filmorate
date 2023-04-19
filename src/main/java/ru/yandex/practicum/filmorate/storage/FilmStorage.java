package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;

public interface FilmStorage {

    Film addFilm(Film film);

    List<Film> getFilms();

    Film updateFilm(Film film);

    Film getFilm(int filmId);

    void removeFilm(int filmId);

    Set<Integer> getFilmLikes(int filmId);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);
}
