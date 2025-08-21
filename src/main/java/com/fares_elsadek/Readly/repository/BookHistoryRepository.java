package com.fares_elsadek.Readly.repository;

import com.fares_elsadek.Readly.entity.BookTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookHistoryRepository extends JpaRepository<BookTransaction,String> {
    @Query("""
            SELECT transaction FROM BookTransaction transaction WHERE transaction.user.id = :userId 
            """)
    Page<BookTransaction> findAllBorrowedBooks(String userId, Pageable pageable);

    @Query("""
            SELECT transaction FROM BookTransaction 
            transaction WHERE transaction.user.id = :userId 
            AND transaction.returned = true
            AND transaction.returnApproved = true
            """)
    Page<BookTransaction> findAllReturnedBooks(String userId, Pageable pageable);

    @Query("""
            SELECT (COUNT(*)) AS isBorrowed
            FROM BookTransaction transaction WHERE
            transaction.user.id = :userId AND
            transaction.book.id = :bookId AND
            transaction.returned = false
            """)
    boolean isAlreadyBorrowedByCurrentUser(String userId,String bookId);

    @Query("""
            SELECT (COUNT(*)) AS isBorrowed
            FROM BookTransaction transaction WHERE
            transaction.book.id = :bookId AND
            transaction.returned = false
            """)
    boolean isAlreadyBorrowed(String bookId);


    @Query("""
            SELECT transaction
            FROM BookTransaction transaction WHERE
            transaction.book.id = :bookId AND
            transaction.user.id = :userId AND
            transaction.returned = false
            """)
    Optional<BookTransaction> findByBookIdAndUserId(String userId, String bookId);





}
