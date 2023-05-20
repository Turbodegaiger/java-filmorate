package ru.yandex.practicum.filmorate.validator;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.util.DateUtility;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;


import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ValidatorTest {
    static List<Film> invalidFilms = new ArrayList<>();
    static List<User> invalidUsers = new ArrayList<>();
    static Film okFilm;
    static User okUser;

    @Test
    void validateFilmShouldThrowExceptionIfValuesIsNotCorrect() {
        createFilms();
        for (Film film : invalidFilms) {
            final ValidationException exception = assertThrows(ValidationException.class, () -> Validator.validate(film));
        }
    }

    @Test
    void validateFilmShouldReturnTrueIfValuesIsCorrect() {
        createFilms();
        assertTrue(Validator.validate(okFilm));
    }

    @Test
    void validateUserShouldThrowExceptionIfValuesIsNotCorrect() {
        createUsers();
        for (User user : invalidUsers) {
            final ValidationException exception = assertThrows(ValidationException.class, () -> Validator.validate(user));
        }
    }

    @Test
    void validateUserShouldReturnTrueIfValuesIsCorrectAndSetLoginAsNameIfItDoesntExists() {
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
        Film film1 = new Film();
        film1.setName("");
        film1.setDescription("qeq");
        film1.setDuration(90L);
        Film film2 = new Film();
        film2.setName("1");
        film2.setDuration(90L);
        film2.setDescription(description);
        Film film3 = new Film();
        film3.setName("2");
        film3.setDescription("qeq");
        film3.setDuration(0L);
        Film film4 = new Film();
        film4.setName("3");
        film4.setDescription("qeq");
        film4.setReleaseDate(DateUtility.formatToDate("1895-11-27"));
        film4.setDuration(90L);
        okFilm = new Film();
        okFilm.setName("film");
        okFilm.setReleaseDate(DateUtility.formatToDate("1895-11-28"));
        okFilm.setDuration(1L);
        okFilm.setDescription("BEST FILM EVAR");

        invalidFilms.add(null);
        invalidFilms.add(film1);
        invalidFilms.add(film2);
        invalidFilms.add(film3);
        invalidFilms.add(film4);
    }

    private void createUsers() {
        okUser = new User();
        okUser.setEmail("alibaba@40rogues.com");
        okUser.setLogin("XXXalibabaXXX");
        okUser.setBirthday(DateUtility.formatToDate("1997-10-10"));
        User user1 = new User();
        user1.setEmail("");
        user1.setLogin("emptyEmail");
        User user2 = new User();
        user2.setEmail("abyrvalg");
        user2.setLogin("sharikov");
        User user3 = new User();
        user3.setEmail("chupakabra@mail.com");
        user3.setLogin("space login");
        User user4 = new User();
        user4.setEmail("chupakabrenok@mail.com");
        user4.setLogin("");
        User user5 = new User();
        user5.setEmail("iwantcyberpunkeverywhere@cyber.com");
        user5.setLogin("silverhand");
        user5.setBirthday(DateUtility.formatToDate("2077-11-20"));

        invalidUsers.add(user1);
        invalidUsers.add(user2);
        invalidUsers.add(user3);
        invalidUsers.add(user4);
        invalidUsers.add(user5);
    }
}
