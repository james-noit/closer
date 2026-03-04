package com.closer.controller;

import com.closer.model.Invitation;
import com.closer.security.UserPrincipal;
import com.closer.service.InvitationService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping("/email")
    public ResponseEntity<Map<String, String>> inviteByEmail(
            @AuthenticationPrincipal UserPrincipal principal,
            @jakarta.validation.Valid @RequestBody InviteRequest request) {
        invitationService.inviteByEmail(principal.getId(), request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Invitation sent to " + request.getEmail()));
    }

    @GetMapping("/accept/{token}")
    public ResponseEntity<Map<String, String>> acceptInvitation(@PathVariable String token) {
        Invitation invitation = invitationService.acceptInvitation(token);
        return ResponseEntity.ok(Map.of(
                "message", "Invitation accepted",
                "email", invitation.getEmail()
        ));
    }

    @Data
    public static class InviteRequest {
        @NotBlank
        @Email(message = "Valid email is required")
        private String email;
    }
}
