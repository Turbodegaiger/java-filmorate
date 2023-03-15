package ru.yandex.practicum.filmorate.validator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.date.DateUtility;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import org.junit.jupiter.api.function.Executable;
import ru.yandex.practicum.filmorate.model.User;


import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ValidatorTest {
    static List<Film> invalidFilms = new ArrayList<>();
    static List<User> invalidUsers = new ArrayList<>();
    static Film okFilm;
    static User okUser;

    @Test
    void ValidateFilmShouldThrowExceptionIfValuesIsNotCorrect() {
        createFilms();
        for(Film film : invalidFilms) {
            final ValidationException exception = assertThrows(ValidationException.class, () -> Validator.validate(film));
        }
    }

    @Test
    void ValidateFilmShouldReturnTrueIfValuesIsCorrect() {
        createFilms();
        assertTrue(Validator.validate(okFilm));
    }

    @Test
    void ValidateUserShouldThrowExceptionIfValuesIsNotCorrect() {
        createUsers();
        for(User user : invalidUsers) {
            final ValidationException exception = assertThrows(ValidationException.class, () -> Validator.validate(user));
        }
    }

    @Test
    void ValidateUserShouldReturnTrueIfValuesIsCorrectAndSetLoginAsNameIfItDoesntExists() {
        createUsers();
        assertTrue(Validator.validate(okUser));
        assertEquals(okUser.getName(), okUser.getLogin());
    }

    private void createFilms() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 201; i++) {
            builder.append("A");
        }
        String description = builder.toString();
        Film film1 = new Film("");
        film1.setDuration(Duration.ofMinutes(90));
        Film film2 = new Film("1");
        film2.setDuration(Duration.ofMinutes(90));
        film2.setDescription(description);
        Film film3 = new Film("2");
        film3.setDuration(Duration.ZERO);
        Film film4 = new Film("3");
        film4.setReleaseDate(DateUtility.formatter("1895-11-27"));
        film4.setDuration(Duration.ofMinutes(90));
        okFilm = new Film("film");
        okFilm.setReleaseDate(DateUtility.formatter("1895-11-28"));
        okFilm.setDuration(Duration.ofMinutes(1));
        okFilm.setDescription("BEST FILM EVAR");

        invalidFilms.add(null);
        invalidFilms.add(film1);
        invalidFilms.add(film2);
        invalidFilms.add(film3);
        invalidFilms.add(film4);
    }

    private void createUsers() {
        okUser = new User("alibaba@40rogues.com", "XXXalibabaXXX");
        okUser.setBirthday(DateUtility.formatter("1997-10-10"));
        User user1 = new User("", "emptyEmail");
        User user2 = new User("abyrvalg", "sharikov");
        User user3 = new User("chupakabra@mail.com", "space login");
        User user4 = new User("chupakabrenok@mail.com", "");
        User user5 = new User("iwantcyberpunkeverywhere@cyber.com", "silverhand");
        user5.setBirthday(DateUtility.formatter("2077-11-20"));

        invalidUsers.add(user1);
        invalidUsers.add(user2);
        invalidUsers.add(user3);
        invalidUsers.add(user4);
        invalidUsers.add(user5);
    }
}
