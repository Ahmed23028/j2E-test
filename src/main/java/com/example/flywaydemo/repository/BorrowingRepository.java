package com.example.flywaydemo.repository;

import com.example.flywaydemo.entity.Borrowing;
import com.example.flywaydemo.entity.Borrowing.BorrowingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {
    List<Borrowing> findByUserId(Long userId);
    List<Borrowing> findByBookId(Long bookId);
    List<Borrowing> findByStatus(BorrowingStatus status);
    List<Borrowing> findByDueDateBeforeAndStatus(LocalDate date, BorrowingStatus status);
}
