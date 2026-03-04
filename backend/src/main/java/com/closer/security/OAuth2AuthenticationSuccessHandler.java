package com.closer.security;

import com.closer.dto.AuthResponse;
import com.closer.dto.UserResponse;
import com.closer.model.User;
import com.closer.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2UserPrincipal oAuth2User = (OAuth2UserPrincipal) authentication.getPrincipal();
        UserPrincipal principal = oAuth2User.getUserPrincipal();

        String token = jwtTokenProvider.generateToken(principal.getId(), principal.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(principal.getId(), principal.getEmail());

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .profilePicture(user.getProfilePicture())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .maxContacts(user.getMaxContacts())
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(userResponse)
                .build();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(authResponse));
    }
}
