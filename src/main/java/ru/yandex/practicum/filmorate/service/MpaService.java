package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.Mapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MpaService {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MpaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Mpa> getAllMpa() {
        String sql = "SELECT mpa_id, name AS mpa FROM mpa;";
        List<Mpa> mpaList = jdbcTemplate.query(sql, (rs, rowNum) -> Mapper.mpa.mapRow(rs,rowNum));
        mpaList = mpaList.stream().sorted(Comparator.comparing(Mpa::getId)).collect(Collectors.toList());
        return mpaList;
    }

    public Mpa getMpaById(int id) {
        String sql = "SELECT mpa_id, name AS mpa FROM mpa WHERE mpa_id = ?;";
        List<Mpa> mpaList = jdbcTemplate.query(sql, (rs, rowNum) -> Mapper.mpa.mapRow(rs, rowNum), id);
        if (mpaList.isEmpty()) {
            log.info("Запрашиваемого MPA с id = {} не существует.", id);
            throw new NotFoundException("Запрашиваемого MPA с id = " + id + " не существует.");
        }
        log.info("Выгружен MPA {} с id = {}", mpaList.get(0).getName(), mpaList.get(0).getId());
        return mpaList.get(0);
    }
}
