package com.closer.service;

import com.closer.dto.ContactRequest;
import com.closer.dto.UserResponse;
import com.closer.exception.ResourceNotFoundException;
import com.closer.model.User;
import com.closer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ContactService contactService;

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(UUID userId) {
        User user = getUser(userId);
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, String name, String profilePicture) {
        User user = getUser(userId);
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        if (profilePicture != null) {
            user.setProfilePicture(profilePicture);
        }
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public List<com.closer.dto.ContactResponse> syncContacts(UUID userId, List<ContactRequest> contacts) {
        return contactService.importContacts(userId, contacts);
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .profilePicture(user.getProfilePicture())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .maxContacts(user.getMaxContacts())
                .build();
    }
}
