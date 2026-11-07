-- =============================================================================
-- 01-init-schema.sql
-- Script di inizializzazione per il database relazionale MapDB
-- Progetto: Albero di Regressione (Metodi Avanzati di Programmazione)
-- =============================================================================

-- 1. Creazione del database MapDB con codifica UTF-8
CREATE DATABASE IF NOT EXISTS MapDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- 2. Creazione dell'utente applicativo 'MapUser' con password 'map'
CREATE USER IF NOT EXISTS 'MapUser'@'localhost' IDENTIFIED BY 'map';
CREATE USER IF NOT EXISTS 'MapUser'@'%' IDENTIFIED BY 'map';

-- 3. Assegnazione di tutti i privilegi sul database MapDB
GRANT ALL PRIVILEGES ON MapDB.* TO 'MapUser'@'localhost';
GRANT ALL PRIVILEGES ON MapDB.* TO 'MapUser'@'%';

-- 4. Applicazione immediata dei privilegi
FLUSH PRIVILEGES;
