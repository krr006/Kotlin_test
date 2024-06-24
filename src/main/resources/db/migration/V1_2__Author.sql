drop table if exists author;

CREATE TABLE author (
    id SERIAL PRIMARY KEY,
    fullname VARCHAR(255) NOT NULL,
    date_of_creation TIMESTAMP NOT NULL
);
