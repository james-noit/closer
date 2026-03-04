package com.closer.dto;

import com.closer.model.Reminder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReminderRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Reminder date is required")
    private LocalDateTime reminderDate;

    private UUID contactId;

    @NotNull(message = "Reminder type is required")
    private Reminder.ReminderType type;
}
