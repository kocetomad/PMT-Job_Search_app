CREATE DATABASE pmt_db;

-- \c into pmt_db

CREATE TABLE user_tbl(
    user_id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    surname VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    hashed_password CHAR(64)
);

CREATE TABLE pinned_posts_tbl(
    job_id PRIMARY KEY NOT NULL,
    user_id REFERENCES user_tbl (user_id),

);

CREATE TABLE reviews_tbl(
    job_id PRIMARY KEY NOT NULL,
    user_id REFERENCES user_tbl (user_id),
); -- TODO finish this before continuing