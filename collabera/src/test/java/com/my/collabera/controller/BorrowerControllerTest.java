package com.my.collabera.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.collabera.dto.BorrowerRequest;
import com.my.collabera.dto.BorrowerResponse;
import com.my.collabera.exception.InvalidDataException;
import com.my.collabera.service.BorrowerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BorrowerController.class)
class BorrowerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BorrowerService borrowerService;

    @Test
    void registerBorrower_ValidRequest_ReturnsCreated() throws Exception {
        BorrowerRequest request = new BorrowerRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");

        BorrowerResponse response = new BorrowerResponse(
                1L, "John Doe", "john@example.com", LocalDateTime.now()
        );

        when(borrowerService.registerBorrower(any(BorrowerRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void registerBorrower_InvalidEmail_ReturnsBadRequest() throws Exception {
        BorrowerRequest request = new BorrowerRequest();
        request.setName("John Doe");
        request.setEmail("invalid-email");

        mockMvc.perform(post("/api/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerBorrower_DuplicateEmail_ReturnsConflict() throws Exception {
        BorrowerRequest request = new BorrowerRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");

        when(borrowerService.registerBorrower(any(BorrowerRequest.class)))
                .thenThrow(new InvalidDataException("Email already registered"));

        mockMvc.perform(post("/api/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
