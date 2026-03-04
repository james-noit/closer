package com.closer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MessageRequest {

    @NotNull(message = "Receiver ID is required")
    private UUID receiverId;

    @NotBlank(message = "Message content is required")
    private String content;
}
