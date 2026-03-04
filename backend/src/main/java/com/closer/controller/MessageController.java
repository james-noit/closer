package com.closer.controller;

import com.closer.dto.MessageRequest;
import com.closer.dto.MessageResponse;
import com.closer.security.UserPrincipal;
import com.closer.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/{contactId}")
    public ResponseEntity<List<MessageResponse>> getConversation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID contactId) {
        return ResponseEntity.ok(messageService.getConversation(principal.getId(), contactId));
    }

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody MessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.sendMessage(principal.getId(), request));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<MessageResponse> markAsRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(messageService.markAsRead(id, principal.getId()));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(messageService.getUnreadCount(principal.getId()));
    }
}
