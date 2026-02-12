-- Migration V1: Création des tables initiales
-- Cette migration crée la structure de base de notre application de bibliothèque

-- Table des utilisateurs
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    active BOOLEAN DEFAULT TRUE
);

-- Table des catégories de livres
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- Insertion de données initiales pour les catégories
-- Note: Ces données sont insérées uniquement si elles n'existent pas déjà
INSERT INTO categories (name, description) 
SELECT 'Fiction', 'Livres de fiction et romans'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Fiction');

INSERT INTO categories (name, description) 
SELECT 'Science', 'Livres scientifiques et techniques'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Science');

INSERT INTO categories (name, description) 
SELECT 'Histoire', 'Livres historiques'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Histoire');

INSERT INTO categories (name, description) 
SELECT 'Informatique', 'Livres sur la programmation et les technologies'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Informatique');
