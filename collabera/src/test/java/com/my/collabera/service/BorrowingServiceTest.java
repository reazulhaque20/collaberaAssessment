package com.my.collabera.service;

import com.my.collabera.dto.BorrowRequest;
import com.my.collabera.exception.BookAlreadyBorrowedException;
import com.my.collabera.exception.ResourceNotFoundException;
import com.my.collabera.model.Book;
import com.my.collabera.model.Borrower;
import com.my.collabera.model.BorrowingRecord;
import com.my.collabera.repository.BookRepository;
import com.my.collabera.repository.BorrowerRepository;
import com.my.collabera.repository.BorrowingRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowingServiceTest {

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowingRecordRepository borrowingRecordRepository;

    @InjectMocks
    private BorrowingService borrowingService;

    private Borrower borrower;
    private Book availableBook;
    private Book borrowedBook;
    private BorrowRequest borrowRequest;

    @BeforeEach
    void setUp() {
        borrower = new Borrower();
        borrower.setId(1L);
        borrower.setName("John Doe");
        borrower.setEmail("john@example.com");

        availableBook = new Book();
        availableBook.setId(1L);
        availableBook.setIsbn("978-0134685991");
        availableBook.setTitle("Effective Java");
        availableBook.setAuthor("Joshua Bloch");
        availableBook.setIsBorrowed(false);

        borrowedBook = new Book();
        borrowedBook.setId(2L);
        borrowedBook.setIsbn("978-0134685991");
        borrowedBook.setTitle("Effective Java");
        borrowedBook.setAuthor("Joshua Bloch");
        borrowedBook.setIsBorrowed(true);

        borrowRequest = new BorrowRequest();
        borrowRequest.setBorrowerId(1L);
        borrowRequest.setBookId(1L);
    }

    @Test
    void borrowBook_Success() {
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(availableBook));
        when(bookRepository.save(any(Book.class))).thenReturn(availableBook);

        borrowingService.borrowBook(borrowRequest);

        assertTrue(availableBook.getIsBorrowed());
        verify(bookRepository, times(1)).save(availableBook);
        verify(borrowingRecordRepository, times(1)).save(any(BorrowingRecord.class));
    }

    @Test
    void borrowBook_BorrowerNotFound_ThrowsException() {
        when(borrowerRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> borrowingService.borrowBook(borrowRequest)
        );

        assertEquals("Borrower not found", exception.getMessage());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void borrowBook_BookNotFound_ThrowsException() {
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> borrowingService.borrowBook(borrowRequest)
        );

        assertEquals("Book not found", exception.getMessage());
        verify(borrowingRecordRepository, never()).save(any(BorrowingRecord.class));
    }

    @Test
    void borrowBook_AlreadyBorrowed_ThrowsException() {
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(borrowedBook));

        borrowRequest.setBookId(2L);

        BookAlreadyBorrowedException exception = assertThrows(
                BookAlreadyBorrowedException.class,
                () -> borrowingService.borrowBook(borrowRequest)
        );

        assertEquals("Book is already borrowed", exception.getMessage());
        verify(borrowingRecordRepository, never()).save(any(BorrowingRecord.class));
    }

    @Test
    void returnBook_Success() {
        BorrowingRecord record = new BorrowingRecord();
        record.setId(1L);
        record.setBorrower(borrower);
        record.setBook(borrowedBook);

        when(bookRepository.findById(2L)).thenReturn(Optional.of(borrowedBook));
        when(borrowingRecordRepository.findByBookIdAndReturnedAtIsNull(2L))
                .thenReturn(Optional.of(record));

        borrowingService.returnBook(2L);

        assertFalse(borrowedBook.getIsBorrowed());
        assertNotNull(record.getReturnedAt());
        verify(bookRepository, times(1)).save(borrowedBook);
        verify(borrowingRecordRepository, times(1)).save(record);
    }

    @Test
    void returnBook_BookNotFound_ThrowsException() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> borrowingService.returnBook(1L)
        );

        assertEquals("Book not found", exception.getMessage());
    }

    @Test
    void returnBook_NotBorrowed_ThrowsException() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(availableBook));

        BookAlreadyBorrowedException exception = assertThrows(
                BookAlreadyBorrowedException.class,
                () -> borrowingService.returnBook(1L)
        );

        assertEquals("Book is not currently borrowed", exception.getMessage());
    }
}
