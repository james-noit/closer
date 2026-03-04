package com.closer.service;

import com.closer.dto.ContactResponse;
import com.closer.dto.DashboardResponse;
import com.closer.model.Contact;
import com.closer.model.User;
import com.closer.repository.ContactRepository;
import com.closer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContactService contactService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .name("Test User")
                .maxContacts(150)
                .build();
    }

    @Test
    void contactGroup_withinTwoWeeks_returnsGroup1() {
        Contact contact = buildContact(LocalDateTime.now().minusDays(7));
        assertThat(contact.getGroup()).isEqualTo(1);
    }

    @Test
    void contactGroup_withinOneMonth_returnsGroup2() {
        Contact contact = buildContact(LocalDateTime.now().minusDays(20));
        assertThat(contact.getGroup()).isEqualTo(2);
    }

    @Test
    void contactGroup_withinThreeMonths_returnsGroup3() {
        Contact contact = buildContact(LocalDateTime.now().minusDays(60));
        assertThat(contact.getGroup()).isEqualTo(3);
    }

    @Test
    void contactGroup_withinSixMonths_returnsGroup4() {
        Contact contact = buildContact(LocalDateTime.now().minusDays(120));
        assertThat(contact.getGroup()).isEqualTo(4);
    }

    @Test
    void contactGroup_overSixMonths_returnsGroup5() {
        Contact contact = buildContact(LocalDateTime.now().minusDays(200));
        assertThat(contact.getGroup()).isEqualTo(5);
    }

    @Test
    void contactGroup_nullLastInteraction_returnsGroup5() {
        Contact contact = buildContact(null);
        assertThat(contact.getGroup()).isEqualTo(5);
    }

    @Test
    void contactColor_group1_returnsGreen() {
        assertThat(ContactResponse.colorForGroup(1)).isEqualTo("green");
    }

    @Test
    void contactColor_group2_returnsLightGreen() {
        assertThat(ContactResponse.colorForGroup(2)).isEqualTo("light-green");
    }

    @Test
    void contactColor_group3_returnsYellow() {
        assertThat(ContactResponse.colorForGroup(3)).isEqualTo("yellow");
    }

    @Test
    void contactColor_group4_returnsOrange() {
        assertThat(ContactResponse.colorForGroup(4)).isEqualTo("orange");
    }

    @Test
    void contactColor_group5_returnsRed() {
        assertThat(ContactResponse.colorForGroup(5)).isEqualTo("red");
    }

    @Test
    void getDashboard_returnsCorrectGroupCounts() {
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        List<Contact> contacts = List.of(
                buildContact(LocalDateTime.now().minusDays(5)),   // group 1
                buildContact(LocalDateTime.now().minusDays(5)),   // group 1
                buildContact(LocalDateTime.now().minusDays(20)),  // group 2
                buildContact(LocalDateTime.now().minusDays(60)),  // group 3
                buildContact(LocalDateTime.now().minusDays(200))  // group 5
        );
        when(contactRepository.findByOwnerAndIsActiveTrue(testUser)).thenReturn(contacts);

        DashboardResponse dashboard = contactService.getContactDashboard(userId);

        assertThat(dashboard.getTotalContacts()).isEqualTo(5);
        assertThat(dashboard.getMaxContacts()).isEqualTo(150);
        assertThat(dashboard.getGroups()).hasSize(5);

        DashboardResponse.GroupSummary group1 = dashboard.getGroups().get(0);
        assertThat(group1.getGroupNumber()).isEqualTo(1);
        assertThat(group1.getColor()).isEqualTo("green");
        assertThat(group1.getContactCount()).isEqualTo(2);
        assertThat(group1.getPercentage()).isEqualTo(40.0);

        DashboardResponse.GroupSummary group2 = dashboard.getGroups().get(1);
        assertThat(group2.getGroupNumber()).isEqualTo(2);
        assertThat(group2.getContactCount()).isEqualTo(1);

        DashboardResponse.GroupSummary group5 = dashboard.getGroups().get(4);
        assertThat(group5.getGroupNumber()).isEqualTo(5);
        assertThat(group5.getColor()).isEqualTo("red");
        assertThat(group5.getContactCount()).isEqualTo(1);
    }

    @Test
    void getDashboard_emptyContacts_returnsZeroPercentages() {
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(contactRepository.findByOwnerAndIsActiveTrue(testUser)).thenReturn(List.of());

        DashboardResponse dashboard = contactService.getContactDashboard(userId);

        assertThat(dashboard.getTotalContacts()).isEqualTo(0);
        assertThat(dashboard.getUtilizationPercentage()).isEqualTo(0.0);
        dashboard.getGroups().forEach(g -> {
            assertThat(g.getContactCount()).isEqualTo(0);
            assertThat(g.getPercentage()).isEqualTo(0.0);
        });
    }

    @Test
    void getDashboard_utilizationPercentage_isCorrect() {
        testUser = User.builder()
                .id(testUser.getId())
                .email(testUser.getEmail())
                .name(testUser.getName())
                .maxContacts(10)
                .build();

        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        List<Contact> contacts = List.of(
                buildContact(LocalDateTime.now().minusDays(1)),
                buildContact(LocalDateTime.now().minusDays(2)),
                buildContact(LocalDateTime.now().minusDays(3))
        );
        when(contactRepository.findByOwnerAndIsActiveTrue(testUser)).thenReturn(contacts);

        DashboardResponse dashboard = contactService.getContactDashboard(userId);
        assertThat(dashboard.getUtilizationPercentage()).isEqualTo(30.0);
    }

    private Contact buildContact(LocalDateTime lastInteraction) {
        return Contact.builder()
                .id(UUID.randomUUID())
                .owner(testUser)
                .name("Test Contact")
                .lastInteractionDate(lastInteraction)
                .isActive(true)
                .build();
    }
}
