MERGE INTO genres AS g USING (VALUES (1, 'Комедия'),
(2, 'Драма'),
(3, 'Мультфильм'),
(4, 'Триллер'),
(5, 'Документальный'),
(6, 'Боевик')) s(genre_id, name)
ON g.genre_id = s.genre_id AND g.name = s.name
WHEN NOT MATCHED THEN INSERT VALUES (s.genre_id, s.name);

MERGE INTO mpa AS m USING (VALUES (1, 'G'),
(2, 'PG'),
(3, 'PG-13'),
(4, 'R'),
(5, 'NC-17')) s(mpa_id, name)
ON m.mpa_id = s.mpa_id AND m.name = s.name
WHEN NOT MATCHED THEN INSERT VALUES (s.mpa_id, s.name);