-- Migration V2: Ajout de la table des livres
-- Cette migration ajoute la table principale pour gérer les livres de la bibliothèque

CREATE TABLE IF NOT EXISTS books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    category_id BIGINT,
    price DECIMAL(10, 2),
    stock_quantity INTEGER DEFAULT 0,
    description TEXT,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- Insertion de quelques livres d'exemple
-- Note: Ces données sont insérées uniquement si elles n'existent pas déjà
INSERT INTO books (title, isbn, category_id, price, stock_quantity, description) 
SELECT 'Spring Boot in Action', '978-1617292545', 4, 39.99, 10, 'Guide complet pour développer des applications Spring Boot'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '978-1617292545');

INSERT INTO books (title, isbn, category_id, price, stock_quantity, description) 
SELECT 'Clean Code', '978-0132350884', 4, 45.00, 5, 'Un manuel de savoir-faire en programmation'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '978-0132350884');

INSERT INTO books (title, isbn, category_id, price, stock_quantity, description) 
SELECT 'The Great Gatsby', '978-0743273565', 1, 12.99, 20, 'Un classique de la littérature américaine'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '978-0743273565');

INSERT INTO books (title, isbn, category_id, price, stock_quantity, description) 
SELECT 'A Brief History of Time', '978-0553380163', 2, 18.99, 8, 'Introduction à la cosmologie moderne'
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '978-0553380163');
