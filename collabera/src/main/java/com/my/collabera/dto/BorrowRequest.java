package com.my.collabera.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class BorrowRequest {
    @NotNull(message = "Borrower ID is required")
    private Long borrowerId;

    @NotNull(message = "Book ID is required")
    private Long bookId;

    public Long getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(Long borrowerId) {
        this.borrowerId = borrowerId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
}
