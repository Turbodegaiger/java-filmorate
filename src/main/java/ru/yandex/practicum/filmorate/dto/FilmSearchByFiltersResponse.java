package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilmSearchByFiltersResponse {
    @NotNull
    Integer total;
    @NotNull
    Integer totalPages;
    @NotNull
    List<FilmSearchByFiltersResponse_items> items;
}
