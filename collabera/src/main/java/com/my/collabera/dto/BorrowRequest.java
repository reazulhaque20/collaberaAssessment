package com.my.collabera.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BorrowRequest {
    @NotNull(message = "Borrower ID is required")
    private Long borrowerId;

    @NotNull(message = "Book ID is required")
    private Long bookId;
}
