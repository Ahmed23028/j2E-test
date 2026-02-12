package com.example.flywaydemo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité Borrowing correspondant à la table borrowings créée dans V6__add_borrowings_table.sql
 */
@Entity
@Table(name = "borrowings", indexes = {
    @Index(name = "idx_borrowings_user_id", columnList = "user_id"),
    @Index(name = "idx_borrowings_book_id", columnList = "book_id"),
    @Index(name = "idx_borrowings_status", columnList = "status"),
    @Index(name = "idx_borrowings_due_date", columnList = "due_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Borrowing {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;
    
    @Column(name = "return_date")
    private LocalDate returnDate;
    
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private BorrowingStatus status = BorrowingStatus.BORROWED;
    
    // Colonnes d'audit
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (borrowDate == null) {
            borrowDate = LocalDate.now();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum BorrowingStatus {
        BORROWED,
        RETURNED,
        OVERDUE
    }
}
