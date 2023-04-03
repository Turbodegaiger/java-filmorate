# java-filmorate
Template repository for Filmorate project.

![Database scheme](/docs/Filmorate db scheme.jpg)

# SQL request examples
Получение списка имён 10 пользователей c наибольшим числом друзей
SELECT name
FROM user
WHERE user_id IN (SELECT user_id
               FROM friends_confirmed
               GROUP BY user_id
               ORDER BY COUNT(friend_id_confirmed) DESC
               LIMIT 10);

Получение самого популярного жанра фильмов и количество фильмов в этом жанре
SELECT (SELECT name
        FROM genre
        WHERE f.genre_id = genre_id) AS popular_genre,
	COUNT(f.film_id)
FROM film AS f
WHERE f.genre_id IN (SELECT genre_id
               FROM film
               GROUP BY genre_id
               ORDER BY COUNT(genre_id) DESC
               LIMIT 1);
