package com.closer.controller;

import com.closer.dto.ContactRequest;
import com.closer.dto.ContactResponse;
import com.closer.dto.UserResponse;
import com.closer.security.UserPrincipal;
import com.closer.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.getUserProfile(principal.getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(
                userService.updateProfile(principal.getId(), request.getName(), request.getProfilePicture()));
    }

    @PostMapping("/me/sync-contacts")
    public ResponseEntity<List<ContactResponse>> syncContacts(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody List<ContactRequest> contacts) {
        return ResponseEntity.ok(userService.syncContacts(principal.getId(), contacts));
    }

    @Data
    public static class UpdateProfileRequest {
        private String name;
        private String profilePicture;
    }
}
