package com.my.collabera.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BorrowerResponse {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
}
