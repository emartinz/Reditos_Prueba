CREATE DATABASE IF NOT EXISTS prtec_auth_db;

USE prtec_auth_db;

CREATE TABLE role (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE user_role (
    user_id INT,
    role_id INT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (role_id) REFERENCES role(id)
);

# Roles Iniciales
INSERT INTO role (name) VALUES ('USER'), ('ADMIN');

# Usuarios Iniciales
INSERT INTO user (username, password) VALUES
('admin', '$2a$10$9yGjSeE9NqZ3E/zNfbjBXuYRCwxE4XtRNsftQx5d.4EiXEzCxTCo2'), -- admin123
('user1', '$2a$10$W7HK0gVckFE/ZrEj7HLcKeCwGxaCRBZ8xJYXHjT3j6YUdAEFD1JOC'); -- user123

-- Asignación de Roles
INSERT INTO user_role (user_id, role_id) VALUES
(1, 2), -- Admin con rol ADMIN
(2, 1); -- User1 con rol USER