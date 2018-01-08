CREATE TABLE countries (
  id SERIAL PRIMARY KEY,
  code VARCHAR (2) NOT NULL UNIQUE,
  name VARCHAR (100) NOT NULL UNIQUE,
  currency VARCHAR (3) NOT NULL,
  schengen BOOLEAN NOT NULL
);