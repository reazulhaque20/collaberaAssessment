package com.my.collabera.service;

import com.my.collabera.dto.BorrowRequest;
import com.my.collabera.exception.BookAlreadyBorrowedException;
import com.my.collabera.exception.ResourceNotFoundException;
import com.my.collabera.model.Book;
import com.my.collabera.model.Borrower;
import com.my.collabera.model.BorrowingRecord;
import com.my.collabera.repository.BookRepository;
import com.my.collabera.repository.BorrowerRepository;
import com.my.collabera.repository.BorrowingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BorrowingService {
    private final BorrowerRepository borrowerRepository;
    private final BookRepository bookRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;

    @Transactional
    public void borrowBook(BorrowRequest request) {
        Borrower borrower = borrowerRepository.findById(request.getBorrowerId())
                .orElseThrow(() -> new ResourceNotFoundException("Borrower not found"));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        if (book.getIsBorrowed()) {
            throw new BookAlreadyBorrowedException("Book is already borrowed");
        }

        book.setIsBorrowed(true);
        bookRepository.save(book);

        BorrowingRecord record = new BorrowingRecord();
        record.setBorrower(borrower);
        record.setBook(book);
        borrowingRecordRepository.save(record);
    }

    @Transactional
    public void returnBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        if (!book.getIsBorrowed()) {
            throw new BookAlreadyBorrowedException("Book is not currently borrowed");
        }

        BorrowingRecord record = borrowingRecordRepository
                .findByBookIdAndReturnedAtIsNull(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("No active borrowing record found"));

        record.setReturnedAt(LocalDateTime.now());
        borrowingRecordRepository.save(record);

        book.setIsBorrowed(false);
        bookRepository.save(book);
    }
}
