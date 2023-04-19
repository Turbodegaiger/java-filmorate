CREATE TABLE IF NOT EXISTS rating (
    rating_id INTEGER PRIMARY KEY,
    name varchar(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS genre (
    genre_id INTEGER PRIMARY KEY,
    name varchar(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS films (
    film_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    name varchar NOT NULL,
    description varchar DEFAULT 'No description',
    release_date timestamp,
    duration INTEGER,
    genre_id INTEGER,
    FOREIGN KEY (genre_id) REFERENCES genre (genre_id),
    rating_id INTEGER,
    FOREIGN KEY (rating_id) REFERENCES rating (rating_id)
);


CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    name varchar(50) NOT NULL,
    email varchar(40) NOT NULL,
    login varchar(30) NOT NULL,
    birthday timestamp
);


CREATE TABLE IF NOT EXISTS users_liked (
    film_id INTEGER REFERENCES films (film_id),
    user_id INTEGER REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS friends_confirmed (
    user_id INTEGER PRIMARY KEY NOT NULL REFERENCES users (user_id),
    friend_id INTEGER REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS friends_not_confirmed (
    user_id INTEGER PRIMARY KEY NOT NULL REFERENCES users (user_id),
    friend_id INTEGER REFERENCES users (user_id)
);