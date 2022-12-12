INSERT INTO currency (name, code)
VALUES ('Euro', '€'),
       ('Dollar', '$')
ON DUPLICATE KEY
    UPDATE name = name;
