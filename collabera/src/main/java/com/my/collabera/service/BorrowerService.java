package com.my.collabera.service;

import com.my.collabera.dto.BorrowerRequest;
import com.my.collabera.dto.BorrowerResponse;
import com.my.collabera.exception.InvalidDataException;
import com.my.collabera.model.Borrower;
import com.my.collabera.repository.BorrowerRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class BorrowerService {
    private static final Logger log = LogManager.getLogger(BorrowerService.class);
    private final BorrowerRepository borrowerRepository;

    @Transactional // Ensures atomicity for the check (existsByEmail) and the write operation (save).
    public BorrowerResponse registerBorrower(BorrowerRequest request) {
        log.info("Starting borrower registration - Name: {}, Email: {}",
                request.getName(), request.getEmail());
        long startTime = System.currentTimeMillis(); // Start timer.
        try {
            // Uniqueness Validation
            log.debug("Checking email uniqueness: {}", request.getEmail());
            // Use existsBy...() to perform an efficient database check without fetching unnecessary data.
            boolean emailExists = borrowerRepository.existsByEmail(request.getEmail());
            if (emailExists) {
                // Log at a high level (ERROR/WARN) as this is a failure point.
                log.error("Borrower registration failed - Email already registered: {}",
                        request.getEmail());
                // Throw a specific exception signaling a business/validation error.
                throw new InvalidDataException("Email already registered: " + request.getEmail());
            }

            // Entity Mapping
            log.debug("Email validation passed - Creating new borrower entity");
            Borrower borrower = new Borrower();
            // Simple manual mapping from Request DTO to Entity.
            borrower.setName(request.getName());
            borrower.setEmail(request.getEmail());

            // Persistence
            log.debug("Saving borrower to database");
            Borrower saved = borrowerRepository.save(borrower); // Persist the new entity.
            log.info("Borrower saved successfully - Generated ID: {}", saved.getId());

            // Response Mapping
            // Mapping the saved entity back to a Response DTO is good practice for decoupling.
            BorrowerResponse response = new BorrowerResponse(
                    saved.getId(),
                    saved.getName(),
                    saved.getEmail(),
                    saved.getCreatedAt() // Including metadata like creation time in the response.
            );

            // Successful Completion Logging
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Borrower registration completed successfully - ID: {}, Name: {}, Time: {}ms",
                    response.getId(), response.getName(), executionTime);
            return response;
        } catch (InvalidDataException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            // Log expected failures at WARN level.
            log.warn("Borrower registration rejected after {}ms - Email conflict: {}",
                    executionTime, request.getEmail());
            throw e; // Re-throw to propagate the exception and trigger a possible rollback/status code mapping.
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            // Log unexpected errors at ERROR level, including the stack trace (e) for diagnosis.
            log.error("Borrower registration failed after {}ms - Name: {}, Email: {}, Error: {}",
                    executionTime, request.getName(), request.getEmail(), e.getMessage(), e);
            throw e; // Re-throw to ensure transaction rollback and error propagation.
        }
    }
}
