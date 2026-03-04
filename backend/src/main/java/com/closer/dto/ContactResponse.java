package com.closer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponse {

    private UUID id;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime lastInteractionDate;
    private LocalDateTime createdAt;
    private String notes;
    private LocalDate birthday;
    private boolean isActive;
    private int group;
    private String color;

    public static String colorForGroup(int group) {
        return switch (group) {
            case 1 -> "green";
            case 2 -> "light-green";
            case 3 -> "yellow";
            case 4 -> "orange";
            default -> "red";
        };
    }
}
