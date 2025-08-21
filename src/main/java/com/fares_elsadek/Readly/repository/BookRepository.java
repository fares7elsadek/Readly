package com.fares_elsadek.Readly.repository;

import com.fares_elsadek.Readly.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book,String> {
     Page<Book> findAllByCreatedByEquals(String userId, Pageable pageable);
}
