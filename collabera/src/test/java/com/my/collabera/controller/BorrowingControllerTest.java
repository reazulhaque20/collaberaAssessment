package com.my.collabera.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.collabera.dto.BorrowRequest;
import com.my.collabera.exception.BookAlreadyBorrowedException;
import com.my.collabera.exception.ResourceNotFoundException;
import com.my.collabera.service.BorrowingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BorrowingController.class)
class BorrowingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BorrowingService borrowingService;

    @Test
    void borrowBook_ValidRequest_ReturnsOk() throws Exception {
        BorrowRequest request = new BorrowRequest();
        request.setBorrowerId(1L);
        request.setBookId(1L);

        doNothing().when(borrowingService).borrowBook(any(BorrowRequest.class));

        mockMvc.perform(post("/api/borrowing/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void borrowBook_BookNotFound_ReturnsNotFound() throws Exception {
        BorrowRequest request = new BorrowRequest();
        request.setBorrowerId(1L);
        request.setBookId(999L);

        doThrow(new ResourceNotFoundException("Book not found"))
                .when(borrowingService).borrowBook(any(BorrowRequest.class));

        mockMvc.perform(post("/api/borrowing/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void borrowBook_AlreadyBorrowed_ReturnsConflict() throws Exception {
        BorrowRequest request = new BorrowRequest();
        request.setBorrowerId(1L);
        request.setBookId(1L);

        doThrow(new BookAlreadyBorrowedException("Book is already borrowed"))
                .when(borrowingService).borrowBook(any(BorrowRequest.class));

        mockMvc.perform(post("/api/borrowing/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void returnBook_ValidRequest_ReturnsOk() throws Exception {
        doNothing().when(borrowingService).returnBook(anyLong());

        mockMvc.perform(post("/api/borrowing/return/1"))
                .andExpect(status().isOk());
    }

    @Test
    void returnBook_BookNotFound_ReturnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Book not found"))
                .when(borrowingService).returnBook(anyLong());

        mockMvc.perform(post("/api/borrowing/return/999"))
                .andExpect(status().isNotFound());
    }
}
