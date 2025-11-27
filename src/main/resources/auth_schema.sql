-- AuthDB Schema

CREATE TABLE IF NOT EXISTS users_auth (
    user_id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    last_login TIMESTAMP NULL,
    failed_attempts INT DEFAULT 0,
    lockout_time TIMESTAMP NULL,
    PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS password_history (
    history_id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (history_id),
    FOREIGN KEY (user_id) REFERENCES users_auth(user_id) ON DELETE CASCADE
);
