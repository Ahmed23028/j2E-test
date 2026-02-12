-- Migration V3: Ajout de la colonne auteur
-- Cette migration illustre comment ajouter une nouvelle colonne à une table existante

-- Ajout de la colonne author à la table books
ALTER TABLE books ADD COLUMN IF NOT EXISTS author VARCHAR(255);

-- Mise à jour des livres existants avec les noms d'auteurs
UPDATE books SET author = 'Craig Walls' WHERE title = 'Spring Boot in Action';
UPDATE books SET author = 'Robert C. Martin' WHERE title = 'Clean Code';
UPDATE books SET author = 'F. Scott Fitzgerald' WHERE title = 'The Great Gatsby';
UPDATE books SET author = 'Stephen Hawking' WHERE title = 'A Brief History of Time';

-- Ajout d'une contrainte pour rendre la colonne obligatoire pour les nouveaux livres
ALTER TABLE books ALTER COLUMN author SET NOT NULL;
