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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BorrowingService {
    private static final Logger log = LogManager.getLogger(BorrowerService.class);
    private final BorrowerRepository borrowerRepository;
    private final BookRepository bookRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;

    @Transactional // Ensures the entire operation (fetch borrower, fetch book, update book, create record) is atomic. Essential for data integrity.
    public void borrowBook(BorrowRequest request) {
        log.info("Starting book borrowing process - Book ID: {}, Borrower ID: {}",
                request.getBookId(), request.getBorrowerId());
        long startTime = System.currentTimeMillis(); // Start time tracker for performance logging.
        try {
            // Fetch and Validate Borrower Existence
            log.debug("Fetching borrower with ID: {}", request.getBorrowerId());
            Borrower borrower = borrowerRepository.findById(request.getBorrowerId())
                    // Use orElseThrow for concise Optional handling. Throws ResourceNotFoundException if ID is invalid.
                    .orElseThrow(() -> {
                        log.error("Borrower not found - Borrower ID: {}", request.getBorrowerId());
                        return new ResourceNotFoundException("Borrower not found with ID: " + request.getBorrowerId());
                    });
            log.debug("Borrower found - ID: {}, Name: {}", borrower.getId(), borrower.getName());

            // Fetch and Validate Book Existence
            log.debug("Fetching book with ID: {}", request.getBookId());
            Book book = bookRepository.findById(request.getBookId())
                    .orElseThrow(() -> {
                        log.error("Book not found - Book ID: {}", request.getBookId());
                        return new ResourceNotFoundException("Book not found with ID: " + request.getBookId());
                    });
            log.debug("Book found - ID: {}, Title: '{}', Currently borrowed: {}",
                    book.getId(), book.getTitle(), book.getIsBorrowed());

            // Validate Book Availability (Critical Business Rule)
            if (book.getIsBorrowed()) {
                // Log failure at ERROR/WARN level for a business rule violation.
                log.error("Book borrowing failed - Book already borrowed - Book ID: {}, Title: '{}'",
                        book.getId(), book.getTitle());
                throw new BookAlreadyBorrowedException( // Throws a specific, actionable exception.
                        "Book '" + book.getTitle() + "' is already borrowed"
                );
            }

            // Update Book Status
            log.debug("Updating book status to borrowed");
            book.setIsBorrowed(true);
            bookRepository.save(book); // Persist the updated book status.
            log.debug("Book status updated successfully");

            // Create Borrowing Record
            log.debug("Creating borrowing record");
            BorrowingRecord record = new BorrowingRecord();
            record.setBorrower(borrower); // Establish relationship to Borrower.
            record.setBook(book);         // Establish relationship to Book.
            // NOTE: The transaction will ensure this new record is only committed if the entire method succeeds.
            borrowingRecordRepository.save(record);
            log.debug("Borrowing record created successfully");

            // Successful Completion Logging
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Book borrowing completed successfully - Book: '{}', Borrower: '{}', Time: {}ms",
                    book.getTitle(), borrower.getName(), executionTime);

            // Error Handling - Specific Business Exceptions
        } catch (ResourceNotFoundException | BookAlreadyBorrowedException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            // Log expected failures at WARN level.
            log.warn("Book borrowing failed after {}ms - Book ID: {}, Borrower ID: {}, Reason: {}",
                    executionTime, request.getBookId(), request.getBorrowerId(), e.getMessage());
            throw e; // Re-throw to trigger rollback and exception propagation.

            // Error Handling - Unexpected System Exceptions
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            // Log unexpected failures at ERROR level, including the stack trace (e) for detailed analysis.
            log.error("Unexpected error during book borrowing after {}ms - Book ID: {}, Borrower ID: {}, Error: {}",
                    executionTime, request.getBookId(), request.getBorrowerId(), e.getMessage(), e);
            throw e; // Re-throw to trigger rollback and exception propagation.
        }
    }

    @Transactional // Ensures the entire method executes within a single database transaction. Critical for data consistency (book status AND record update must both succeed or fail).
    public void returnBook(Long bookId) {
        log.info("Starting book return process - Book ID: {}", bookId);
        long startTime = System.currentTimeMillis(); // Start time tracker for performance logging.
        try {
            // Fetch Book and Validate Existence
            log.debug("Fetching book with ID: {}", bookId);
            Book book = bookRepository.findById(bookId)
                    // Use orElseThrow for concise Optional handling and to throw a specific exception.
                    .orElseThrow(() -> {
                        // Log the failure before throwing the exception, ensuring the context is saved.
                        log.error("Book not found for return - Book ID: {}", bookId);
                        return new ResourceNotFoundException("Book not found with ID: " + bookId);
                    });

            log.debug("Book found - ID: {}, Title: '{}', Currently borrowed: {}",
                    book.getId(), book.getTitle(), book.getIsBorrowed());

            // Validate Book Status (Business Rule Check)
            if (!book.getIsBorrowed()) {
                // Log a warning for an expected business rule violation (e.g., user error).
                log.warn("Book return failed - Book is not currently borrowed - Book ID: {}, Title: '{}'",
                        book.getId(), book.getTitle());
                throw new BookAlreadyBorrowedException("Book '" + book.getTitle() + "' is not currently borrowed");
            }

            // Find Active Borrowing Record
            log.debug("Searching for active borrowing record for book ID: {}", bookId);
            BorrowingRecord record = borrowingRecordRepository
                    // Efficiently finds the record associated with the book that hasn't been returned yet (ReturnedAtIsNull).
                    .findByBookIdAndReturnedAtIsNull(bookId)
                    .orElseThrow(() -> {
                        // Log an error if the record is missing, indicating a data integrity issue or unexpected state.
                        log.error("No active borrowing record found - Book ID: {}, Title: '{}'",
                                bookId, book.getTitle());
                        return new ResourceNotFoundException("No active borrowing record found for book: " + book.getTitle());
                    });

            log.debug("Active borrowing record found - Record ID: {}, Borrower: {}",
                    record.getId(), record.getBorrower().getName());

            // Update Borrowing Record
            log.debug("Updating borrowing record with return timestamp");
            record.setReturnedAt(LocalDateTime.now()); // Set the return timestamp.
            borrowingRecordRepository.save(record); // Persist the updated record.
            log.debug("Borrowing record updated successfully");

            // Update Book Status
            log.debug("Updating book status to available");
            book.setIsBorrowed(false); // Change the book status back to available.
            bookRepository.save(book); // Persist the updated book status.
            log.debug("Book status updated successfully");

            // Successful Completion Logging
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Book return completed successfully - Book: '{}', Borrower: '{}', Time: {}ms",
                    book.getTitle(), record.getBorrower().getName(), executionTime);

            // Error Handling - Specific Business Exceptions
        } catch (ResourceNotFoundException | BookAlreadyBorrowedException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            // Log expected failures at WARN level. The transaction will be rolled back automatically.
            log.warn("Book return failed after {}ms - Book ID: {}, Reason: {}",
                    executionTime, bookId, e.getMessage());
            throw e; // Re-throw to allow Spring to handle the rollback and exception propagation (e.g., to a controller).

            // Error Handling - Unexpected System Exceptions
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            // Log unexpected errors at ERROR level, including the stack trace (e) for debugging.
            log.error("Unexpected error during book return after {}ms - Book ID: {}, Error: {}",
                    executionTime, bookId, e.getMessage(), e);
            throw e; // Re-throw to ensure the transaction rolls back and the error propagates.
        }
    }
}
