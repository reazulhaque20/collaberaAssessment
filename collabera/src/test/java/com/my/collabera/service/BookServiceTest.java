package com.my.collabera.service;

import com.my.collabera.dto.BookRequest;
import com.my.collabera.dto.BookResponse;
import com.my.collabera.exception.InvalidDataException;
import com.my.collabera.model.Book;
import com.my.collabera.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private BookRequest validRequest;
    private Book savedBook;

    @BeforeEach
    void setUp() {
        validRequest = new BookRequest();
        validRequest.setIsbn("978-0134685991");
        validRequest.setTitle("Effective Java");
        validRequest.setAuthor("Joshua Bloch");

        savedBook = new Book();
        savedBook.setId(1L);
        savedBook.setIsbn("978-0134685991");
        savedBook.setTitle("Effective Java");
        savedBook.setAuthor("Joshua Bloch");
        savedBook.setIsBorrowed(false);
    }

    @Test
    void registerBook_NewIsbn_Success() {
        when(bookRepository.findByIsbn(anyString())).thenReturn(List.of());
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        BookResponse response = bookService.registerBook(validRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("978-0134685991", response.getIsbn());
        assertEquals("Effective Java", response.getTitle());
        assertEquals("Joshua Bloch", response.getAuthor());
        assertFalse(response.getIsBorrowed());

        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void registerBook_SameIsbnSameTitleAuthor_Success() {
        Book existingBook = new Book();
        existingBook.setIsbn("978-0134685991");
        existingBook.setTitle("Effective Java");
        existingBook.setAuthor("Joshua Bloch");

        when(bookRepository.findByIsbn(anyString())).thenReturn(List.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        BookResponse response = bookService.registerBook(validRequest);

        assertNotNull(response);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void registerBook_SameIsbnDifferentTitle_ThrowsException() {
        Book existingBook = new Book();
        existingBook.setIsbn("978-0134685991");
        existingBook.setTitle("Different Title");
        existingBook.setAuthor("Joshua Bloch");

        when(bookRepository.findByIsbn(anyString())).thenReturn(List.of(existingBook));

        InvalidDataException exception = assertThrows(
                InvalidDataException.class,
                () -> bookService.registerBook(validRequest)
        );

        assertEquals("Books with same ISBN must have same title and author",
                exception.getMessage());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void getAllBooks_ReturnsAllBooks() {
        Book book1 = new Book();
        book1.setId(1L);
        book1.setIsbn("978-0134685991");
        book1.setTitle("Effective Java");
        book1.setAuthor("Joshua Bloch");
        book1.setIsBorrowed(false);

        Book book2 = new Book();
        book2.setId(2L);
        book2.setIsbn("978-0134685991");
        book2.setTitle("Effective Java");
        book2.setAuthor("Joshua Bloch");
        book2.setIsBorrowed(true);

        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

        List<BookResponse> books = bookService.getAllBooks();

        assertEquals(2, books.size());
        assertEquals(1L, books.get(0).getId());
        assertEquals(2L, books.get(1).getId());
        assertTrue(books.get(1).getIsBorrowed());
    }
}
