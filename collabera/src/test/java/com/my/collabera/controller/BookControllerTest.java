package com.my.collabera.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.collabera.dto.BookRequest;
import com.my.collabera.dto.BookResponse;
import com.my.collabera.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @Test
    void registerBook_ValidRequest_ReturnsCreated() throws Exception {
        BookRequest request = new BookRequest();
        request.setIsbn("978-0134685991");
        request.setTitle("Effective Java");
        request.setAuthor("Joshua Bloch");

        BookResponse response = new BookResponse(
                1L, "978-0134685991", "Effective Java", "Joshua Bloch", false
        );

        when(bookService.registerBook(any(BookRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.isbn").value("978-0134685991"))
                .andExpect(jsonPath("$.title").value("Effective Java"))
                .andExpect(jsonPath("$.author").value("Joshua Bloch"))
                .andExpect(jsonPath("$.isBorrowed").value(false));
    }

    @Test
    void getAllBooks_ReturnsBookList() throws Exception {
        List<BookResponse> books = Arrays.asList(
                new BookResponse(1L, "978-0134685991", "Effective Java", "Joshua Bloch", false),
                new BookResponse(2L, "978-0134685991", "Effective Java", "Joshua Bloch", true)
        );

        when(bookService.getAllBooks()).thenReturn(books);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].isBorrowed").value(true));
    }
}
