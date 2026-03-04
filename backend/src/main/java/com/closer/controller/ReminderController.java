package com.closer.controller;

import com.closer.dto.ReminderRequest;
import com.closer.dto.ReminderResponse;
import com.closer.security.UserPrincipal;
import com.closer.service.ReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping
    public ResponseEntity<List<ReminderResponse>> getReminders(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(reminderService.getAllReminders(principal.getId()));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<ReminderResponse>> getUpcoming(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(reminderService.getUpcomingReminders(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<ReminderResponse> createReminder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReminderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reminderService.createReminder(principal.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReminderResponse> updateReminder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody ReminderRequest request) {
        return ResponseEntity.ok(reminderService.updateReminder(principal.getId(), id, request));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ReminderResponse> completeReminder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(reminderService.completeReminder(principal.getId(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReminder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        reminderService.deleteReminder(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
