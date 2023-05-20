package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Set;

public interface FilmStorage {

    Film addFilm(Film film);

    List<Film> getFilms();

    Film updateFilm(Film film);

    Film getFilm(int filmId);

    void removeFilm(int filmId);

    void removeAllFilms();

    Set<Integer> getFilmLikes(int filmId);

    List<Film> getMostlyPopular(int count);

    List<Genre> getFilmGenres(int filmId);
}
