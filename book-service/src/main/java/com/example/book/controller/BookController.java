package com.example.book.controller;

import com.example.book.entity.Book;
import com.example.book.service.BookService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller REST pour la gestion des livres
 * 
 * Endpoints:
 * - GET /books : liste tous les livres
 * - GET /books/{id} : récupère un livre par ID
 * - POST /books : crée un nouveau livre
 * - PUT /books/{id} : met à jour un livre
 * - DELETE /books/{id} : supprime un livre
 * - POST /books/{id}/borrow : emprunte un livre
 * - GET /books/available : liste les livres disponibles
 */
@RestController
@RequestMapping("/books")
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Liste tous les livres
     */
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        logger.info("GET /books - Fetching all books");
        List<Book> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    /**
     * Récupère un livre par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        logger.info("GET /books/{} - Fetching book", id);
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("Book with id={} not found", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Crée un nouveau livre
     */
    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody Book book) {
        logger.info("POST /books - Creating book: {}", book.getTitle());
        Book createdBook = bookService.createBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    /**
     * Met à jour un livre
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @Valid @RequestBody Book book) {
        logger.info("PUT /books/{} - Updating book", id);
        try {
            Book updatedBook = bookService.updateBook(id, book);
            return ResponseEntity.ok(updatedBook);
        } catch (RuntimeException e) {
            logger.error("Error updating book {}: {}", id, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Supprime un livre
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        logger.info("DELETE /books/{} - Deleting book", id);
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Emprunte un livre
     * Décrémente le stock et récupère le prix depuis pricing-service
     * Utilise un fallback si pricing-service est indisponible
     */
    @PostMapping("/{id}/borrow")
    public ResponseEntity<Map<String, Object>> borrowBook(@PathVariable Long id) {
        logger.info("POST /books/{}/borrow - Borrowing book", id);

        Map<String, Object> result = bookService.borrowBook(id);

        Boolean success = (Boolean) result.get("success");

        if (success == null || !success) {
            String status = (String) result.get("status");
            HttpStatus httpStatus = "NOT_FOUND".equals(status) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(httpStatus).body(result);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Liste les livres disponibles (stock > 0)
     */
    @GetMapping("/available")
    public ResponseEntity<List<Book>> getAvailableBooks() {
        logger.info("GET /books/available - Fetching available books");
        List<Book> books = bookService.getAvailableBooks();
        return ResponseEntity.ok(books);
    }

    /**
     * Endpoint racine pour info service
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("service", "book-service");
        info.put("version", "1.0.0");
        info.put("description", "Book management microservice with resilience");
        return ResponseEntity.ok(info);
    }
}
