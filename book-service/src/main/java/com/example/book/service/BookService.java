package com.example.book.service;

import com.example.book.client.PricingServiceClient;
import com.example.book.entity.Book;
import com.example.book.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service métier pour la gestion des livres
 * Gère les opérations CRUD et l'emprunt avec résilience
 */
@Service
public class BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    private static final Double FALLBACK_PRICE = 0.0;

    private final BookRepository bookRepository;
    private final PricingServiceClient pricingServiceClient;

    public BookService(BookRepository bookRepository, PricingServiceClient pricingServiceClient) {
        this.bookRepository = bookRepository;
        this.pricingServiceClient = pricingServiceClient;
    }

    /**
     * Récupère tous les livres
     */
    public List<Book> getAllBooks() {
        logger.info("Fetching all books");
        return bookRepository.findAll();
    }

    /**
     * Récupère un livre par son ID
     */
    public Optional<Book> getBookById(Long id) {
        logger.info("Fetching book with id={}", id);
        return bookRepository.findById(id);
    }

    /**
     * Crée un nouveau livre
     */
    public Book createBook(Book book) {
        logger.info("Creating new book: {}", book.getTitle());
        return bookRepository.save(book);
    }

    /**
     * Met à jour un livre existant
     */
    public Book updateBook(Long id, Book bookDetails) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setStock(bookDetails.getStock());

        logger.info("Updating book with id={}", id);
        return bookRepository.save(book);
    }

    /**
     * Supprime un livre
     */
    public void deleteBook(Long id) {
        logger.info("Deleting book with id={}", id);
        bookRepository.deleteById(id);
    }

    /**
     * Emprunte un livre avec transaction et résilience
     * 
     * Processus:
     * 1. Charge le livre avec verrouillage (évite race conditions)
     * 2. Vérifie le stock disponible
     * 3. Décrémente le stock
     * 4. Appelle pricing-service pour récupérer le prix
     * 5. Si pricing-service échoue, utilise le fallback (prix = 0.0)
     * 6. Persiste les changements et retourne le résultat
     * 
     * @param bookId ID du livre à emprunter
     * @return Map contenant le résultat de l'emprunt
     */
    @Transactional
    public Map<String, Object> borrowBook(Long bookId) {
        logger.info("=== BORROW OPERATION START for bookId={} ===", bookId);

        Map<String, Object> result = new HashMap<>();
        result.put("bookId", bookId);
        result.put("timestamp", System.currentTimeMillis());

        // 1. Charger le livre avec verrouillage pessimiste
        Optional<Book> optionalBook = bookRepository.findByIdWithLock(bookId);

        if (optionalBook.isEmpty()) {
            logger.error("Book not found with id={}", bookId);
            result.put("success", false);
            result.put("error", "Book not found");
            result.put("status", "NOT_FOUND");
            return result;
        }

        Book book = optionalBook.get();
        result.put("title", book.getTitle());
        result.put("author", book.getAuthor());

        // 2. Vérifier le stock
        if (book.getStock() <= 0) {
            logger.warn("Book {} is out of stock", bookId);
            result.put("success", false);
            result.put("error", "Book is out of stock");
            result.put("status", "OUT_OF_STOCK");
            result.put("stock", 0);
            return result;
        }

        // 3. Décrémenter le stock
        int previousStock = book.getStock();
        book.decrementStock();
        logger.info("Stock decremented for book {}: {} -> {}", bookId, previousStock, book.getStock());

        // 4. Appeler pricing-service (avec résilience)
        Double price;
        boolean pricingAvailable = true;

        try {
            price = pricingServiceClient.getPrice(bookId);
            logger.info("Price retrieved from pricing-service: {}", price);

            // Vérifier si c'est le prix fallback
            if (price.equals(FALLBACK_PRICE)) {
                pricingAvailable = false;
                logger.warn("Fallback price used - pricing-service was unavailable");
            }
        } catch (Exception e) {
            // Ce cas ne devrait pas arriver grâce au fallback, mais par sécurité
            logger.error("Unexpected error calling pricing-service: {}", e.getMessage());
            price = FALLBACK_PRICE;
            pricingAvailable = false;
        }

        // 5. Sauvegarder les changements
        bookRepository.save(book);
        logger.info("Book {} saved with new stock={}", bookId, book.getStock());

        // 6. Construire la réponse
        result.put("success", true);
        result.put("status", "BORROWED");
        result.put("previousStock", previousStock);
        result.put("remainingStock", book.getStock());
        result.put("price", price);
        result.put("currency", "EUR");
        result.put("pricingServiceAvailable", pricingAvailable);

        if (!pricingAvailable) {
            result.put("priceNote", "Fallback price used - pricing service was unavailable");
        }

        logger.info("=== BORROW OPERATION SUCCESS for bookId={} ===", bookId);
        logger.info("Result: stock {} -> {}, price={}, pricingAvailable={}",
                previousStock, book.getStock(), price, pricingAvailable);

        return result;
    }

    /**
     * Recherche les livres disponibles (stock > 0)
     */
    public List<Book> getAvailableBooks() {
        return bookRepository.findByStockGreaterThan(0);
    }
}
