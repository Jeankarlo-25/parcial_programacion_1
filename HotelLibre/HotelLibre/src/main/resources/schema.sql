-- =====================================================================
-- HotelLibre - Script de la base de datos (SQLite)
-- Variante 4 - Parcial tercer corte - Programación 1
-- 2 tablas: habitacion y reserva (máximo permitido: 3).
-- La aplicación lo ejecuta sola al iniciar. Se puede correr varias veces.
-- =====================================================================

CREATE TABLE IF NOT EXISTS habitacion (
    numero    INTEGER PRIMARY KEY CHECK (numero BETWEEN 100 AND 999),
    piso      INTEGER NOT NULL,
    tipo      TEXT    NOT NULL CHECK (tipo IN ('SENCILLA', 'DOBLE', 'SUITE')),
    capacidad INTEGER NOT NULL CHECK (capacidad BETWEEN 1 AND 4)
);

CREATE TABLE IF NOT EXISTS reserva (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    huesped           TEXT    NOT NULL,
    numero_habitacion INTEGER NOT NULL,
    fecha_entrada     TEXT    NOT NULL,              -- formato aaaa-mm-dd
    fecha_salida      TEXT    NOT NULL,              -- formato aaaa-mm-dd
    personas          INTEGER NOT NULL CHECK (personas >= 1),
    desayuno          INTEGER NOT NULL DEFAULT 0,    -- 0 = no, 1 = sí
    parqueadero       INTEGER NOT NULL DEFAULT 0,
    salida_tardia     INTEGER NOT NULL DEFAULT 0,
    consumo_minibar   REAL    NOT NULL DEFAULT 0 CHECK (consumo_minibar >= 0),
    CHECK (fecha_salida > fecha_entrada),
    FOREIGN KEY (numero_habitacion) REFERENCES habitacion (numero)
);

-- Habitaciones iniciales del hotel (solo se insertan si no existen)
INSERT OR IGNORE INTO habitacion (numero, piso, tipo, capacidad) VALUES
    (101, 1, 'SENCILLA', 1),
    (102, 1, 'SENCILLA', 1),
    (103, 1, 'DOBLE',    3),
    (201, 2, 'SENCILLA', 1),
    (202, 2, 'DOBLE',    3),
    (203, 2, 'DOBLE',    3),
    (204, 2, 'SENCILLA', 1),
    (301, 3, 'SENCILLA', 1),
    (305, 3, 'DOBLE',    3),
    (310, 3, 'DOBLE',    3),
    (401, 4, 'DOBLE',    3),
    (402, 4, 'SUITE',    4),
    (501, 5, 'SUITE',    4),
    (502, 5, 'SUITE',    4);
