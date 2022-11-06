CREATE TABLE IF NOT EXISTS patients(
                             id serial PRIMARY KEY,
                             full_name VARCHAR(50) NOT NULL,
                             birthdate DATE NOT NULL,
                             sex VARCHAR(6) NOT NULL,
                             address VARCHAR(50) NOT NULL,
                             medical_policy BIGINT NOT NULL UNIQUE
);

--;;

INSERT INTO patients(full_name, birthdate, sex, address, medical_policy)
VALUES
        ('Gleb Vladimirovich Taftin', '1993-08-24', 'male', 'Moscow', 1231321432253324),
        ('Kirill Aleksandrovich Mineev', '1990-01-20', 'male', 'Moscow', 2344234325525325),
        ('Kirill VLadimirovich Fedorov', '1993-02-04', 'male', 'Moscow', 2535235235233525),
        ('Albina Urievna Dzagoeva', '1970-03-15', 'female', 'Kazan', 2553228998278366),
        ('Nina VLadimirovna Lupkina', '2000-04-20', 'female', 'Kazan', 2626246466444525),
        ('Evelina Kadzemirovna Bledans', '1993-12-23', 'female', 'Novosibirsk', 3259059252532235)
