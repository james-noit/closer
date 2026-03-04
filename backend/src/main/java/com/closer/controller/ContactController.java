package com.closer.controller;

import com.closer.dto.ContactRequest;
import com.closer.dto.ContactResponse;
import com.closer.dto.DashboardResponse;
import com.closer.security.UserPrincipal;
import com.closer.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @GetMapping
    public ResponseEntity<List<ContactResponse>> getContacts(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(contactService.getAllContacts(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<ContactResponse> addContact(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ContactRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contactService.addContact(principal.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactResponse> updateContact(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody ContactRequest request) {
        return ResponseEntity.ok(contactService.updateContact(principal.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        contactService.deleteContact(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(contactService.getContactDashboard(principal.getId()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ContactResponse>> search(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("q") String query) {
        return ResponseEntity.ok(contactService.searchContacts(principal.getId(), query));
    }

    @PostMapping("/sync")
    public ResponseEntity<List<ContactResponse>> syncContacts(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody List<ContactRequest> contacts) {
        return ResponseEntity.ok(contactService.importContacts(principal.getId(), contacts));
    }

    @PostMapping("/{id}/interact")
    public ResponseEntity<ContactResponse> recordInteraction(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(contactService.updateLastInteraction(principal.getId(), id));
    }
}
