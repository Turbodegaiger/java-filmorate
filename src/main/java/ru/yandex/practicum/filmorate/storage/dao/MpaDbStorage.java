package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.Mapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Mpa> getAllMpa() {
        String sql = "SELECT mpa_id, mpa_name FROM mpa;";
        List<Mpa> mpaList = jdbcTemplate.query(sql, (rs, rowNum) -> Mapper.mpa.mapRow(rs,rowNum));
        mpaList = mpaList.stream().sorted(Comparator.comparing(Mpa::getId)).collect(Collectors.toList());
        return mpaList;
    }

    public Mpa getMpaById(int id) {
        String sql = "SELECT mpa_id, mpa_name FROM mpa WHERE mpa_id = ?;";
        List<Mpa> mpaList = jdbcTemplate.query(sql, (rs, rowNum) -> Mapper.mpa.mapRow(rs, rowNum), id);
        if (mpaList.isEmpty()) {
            log.info("Запрашиваемого MPA [id {}] не существует.", id);
            throw new NotFoundException("Запрашиваемого MPA [id " + id + "] не существует.");
        }
        log.info("Выгружен MPA {} [id {}]", mpaList.get(0).getName(), mpaList.get(0).getId());
        return mpaList.get(0);
    }
}
