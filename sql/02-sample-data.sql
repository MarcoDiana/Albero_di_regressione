-- =============================================================================
-- 02-sample-data.sql
-- Dataset dimostrativi e benchmark per Albero di Regressione
-- Progetto: Albero di Regressione (Metodi Avanzati di Programmazione)
-- =============================================================================

USE MapDB;

-- -----------------------------------------------------------------------------
-- 1. Tabella di test 'provaC'
--    Comprende 15 tuple con attributi discreti e continui per apprendimento rapido.
--    X: discreto (String)
--    Y: continuo (Double)
--    C: target continuo (Double)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS provaC;
CREATE TABLE provaC (
    X VARCHAR(10) NOT NULL,
    Y DOUBLE NOT NULL,
    C DOUBLE NOT NULL
);

INSERT INTO provaC (X, Y, C) VALUES
('A', 2.0, 1.0),
('A', 2.0, 1.0),
('A', 1.0, 1.0),
('A', 2.0, 1.0),
('A', 5.0, 1.5),
('A', 5.0, 1.5),
('A', 6.0, 1.5),
('B', 6.0, 10.0),
('A', 6.0, 1.5),
('A', 6.0, 1.5),
('B', 10.0, 10.0),
('B', 5.0, 10.0),
('B', 12.0, 10.0),
('B', 14.0, 10.0),
('A', 1.0, 1.0);

-- -----------------------------------------------------------------------------
-- 2. Tabella benchmark 'servo'
--    Dataset UCI Machine Learning per la predizione del tempo di risposta di un servomeccanismo.
--    motor, screw, pgain, vgain: discreti (String)
--    class: target continuo (Double)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS servo;
CREATE TABLE servo (
    motor VARCHAR(10) NOT NULL,
    screw VARCHAR(10) NOT NULL,
    pgain VARCHAR(10) NOT NULL,
    vgain VARCHAR(10) NOT NULL,
    class DOUBLE NOT NULL
);

INSERT INTO servo (motor, screw, pgain, vgain, class) VALUES
('E', 'E', '5', '4', 0.28125095),
('B', 'D', '6', '5', 0.5062525),
('D', 'D', '4', '3', 0.35625148),
('B', 'A', '3', '2', 5.500033),
('D', 'B', '6', '5', 0.35625148),
('E', 'C', '4', '3', 0.8062546),
('C', 'A', '3', '2', 5.100014),
('A', 'A', '3', '2', 5.7000422),
('C', 'A', '6', '5', 0.76875436),
('D', 'A', '4', '1', 1.0312537),
('B', 'E', '6', '5', 0.46875226),
('E', 'C', '5', '4', 0.39375174),
('B', 'C', '4', '1', 0.28125095),
('E', 'C', '3', '1', 1.1),
('C', 'C', '5', '4', 0.5062525),
('E', 'B', '3', '2', 1.8999897),
('D', 'C', '3', '1', 0.9000011),
('B', 'C', '5', '4', 0.46875226),
('B', 'B', '5', '4', 0.5437528),
('C', 'E', '4', '2', 0.20625044),
('E', 'D', '4', '3', 0.9187554),
('A', 'D', '4', '3', 1.1062483),
('B', 'C', '6', '5', 0.46875226),
('A', 'C', '4', '2', 0.58125305),
('A', 'B', '6', '5', 0.58125305),
('E', 'C', '6', '5', 0.39375174),
('A', 'A', '3', '1', 5.3000236),
('A', 'E', '4', '2', 0.46875226),
('C', 'D', '3', '2', 1.8999897),
('B', 'B', '3', '2', 4.299977),
('B', 'E', '4', '2', 0.35625148),
('B', 'C', '3', '1', 3.899964),
('C', 'E', '4', '1', 0.5437528);
