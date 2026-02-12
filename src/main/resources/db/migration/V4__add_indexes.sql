-- Migration V4: Ajout d'index pour améliorer les performances
-- Cette migration illustre l'optimisation des requêtes avec des index

-- Index sur le titre pour accélérer les recherches
CREATE INDEX IF NOT EXISTS idx_books_title ON books(title);

-- Index sur l'auteur
CREATE INDEX IF NOT EXISTS idx_books_author ON books(author);

-- Index sur l'ISBN (déjà unique, mais l'index améliore les recherches)
CREATE INDEX IF NOT EXISTS idx_books_isbn ON books(isbn);

-- Index composite sur category_id et stock_quantity pour les requêtes de filtrage
CREATE INDEX IF NOT EXISTS idx_books_category_stock ON books(category_id, stock_quantity);

-- Index sur l'email des utilisateurs (déjà unique, mais utile pour les recherches)
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
