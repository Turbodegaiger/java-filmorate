INSERT INTO genres (genre_id, name)
VALUES (1, 'Комедия'), 
(2, 'Драма'), 
(3, 'Мультфильм'), 
(4, 'Триллер'), 
(5, 'Документальный'), 
(6, 'Боевик'), 
(7, 'Ужасы')
ON CONFLICT DO NOTHING;

INSERT INTO mpa (mpa_id, name)
VALUES (1, 'G'), 
(2, 'PG'), 
(3, 'PG-13'), 
(4, 'R'), 
(5, 'NC-17')
ON CONFLICT DO NOTHING;