package com.closer.service;

import com.closer.dto.ContactRequest;
import com.closer.dto.ContactResponse;
import com.closer.dto.DashboardResponse;
import com.closer.exception.ContactLimitExceededException;
import com.closer.exception.ResourceNotFoundException;
import com.closer.model.Contact;
import com.closer.model.User;
import com.closer.repository.ContactRepository;
import com.closer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    @Value("${app.contact.inactive-months:6}")
    private int inactiveMonths;
    public ContactResponse addContact(UUID userId, ContactRequest request) {
        User owner = getUser(userId);
        checkContactLimit(owner);

        Contact contact = Contact.builder()
                .owner(owner)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .notes(request.getNotes())
                .birthday(request.getBirthday())
                .lastInteractionDate(LocalDateTime.now())
                .isActive(true)
                .build();

        return toResponse(contactRepository.save(contact));
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> getAllContacts(UUID userId) {
        User owner = getUser(userId);
        return contactRepository.findByOwnerAndIsActiveTrue(owner).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> getContactsByGroup(UUID userId, int group) {
        User owner = getUser(userId);
        return contactRepository.findByOwnerAndIsActiveTrue(owner).stream()
                .filter(c -> c.getGroup() == group)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DashboardResponse getContactDashboard(UUID userId) {
        User owner = getUser(userId);
        List<Contact> contacts = contactRepository.findByOwnerAndIsActiveTrue(owner);
        int total = contacts.size();

        Map<Integer, List<Contact>> byGroup = new LinkedHashMap<>();
        for (int g = 1; g <= 5; g++) {
            byGroup.put(g, new ArrayList<>());
        }
        for (Contact c : contacts) {
            byGroup.get(c.getGroup()).add(c);
        }

        List<DashboardResponse.GroupSummary> groups = new ArrayList<>();
        for (int g = 1; g <= 5; g++) {
            List<Contact> groupContacts = byGroup.get(g);
            int count = groupContacts.size();
            double percentage = total > 0 ? (count * 100.0 / total) : 0.0;

            List<ContactResponse> contactResponses = groupContacts.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            groups.add(DashboardResponse.GroupSummary.builder()
                    .groupNumber(g)
                    .color(ContactResponse.colorForGroup(g))
                    .label(DashboardResponse.GroupSummary.labelForGroup(g))
                    .contactCount(count)
                    .percentage(Math.round(percentage * 10.0) / 10.0)
                    .contacts(contactResponses)
                    .build());
        }

        double utilization = owner.getMaxContacts() > 0
                ? (total * 100.0 / owner.getMaxContacts()) : 0.0;

        return DashboardResponse.builder()
                .totalContacts(total)
                .maxContacts(owner.getMaxContacts())
                .utilizationPercentage(Math.round(utilization * 10.0) / 10.0)
                .groups(groups)
                .build();
    }

    @Transactional
    public ContactResponse updateContact(UUID userId, UUID contactId, ContactRequest request) {
        User owner = getUser(userId);
        Contact contact = getContactForOwner(contactId, owner);

        contact.setName(request.getName());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setNotes(request.getNotes());
        contact.setBirthday(request.getBirthday());

        return toResponse(contactRepository.save(contact));
    }

    @Transactional
    public void deleteContact(UUID userId, UUID contactId) {
        User owner = getUser(userId);
        Contact contact = getContactForOwner(contactId, owner);
        contact.setActive(false);
        contactRepository.save(contact);
    }

    @Transactional
    public ContactResponse updateLastInteraction(UUID userId, UUID contactId) {
        User owner = getUser(userId);
        Contact contact = getContactForOwner(contactId, owner);
        contact.setLastInteractionDate(LocalDateTime.now());
        return toResponse(contactRepository.save(contact));
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> searchContacts(UUID userId, String query) {
        User owner = getUser(userId);
        return contactRepository.searchByOwnerAndQuery(owner, query).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ContactResponse> importContacts(UUID userId, List<ContactRequest> contacts) {
        User owner = getUser(userId);
        List<ContactResponse> imported = new ArrayList<>();
        for (ContactRequest request : contacts) {
            long currentCount = contactRepository.countByOwnerAndIsActiveTrue(owner);
            if (currentCount >= owner.getMaxContacts()) {
                log.warn("Contact limit reached for user {}. Skipping remaining imports.", userId);
                break;
            }
            Contact contact = Contact.builder()
                    .owner(owner)
                    .name(request.getName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .notes(request.getNotes())
                    .birthday(request.getBirthday())
                    .lastInteractionDate(LocalDateTime.now())
                    .isActive(true)
                    .build();
            imported.add(toResponse(contactRepository.save(contact)));
        }
        return imported;
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void removeInactiveContacts() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(inactiveMonths);
        List<Contact> inactive = contactRepository.findInactiveContactsBefore(cutoff);
        log.info("Removing {} inactive contacts (no interaction since {})", inactive.size(), cutoff);
        for (Contact contact : inactive) {
            contact.setActive(false);
            contactRepository.save(contact);
        }
    }

    private void checkContactLimit(User owner) {
        long count = contactRepository.countByOwnerAndIsActiveTrue(owner);
        if (count >= owner.getMaxContacts()) {
            throw new ContactLimitExceededException(owner.getMaxContacts());
        }
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private Contact getContactForOwner(UUID contactId, User owner) {
        return contactRepository.findByIdAndOwner(contactId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", "id", contactId));
    }

    public ContactResponse toResponse(Contact contact) {
        int group = contact.getGroup();
        return ContactResponse.builder()
                .id(contact.getId())
                .name(contact.getName())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .lastInteractionDate(contact.getLastInteractionDate())
                .createdAt(contact.getCreatedAt())
                .notes(contact.getNotes())
                .birthday(contact.getBirthday())
                .isActive(contact.isActive())
                .group(group)
                .color(ContactResponse.colorForGroup(group))
                .build();
    }
}
