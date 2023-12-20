package ru.yandex.practicum.filmorate.client;

import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

public class BaseClient {
    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
        rest.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    protected ResponseEntity<Object> get(String path) {
        return makeAndSendRequest(HttpMethod.GET, path, MediaType.APPLICATION_JSON);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path, MediaType mediaType) {
        HttpEntity<T> requestEntity = new HttpEntity<>(defaultHeaders(mediaType));
        ResponseEntity<Object> serverResponse;
        try {
            serverResponse = rest.exchange(path, method, requestEntity, Object.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepareGatewayResponse(serverResponse);
    }

    private HttpHeaders defaultHeaders(MediaType mediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.add("X-API-KEY", "af610928-86a8-40d3-aa44-50715cb07c84");
        return headers;
    }

    private static ResponseEntity<Object> prepareGatewayResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }
}
