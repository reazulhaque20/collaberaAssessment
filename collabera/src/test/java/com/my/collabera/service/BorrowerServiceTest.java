package com.my.collabera.service;

import com.my.collabera.dto.BorrowerRequest;
import com.my.collabera.dto.BorrowerResponse;
import com.my.collabera.exception.InvalidDataException;
import com.my.collabera.model.Borrower;
import com.my.collabera.repository.BorrowerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowerServiceTest {

    @Mock
    private BorrowerRepository borrowerRepository;

    @InjectMocks
    private BorrowerService borrowerService;

    private BorrowerRequest validRequest;
    private Borrower savedBorrower;

    @BeforeEach
    void setUp() {
        validRequest = new BorrowerRequest();
        validRequest.setName("John Doe");
        validRequest.setEmail("john@example.com");

        savedBorrower = new Borrower();
        savedBorrower.setId(1L);
        savedBorrower.setName("John Doe");
        savedBorrower.setEmail("john@example.com");
        savedBorrower.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void registerBorrower_Success() {
        when(borrowerRepository.existsByEmail(anyString())).thenReturn(false);
        when(borrowerRepository.save(any(Borrower.class))).thenReturn(savedBorrower);

        BorrowerResponse response = borrowerService.registerBorrower(validRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());

        verify(borrowerRepository, times(1)).existsByEmail("john@example.com");
        verify(borrowerRepository, times(1)).save(any(Borrower.class));
    }

    @Test
    void registerBorrower_EmailAlreadyExists_ThrowsException() {
        when(borrowerRepository.existsByEmail(anyString())).thenReturn(true);

        InvalidDataException exception = assertThrows(
                InvalidDataException.class,
                () -> borrowerService.registerBorrower(validRequest)
        );

        assertEquals("Email already registered", exception.getMessage());
        verify(borrowerRepository, never()).save(any(Borrower.class));
    }
}
