package com.my.collabera.controller;

import com.my.collabera.dto.BorrowRequest;
import com.my.collabera.service.BorrowingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrowing")
@RequiredArgsConstructor
@Tag(name = "Borrowing", description = "Book borrowing APIs")
public class BorrowingController {
    private final BorrowingService borrowingService;

    @PostMapping("/borrow")
    @Operation(summary = "Borrow a book")
    public ResponseEntity<Void> borrowBook(@Valid @RequestBody BorrowRequest request) {
        borrowingService.borrowBook(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/return/{bookId}")
    @Operation(summary = "Return a borrowed book")
    public ResponseEntity<Void> returnBook(@PathVariable Long bookId) {
        borrowingService.returnBook(bookId);
        return ResponseEntity.ok().build();
    }
}
