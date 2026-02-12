-- Migration V7: Ajout de la colonne rating pour noter les livres
-- Cette migration illustre comment ajouter une nouvelle fonctionnalité

-- Ajout de la colonne rating (note sur 5 étoiles)
ALTER TABLE books ADD COLUMN IF NOT EXISTS rating DECIMAL(3,2) CHECK (rating >= 0 AND rating <= 5);

-- Mise à jour des livres existants avec une note par défaut
UPDATE books SET rating = 4.5 WHERE title = 'Spring Boot in Action';
UPDATE books SET rating = 5.0 WHERE title = 'Clean Code';
UPDATE books SET rating = 4.8 WHERE title = 'The Great Gatsby';
UPDATE books SET rating = 4.7 WHERE title = 'A Brief History of Time';

-- Création d'un index pour les recherches par note
CREATE INDEX IF NOT EXISTS idx_books_rating ON books(rating);
