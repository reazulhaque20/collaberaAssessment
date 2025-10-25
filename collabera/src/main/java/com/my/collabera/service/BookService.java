package com.my.collabera.service;

import com.my.collabera.dto.BookRequest;
import com.my.collabera.dto.BookResponse;
import com.my.collabera.exception.InvalidDataException;
import com.my.collabera.model.Book;
import com.my.collabera.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private static final Logger log = LogManager.getLogger(BookService.class);
    private final BookRepository bookRepository;

    @Transactional // Ensures the ISBN check and the book save are atomic.
    public BookResponse registerBook(BookRequest request) {
        log.info("Starting book registration - ISBN: {}, Title: {}, Author: {}",
                request.getIsbn(), request.getTitle(), request.getAuthor());
        // NOTE: Adding back the performance tracker for consistency with other methods.
        long startTime = System.currentTimeMillis();
        try {
            // Business Validation: Check for ISBN consistency
            log.debug("Checking for existing books with ISBN: {}", request.getIsbn());
            // Finds all copies of the book. Returning a list is appropriate as ISBN is not unique ID.
            List<Book> existingBooks = bookRepository.findByIsbn(request.getIsbn());

            if (!existingBooks.isEmpty()) {
                Book existing = existingBooks.get(0); // Only need to check the details of one existing copy.
                log.warn("Found existing book with same ISBN - Existing ID: {}, Title: {}, Author: {}",
                        existing.getId(), existing.getTitle(), existing.getAuthor());

                // Core business rule: all copies of the same ISBN must share the same title and author.
                if (!existing.getTitle().equals(request.getTitle()) ||
                        !existing.getAuthor().equals(request.getAuthor())) {

                    // Detailed error logging is crucial for diagnosing ISBN conflicts.
                    log.error("ISBN conflict detected - Existing: '{}' by '{}', Requested: '{}' by '{}'",
                            existing.getTitle(), existing.getAuthor(),
                            request.getTitle(), request.getAuthor());
                    throw new InvalidDataException(
                            "Books with same ISBN must have same title and author"
                    );
                }
                log.info("ISBN matches existing book with consistent details - Proceeding with registration");
            } else {
                log.debug("No existing books found with ISBN: {}", request.getIsbn());
            }

            // Entity Creation
            log.debug("Creating new book entity");
            Book book = new Book();
            book.setIsbn(request.getIsbn());
            book.setTitle(request.getTitle());
            book.setAuthor(request.getAuthor());
            book.setIsBorrowed(false); // New books are always available by default.

            // Persistence
            log.debug("Saving book to database");
            Book saved = bookRepository.save(book);
            log.info("Book saved successfully - Generated ID: {}", saved.getId());

            // Response Mapping and Completion Logging
            BookResponse response = new BookResponse( // Mapping to DTO decouples presentation from persistence.
                    saved.getId(),
                    saved.getIsbn(),
                    saved.getTitle(),
                    saved.getAuthor(),
                    saved.getIsBorrowed()
            );
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Book registration completed successfully - Book ID: {}, Title: {}, Time: {}ms",
                    response.getId(), response.getTitle(), executionTime);
            return response;

            // Error Handling - Validation Failure
        } catch (InvalidDataException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Book registration failed due to data validation after {}ms - ISBN: {}, Error: {}",
                    executionTime, request.getIsbn(), e.getMessage());
            throw e; // Re-throw to ensure transaction rollback.

            // Error Handling - Unexpected System Error
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            // Logs error with stack trace (e) for debugging.
            log.error("Unexpected error during book registration after {}ms - ISBN: {}, Title: {}, Error: {}",
                    executionTime, request.getIsbn(), request.getTitle(), e.getMessage(), e);
            throw e; // Re-throw to ensure transaction rollback.
        }
    }

    @Transactional(readOnly = true) // Crucial for performance: advises the persistence context to perform read-only optimizations.
    public List<BookResponse> getAllBooks() {
        log.info("Starting to retrieve all books from database");
        long startTime = System.currentTimeMillis();
        try {
            // Data Retrieval
            log.debug("Executing findAll() on book repository");
            List<Book> books = bookRepository.findAll(); // Fetches all Book entities.

            log.debug("Found {} books in database", books.size());

            // Conditional Logging for Empty Result Set
            if (books.isEmpty()) {
                log.warn("No books found in database - returning empty list");
            } else {
                log.debug("Mapping {} books to response DTOs", books.size());
            }

            // Mapping to DTOs
            // Uses the Java Stream API for efficient and modern mapping from Entity to Response DTO.
            List<BookResponse> response = books.stream()
                    .map(book -> new BookResponse( // Maps Book entity fields to the BookResponse DTO.
                            book.getId(),
                            book.getIsbn(),
                            book.getTitle(),
                            book.getAuthor(),
                            book.getIsBorrowed() // Ensures only necessary data is exposed (decoupling).
                    ))
                    .collect(Collectors.toList());

            // Successful Completion Logging and Performance Metrics
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Successfully retrieved {} books in {}ms",
                    response.size(), executionTime);

            // Value-Added Statistics Logging
            if (!response.isEmpty()) {
                // Efficiently calculates borrowed count using streaming and filtering.
                long borrowedCount = response.stream()
                        .filter(BookResponse::getIsBorrowed)
                        .count();
                // Logs useful statistics for monitoring inventory status.
                log.info("Books statistics - Total: {}, Borrowed: {}, Available: {}",
                        response.size(), borrowedCount, response.size() - borrowedCount);
            }

            return response; // Returns the list of DTOs.

            // Generic Error Handling (Appropriate for read-only methods)
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            // Logs the failure at ERROR level, including the stack trace (e).
            log.error("Failed to retrieve books after {}ms - Error: {}",
                    executionTime, e.getMessage(), e);
            throw e; // Re-throws to propagate the error up the call stack.
        }
    }
}
