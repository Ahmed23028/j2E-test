package com.example.flywaydemo.service;

import com.example.flywaydemo.entity.Book;
import com.example.flywaydemo.entity.Category;
import com.example.flywaydemo.repository.BookRepository;
import com.example.flywaydemo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {
    
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
    
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }
    
    public List<Book> searchBooks(String keyword) {
        return bookRepository.searchBooks(keyword);
    }
    
    public List<Book> getBooksByCategory(Long categoryId) {
        return bookRepository.findByCategoryId(categoryId);
    }
    
    public List<Book> getAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }
    
    @Transactional
    public Book createBook(Book book) {
        if (book.getCategory() != null && book.getCategory().getId() != null) {
            Optional<Category> category = categoryRepository.findById(book.getCategory().getId());
            category.ifPresent(book::setCategory);
        }
        return bookRepository.save(book);
    }
    
    @Transactional
    public Optional<Book> updateBook(Long id, Book bookDetails) {
        return bookRepository.findById(id).map(book -> {
            book.setTitle(bookDetails.getTitle());
            book.setAuthor(bookDetails.getAuthor());
            book.setIsbn(bookDetails.getIsbn());
            book.setPrice(bookDetails.getPrice());
            book.setStockQuantity(bookDetails.getStockQuantity());
            book.setDescription(bookDetails.getDescription());
            if (bookDetails.getCategory() != null && bookDetails.getCategory().getId() != null) {
                categoryRepository.findById(bookDetails.getCategory().getId())
                    .ifPresent(book::setCategory);
            }
            return bookRepository.save(book);
        });
    }
    
    @Transactional
    public boolean deleteBook(Long id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
