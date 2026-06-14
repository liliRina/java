CREATE TABLE IF NOT EXISTS drying_rules  (
    id SERIAL PRIMARY KEY,
    temp_min INT NOT NULL,
    temp_max INT NOT NULL,
    humidity_min INT NOT NULL,
    humidity_max INT NOT NULL,
    drying_value INT NOT NULL
);

INSERT INTO drying_rules (temp_min, temp_max, humidity_min, humidity_max, drying_value) VALUES
(31, 99, 0, 29, 10),
(31, 99, 30, 70, 9),
(31, 99, 71, 100, 7),
(25, 30, 0, 29, 8),
(25, 30, 30, 70, 7),
(25, 30, 71, 100, 6),
(18, 24, 0, 29, 6),
(18, 24, 30, 70, 6),
(18, 24, 71, 100, 5),
(-50, 17, 0, 100, 5);