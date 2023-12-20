package ru.yandex.practicum.filmorate.client;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.LocalDateTime;

@Service
public class KinopoiskClient extends BaseClient {

    @Autowired
    public KinopoiskClient(@Value("${kinopoisk-server.url:https://kinopoiskapiunofficial.tech}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> getFilms(String order,
                                           String type,
                                           Integer ratingFrom,
                                           Integer ratingTo,
                                           String keyword) {
        StringBuilder sb = new StringBuilder();
        sb.append("/api/v2.2/films?").append("order=").append(order).append("&type=").append(type).append("&ratingFrom=")
                .append(ratingFrom).append("&ratingTo=").append(ratingTo).append("&yearFrom=").append(LocalDateTime.now().getYear());
        if (keyword != null && !keyword.isBlank()) {
            sb.append("&keyword=").append(keyword);
        }
        return get(sb.toString());
    }
}

