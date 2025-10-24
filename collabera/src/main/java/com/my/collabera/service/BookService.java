package com.my.collabera.service;

import com.my.collabera.dto.BookRequest;
import com.my.collabera.dto.BookResponse;
import com.my.collabera.exception.InvalidDataException;
import com.my.collabera.model.Book;
import com.my.collabera.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    @Transactional
    public BookResponse registerBook(BookRequest request) {
        // Validate ISBN consistency
        List<Book> existingBooks = bookRepository.findByIsbn(request.getIsbn());
        if (!existingBooks.isEmpty()) {
            Book existing = existingBooks.get(0);
            if (!existing.getTitle().equals(request.getTitle()) ||
                    !existing.getAuthor().equals(request.getAuthor())) {
                throw new InvalidDataException(
                        "Books with same ISBN must have same title and author"
                );
            }
        }

        Book book = new Book();
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsBorrowed(false);

        Book saved = bookRepository.save(book);
        return new BookResponse(
                saved.getId(),
                saved.getIsbn(),
                saved.getTitle(),
                saved.getAuthor(),
                saved.getIsBorrowed()
        );
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(book -> new BookResponse(
                        book.getId(),
                        book.getIsbn(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getIsBorrowed()
                ))
                .collect(Collectors.toList());
    }
}
