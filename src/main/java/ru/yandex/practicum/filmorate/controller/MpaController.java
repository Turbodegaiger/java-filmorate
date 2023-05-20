package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@Slf4j
public class MpaController {
    private final MpaService mpaService;

    @Autowired
    public MpaController(MpaService mpaService) {
        this.mpaService = mpaService;
    }

    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable int id) {
        log.info("Принят запрос на получение возрастного рейтинга [id {}].", id);
        Mpa response = mpaService.getMpaById(id);
        log.info("Отправлен ответ на запрос получения возрастного рейтинга [id {}]: {}.", id, response);
        return response;
    }

    @GetMapping
    public List<Mpa> getAllMpa() {
        log.info("Принят запрос на получение списка возрастных рейтингов.");
        List<Mpa> response = mpaService.getAllMpa();
        log.info("Отправлен ответ на запрос получения списка возрастных рейтингов размером {} записей: {}.",
                response.size(), response);
        return response;
    }
}
