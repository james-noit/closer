package com.closer.dto;

import com.closer.model.Reminder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderResponse {

    private UUID id;
    private String title;
    private String description;
    private LocalDateTime reminderDate;
    private boolean isCompleted;
    private Reminder.ReminderType type;
    private UUID contactId;
    private String contactName;
    private LocalDateTime createdAt;
}
