package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@NoArgsConstructor
public class Film {
    private int id;
    @NotBlank
    private String name;
    @Size(min = 1, max = 200)
    private String description;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date releaseDate;
    @Min(1)
    private Long duration;
    private String genre;
    private String rating;
    @JsonIgnore
    private Set<Integer> usersLiked = new HashSet<>();
    public Film(String name, String description, Date releaseDate, Long duration, String genre, String rating) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.genre = genre;
        this.rating = rating;
    }

    public Film(Integer id, String name, String description, Date releaseDate, Long duration, String genre, String rating) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.genre = genre;
        this.rating = rating;
    }

    public Integer getLikesCount() {
        return usersLiked.size();
    }

    @Override
    public String toString() {
        return "Film{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", releaseDate=" + releaseDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Film film = (Film) o;
        return getName().equals(film.getName()) && getReleaseDate().equals(film.getReleaseDate()) && getDuration().equals(film.getDuration());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getReleaseDate(), getDuration());
    }
}
