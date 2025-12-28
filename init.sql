-- ===========================================
-- Script d'initialisation MySQL - TP26
-- Microservice Observable & Résilient
-- ===========================================

-- Création de la base de données (si elle n'existe pas déjà)
CREATE DATABASE IF NOT EXISTS books 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- Utiliser la base de données
USE books;

-- Accorder tous les privilèges à l'utilisateur
GRANT ALL PRIVILEGES ON books.* TO 'booksuser'@'%';
FLUSH PRIVILEGES;

-- Note: La table 'books' sera créée automatiquement par JPA (ddl-auto=update)
-- Mais voici un exemple de structure pour référence:
--
-- CREATE TABLE IF NOT EXISTS books (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     title VARCHAR(255) NOT NULL,
--     author VARCHAR(255) NOT NULL,
--     stock INT NOT NULL DEFAULT 0,
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Données initiales (optionnel - le DataInitializer Java s'en charge aussi)
-- INSERT INTO books (title, author, stock) VALUES 
--     ('Clean Code', 'Robert C. Martin', 5),
--     ('Design Patterns', 'Gang of Four', 3),
--     ('The Pragmatic Programmer', 'David Thomas', 4);

SELECT 'Database initialized successfully!' AS status;
