CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS trips (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    destination VARCHAR(180) NOT NULL,
    budget NUMERIC(12, 2) NOT NULL CHECK (budget > 0),
    duration INTEGER NOT NULL CHECK (duration > 0),
    estimated_cost NUMERIC(12, 2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trips_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS trip_interests (
    trip_id BIGINT NOT NULL,
    interest VARCHAR(80) NOT NULL,
    PRIMARY KEY (trip_id, interest),
    CONSTRAINT fk_trip_interests_trip FOREIGN KEY (trip_id) REFERENCES trips (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS itinerary (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    day INTEGER NOT NULL CHECK (day > 0),
    activity TEXT NOT NULL,
    CONSTRAINT fk_itinerary_trip FOREIGN KEY (trip_id) REFERENCES trips (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS hotels (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(180) NOT NULL,
    location VARCHAR(180) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    rating NUMERIC(3, 2) NOT NULL,
    CONSTRAINT uq_hotels_name_location UNIQUE (name, location)
);

CREATE TABLE IF NOT EXISTS attractions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(180) NOT NULL,
    category VARCHAR(100) NOT NULL,
    location VARCHAR(180) NOT NULL,
    rating NUMERIC(3, 2) NOT NULL,
    CONSTRAINT uq_attractions_name_location UNIQUE (name, location)
);

CREATE TABLE IF NOT EXISTS trip_hotels (
    trip_id BIGINT NOT NULL,
    hotel_id BIGINT NOT NULL,
    PRIMARY KEY (trip_id, hotel_id),
    CONSTRAINT fk_trip_hotels_trip FOREIGN KEY (trip_id) REFERENCES trips (id) ON DELETE CASCADE,
    CONSTRAINT fk_trip_hotels_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS trip_attractions (
    trip_id BIGINT NOT NULL,
    attraction_id BIGINT NOT NULL,
    PRIMARY KEY (trip_id, attraction_id),
    CONSTRAINT fk_trip_attractions_trip FOREIGN KEY (trip_id) REFERENCES trips (id) ON DELETE CASCADE,
    CONSTRAINT fk_trip_attractions_attraction FOREIGN KEY (attraction_id) REFERENCES attractions (id) ON DELETE CASCADE
);
