package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilmSearchByFiltersResponse_items {
    Integer kinopoiskId;
    String imdbId;
    String nameRu;
    String nameEn;
    String nameOriginal;
    List<Country> countries;
    List<Genre> genres;
    Float ratingKinopoisk;
    Float ratingImdb;
    Short year;
    String type;
    String posterUrl;
    String posterUrlPreview;
}
