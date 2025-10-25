package com.my.collabera.controller;

import com.my.collabera.dto.BorrowRequest;
import com.my.collabera.service.BorrowingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrowing")
@RequiredArgsConstructor
@Tag(name = "Borrowing", description = "Book borrowing APIs")
public class BorrowingController {
    private static final Logger log = LogManager.getLogger(BorrowerController.class);
    private final BorrowingService borrowingService;

    @PostMapping("/borrow")
    @Operation(summary = "Borrow a book")
    public ResponseEntity<Void> borrowBook(@Valid @RequestBody BorrowRequest request) {
        log.info("POST /api/borrow - Borrowing book. Book ID: {}, Borrower ID: {}",
                request.getBookId(), request.getBorrowerId());
        try {
            borrowingService.borrowBook(request);
            log.info("Book borrowed successfully - Book ID: {}, Borrower ID: {}",
                    request.getBookId(), request.getBorrowerId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to borrow book - Book ID: {}, Borrower ID: {}, Error: {}",
                    request.getBookId(), request.getBorrowerId(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/return/{bookId}")
    @Operation(summary = "Return a borrowed book")
    public ResponseEntity<Void> returnBook(@PathVariable Long bookId) {
        log.info("PUT /api/books/{}/return - Returning book", bookId);
        try {
            borrowingService.returnBook(bookId);
            log.info("Book returned successfully - Book ID: {}", bookId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to return book - Book ID: {}, Error: {}",
                    bookId, e.getMessage(), e);
            throw e;
        }
    }
}
