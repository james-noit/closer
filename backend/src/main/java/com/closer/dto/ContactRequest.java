package com.closer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ContactRequest {

    @NotBlank(message = "Contact name is required")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    private String notes;

    private LocalDate birthday;
}
