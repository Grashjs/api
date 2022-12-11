INSERT INTO currency (id, name, code)
VALUES (1, 'Euro', '€'),
       (2, 'Dollar', '$')
ON DUPLICATE KEY
    UPDATE name = name;
