USE travel_db;

INSERT INTO users (login_id, password, user_name, email)
VALUES ('demo', 'demo1234', '데모사용자', 'demo@example.com')
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    user_name = VALUES(user_name),
    email = VALUES(email);
