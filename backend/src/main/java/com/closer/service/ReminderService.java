package com.closer.service;

import com.closer.dto.ReminderRequest;
import com.closer.dto.ReminderResponse;
import com.closer.exception.ResourceNotFoundException;
import com.closer.model.Contact;
import com.closer.model.Reminder;
import com.closer.model.User;
import com.closer.repository.ContactRepository;
import com.closer.repository.ReminderRepository;
import com.closer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final JavaMailSender mailSender;

    @Transactional
    public ReminderResponse createReminder(UUID userId, ReminderRequest request) {
        User user = getUser(userId);
        Contact contact = null;

        if (request.getContactId() != null) {
            contact = contactRepository.findByIdAndOwner(request.getContactId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Contact", "id", request.getContactId()));
        }

        Reminder reminder = Reminder.builder()
                .user(user)
                .contact(contact)
                .title(request.getTitle())
                .description(request.getDescription())
                .reminderDate(request.getReminderDate())
                .type(request.getType())
                .isCompleted(false)
                .build();

        return toResponse(reminderRepository.save(reminder));
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> getUpcomingReminders(UUID userId) {
        User user = getUser(userId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(30);
        return reminderRepository.findUpcomingByUser(user, now, future).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> getAllReminders(UUID userId) {
        User user = getUser(userId);
        return reminderRepository.findByUserAndIsCompletedFalseOrderByReminderDateAsc(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReminderResponse updateReminder(UUID userId, UUID reminderId, ReminderRequest request) {
        User user = getUser(userId);
        Reminder reminder = reminderRepository.findByIdAndUser(reminderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder", "id", reminderId));

        reminder.setTitle(request.getTitle());
        reminder.setDescription(request.getDescription());
        reminder.setReminderDate(request.getReminderDate());
        reminder.setType(request.getType());

        if (request.getContactId() != null) {
            Contact contact = contactRepository.findByIdAndOwner(request.getContactId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Contact", "id", request.getContactId()));
            reminder.setContact(contact);
        }

        return toResponse(reminderRepository.save(reminder));
    }

    @Transactional
    public ReminderResponse completeReminder(UUID userId, UUID reminderId) {
        User user = getUser(userId);
        Reminder reminder = reminderRepository.findByIdAndUser(reminderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder", "id", reminderId));
        reminder.setCompleted(true);
        return toResponse(reminderRepository.save(reminder));
    }

    @Transactional
    public void deleteReminder(UUID userId, UUID reminderId) {
        User user = getUser(userId);
        Reminder reminder = reminderRepository.findByIdAndUser(reminderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder", "id", reminderId));
        reminderRepository.delete(reminder);
    }

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void sendReminderNotifications() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(24);
        List<Reminder> dueReminders = reminderRepository.findDueReminders(start, end);

        log.info("Sending {} reminder notifications", dueReminders.size());
        for (Reminder reminder : dueReminders) {
            try {
                sendReminderEmail(reminder);
            } catch (Exception ex) {
                log.error("Failed to send reminder email for reminder {}", reminder.getId(), ex);
            }
        }
    }

    private void sendReminderEmail(Reminder reminder) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(reminder.getUser().getEmail());
        message.setSubject("Closer Reminder: " + reminder.getTitle());
        String body = String.format(
                "Hi %s,%n%nThis is a reminder: %s%n%n%s%n%nDate: %s",
                reminder.getUser().getName(),
                reminder.getTitle(),
                reminder.getDescription() != null ? reminder.getDescription() : "",
                reminder.getReminderDate()
        );
        message.setText(body);
        mailSender.send(message);
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private ReminderResponse toResponse(Reminder reminder) {
        return ReminderResponse.builder()
                .id(reminder.getId())
                .title(reminder.getTitle())
                .description(reminder.getDescription())
                .reminderDate(reminder.getReminderDate())
                .isCompleted(reminder.isCompleted())
                .type(reminder.getType())
                .contactId(reminder.getContact() != null ? reminder.getContact().getId() : null)
                .contactName(reminder.getContact() != null ? reminder.getContact().getName() : null)
                .createdAt(reminder.getCreatedAt())
                .build();
    }
}
