CREATE TABLE users (
    id uuid PRIMARY KEY,
    email varchar NOT NULL UNIQUE,
    hashed_password varchar NOT NULL,
    role varchar NOT NULL,
    status varchar NOT NULL,
    registered_at timestamptz NOT NULL
);
