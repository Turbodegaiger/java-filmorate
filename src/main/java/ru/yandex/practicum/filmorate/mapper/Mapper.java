package ru.yandex.practicum.filmorate.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Mapper {
    public static FilmMapper film;
    public static GenreMapper genre;
    public static MpaMapper mpa;
    public static UserMapper user;

    @Autowired
    public Mapper(FilmMapper film, GenreMapper genre, MpaMapper mpa, UserMapper user) {
        Mapper.film = film;
        Mapper.genre = genre;
        Mapper.mpa = mpa;
        Mapper.user = user;
    }
}
