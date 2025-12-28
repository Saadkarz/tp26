package com.example.book.config;

import com.example.book.entity.Book;
import com.example.book.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Initialisation des données au démarrage
 * Crée quelques livres de test si la base est vide
 */
@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initDatabase(BookRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                logger.info("Initializing database with sample books...");

                repository.save(new Book("Clean Code", "Robert C. Martin", 5));
                repository.save(new Book("Design Patterns", "Gang of Four", 3));
                repository.save(new Book("The Pragmatic Programmer", "David Thomas", 4));
                repository.save(new Book("Refactoring", "Martin Fowler", 2));
                repository.save(new Book("Domain-Driven Design", "Eric Evans", 3));

                logger.info("Sample books initialized successfully");
                repository.findAll().forEach(book -> logger.info("Created: {} by {} (stock: {})",
                        book.getTitle(), book.getAuthor(), book.getStock()));
            } else {
                logger.info("Database already contains {} books", repository.count());
            }
        };
    }
}
