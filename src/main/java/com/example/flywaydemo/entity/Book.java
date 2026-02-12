package com.example.flywaydemo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité Book correspondant à la table books créée dans V2__add_books_table.sql
 * La colonne author a été ajoutée dans V3__add_author_column.sql
 * Les colonnes d'audit ont été ajoutées dans V5__add_audit_columns.sql
 */
@Entity
@Table(name = "books", indexes = {
    @Index(name = "idx_books_title", columnList = "title"),
    @Index(name = "idx_books_author", columnList = "author"),
    @Index(name = "idx_books_isbn", columnList = "isbn"),
    @Index(name = "idx_books_category_stock", columnList = "category_id, stock_quantity")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(unique = true, length = 20)
    private String isbn;
    
    // Colonne author ajoutée dans V3__add_author_column.sql
    @Column(nullable = false, length = 255)
    private String author;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "books"})
    private Category category;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    // Colonnes d'audit ajoutées dans V5__add_audit_columns.sql
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relation avec les emprunts (ajoutée dans V6__add_borrowings_table.sql)
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"book", "user"})
    private List<Borrowing> borrowings = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
