package com.closer.security;

import com.closer.model.User;
import com.closer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        return processOAuth2User(registrationId, oAuth2User);
    }

    private OAuth2User processOAuth2User(String registrationId, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email;
        String name;
        String providerId;
        String picture;
        User.AuthProvider provider;

        if ("google".equalsIgnoreCase(registrationId)) {
            provider = User.AuthProvider.GOOGLE;
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            providerId = String.valueOf(attributes.get("sub"));
            picture = (String) attributes.get("picture");
        } else if ("github".equalsIgnoreCase(registrationId)) {
            provider = User.AuthProvider.GITHUB;
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            if (name == null) name = (String) attributes.get("login");
            providerId = String.valueOf(attributes.get("id"));
            picture = (String) attributes.get("avatar_url");
        } else {
            throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
        }

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Optional<User> existingUser = userRepository.findByProviderAndProviderId(provider, providerId);
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.setName(name);
            user.setProfilePicture(picture);
            user = userRepository.save(user);
        } else {
            Optional<User> userByEmail = userRepository.findByEmail(email);
            if (userByEmail.isPresent()) {
                user = userByEmail.get();
                user.setProvider(provider);
                user.setProviderId(providerId);
                user.setProfilePicture(picture);
                user = userRepository.save(user);
            } else {
                user = User.builder()
                        .email(email)
                        .name(name)
                        .provider(provider)
                        .providerId(providerId)
                        .profilePicture(picture)
                        .build();
                user = userRepository.save(user);
            }
        }

        return new OAuth2UserPrincipal(UserPrincipal.create(user), attributes);
    }
}
