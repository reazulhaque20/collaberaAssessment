package com.my.collabera.controller;

import com.my.collabera.dto.BookRequest;
import com.my.collabera.dto.BookResponse;
import com.my.collabera.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Book management APIs")
public class BookController {
    private static final Logger log = LogManager.getLogger(BookController.class);
    private final BookService bookService;

    @PostMapping
    @Operation(summary = "Register a new book")
    public ResponseEntity<BookResponse> registerBook(
            @Valid @RequestBody BookRequest request) {
        log.info("Starting book registration process for request: {}", request);

        try {
            log.debug("Validating book request: {}", request);
            BookResponse response = bookService.registerBook(request);
            log.info("Book registered successfully with ID: {}, Title: {}",
                    response.getId(), response.getTitle());
            log.debug("Complete book registration response: {}", response);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Failed to register book. Request: {}, Error: {}", request, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    @Operation(summary = "Get all books")
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        log.info("GET /api/books - Retrieving all books");
        try {
            List<BookResponse> books = bookService.getAllBooks();
            log.info("Successfully retrieved {} books", books.size());
            if (log.isDebugEnabled()) {
                log.debug("Retrieved books: {}", books);
            }
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            log.error("Failed to retrieve books: {}", e.getMessage(), e);
            throw e;
        }
    }
}
