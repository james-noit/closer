package com.closer.service;

import com.closer.exception.ResourceNotFoundException;
import com.closer.model.Invitation;
import com.closer.model.User;
import com.closer.repository.InvitationRepository;
import com.closer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Transactional
    public Invitation inviteByEmail(UUID userId, String email) {
        User invitedBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // expire any existing pending invitations for same email
        invitationRepository.findByEmailAndStatus(email, Invitation.InvitationStatus.PENDING)
                .forEach(inv -> {
                    inv.setStatus(Invitation.InvitationStatus.EXPIRED);
                    invitationRepository.save(inv);
                });

        String token = UUID.randomUUID().toString();
        Invitation invitation = Invitation.builder()
                .invitedBy(invitedBy)
                .email(email)
                .token(token)
                .status(Invitation.InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        invitation = invitationRepository.save(invitation);
        sendInvitationEmail(invitation, invitedBy);
        return invitation;
    }

    @Transactional
    public Invitation acceptInvitation(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", "token", token));

        if (invitation.getStatus() != Invitation.InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Invitation is no longer valid");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(Invitation.InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new IllegalArgumentException("Invitation has expired");
        }

        invitation.setStatus(Invitation.InvitationStatus.ACCEPTED);
        return invitationRepository.save(invitation);
    }

    private void sendInvitationEmail(Invitation invitation, User invitedBy) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(invitation.getEmail());
            message.setSubject(invitedBy.getName() + " invited you to join Closer");
            message.setText(String.format(
                    "Hi!%n%n%s has invited you to join Closer, a social network focused on " +
                    "maintaining quality relationships.%n%nAccept your invitation here:%n%s/accept/%s%n%n" +
                    "This invitation expires in 7 days.",
                    invitedBy.getName(), frontendUrl, invitation.getToken()
            ));
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send invitation email to {}", invitation.getEmail(), ex);
        }
    }
}
