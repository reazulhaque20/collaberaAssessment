package com.my.collabera.controller;

import com.my.collabera.dto.BorrowerRequest;
import com.my.collabera.dto.BorrowerResponse;
import com.my.collabera.service.BorrowerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/borrowers")
@RequiredArgsConstructor
@Tag(name = "Borrowers", description = "Borrower management APIs")
public class BorrowerController {
    private static final Logger log = LogManager.getLogger(BorrowerController.class);
    private final BorrowerService borrowerService;

    @PostMapping
    @Operation(summary = "Register a new borrower")
    public ResponseEntity<BorrowerResponse> registerBorrower(
            @Valid @RequestBody BorrowerRequest request) {
        log.info("POST /api/borrowers - Registering new borrower: {}", request);
        try {
            BorrowerResponse response = borrowerService.registerBorrower(request);
            log.info("Borrower registered successfully - ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Failed to register borrower: {} - Error: {}", request, e.getMessage(), e);
            throw e;
        }
    }
}
