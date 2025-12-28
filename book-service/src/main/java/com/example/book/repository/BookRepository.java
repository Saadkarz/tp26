package com.example.book.repository;

import com.example.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * Repository JPA pour l'entité Book
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Recherche un livre par son titre (insensible à la casse)
     */
    List<Book> findByTitleContainingIgnoreCase(String title);

    /**
     * Recherche les livres d'un auteur
     */
    List<Book> findByAuthorContainingIgnoreCase(String author);

    /**
     * Recherche les livres avec stock disponible
     */
    List<Book> findByStockGreaterThan(Integer stock);

    /**
     * Recherche un livre avec verrouillage pessimiste pour les transactions
     * Utilisé pour éviter les race conditions lors de l'emprunt
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdWithLock(@Param("id") Long id);
}
