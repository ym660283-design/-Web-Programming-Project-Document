CREATE DATABASE IF NOT EXISTS travel_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE travel_db;

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    login_id VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    user_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS trips (
    trip_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    trip_title VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    description TEXT,
    share_code VARCHAR(50) UNIQUE,
    CONSTRAINT fk_trips_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS trip_details (
    detail_id INT AUTO_INCREMENT PRIMARY KEY,
    trip_id INT NOT NULL,
    schedule_date DATE NOT NULL,
    place_name VARCHAR(100) NOT NULL,
    visit_time TIME,
    memo TEXT,
    cost INT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    CONSTRAINT fk_trip_details_trip
        FOREIGN KEY (trip_id) REFERENCES trips(trip_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS trip_members (
    member_id INT AUTO_INCREMENT PRIMARY KEY,
    trip_id INT NOT NULL,
    user_id INT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'viewer',
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_trip_members_trip_user (trip_id, user_id),
    CONSTRAINT fk_trip_members_trip
        FOREIGN KEY (trip_id) REFERENCES trips(trip_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_trip_members_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
