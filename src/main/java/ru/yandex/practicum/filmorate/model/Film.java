package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.Duration;
import java.util.Date;

@Data
@NoArgsConstructor
public class Film {
    private int id;
    @NonNull
    private String name;
    private String description;
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date releaseDate;
    @JsonFormat(pattern="MINUTES")
    private Duration duration;

    @JsonGetter
    public Long getDuration() {
        return duration.toMinutes();
    }
}
