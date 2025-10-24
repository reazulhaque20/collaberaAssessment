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
        log.info("Register a book: {}", request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookService.registerBook(request));
    }

    @GetMapping
    @Operation(summary = "Get all books")
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }
}
