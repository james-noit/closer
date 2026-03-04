package com.closer.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "contacts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contact {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_user_id")
    private User contactUser;

    @Column(nullable = false)
    private String name;

    private String email;

    private String phone;

    private LocalDateTime lastInteractionDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(length = 1000)
    private String notes;

    private LocalDate birthday;

    @Builder.Default
    private boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (lastInteractionDate == null) {
            lastInteractionDate = LocalDateTime.now();
        }
    }

    /**
     * Computes the relationship group (1-5) based on days since last interaction.
     * Group 1 (green):       <= 14 days
     * Group 2 (light-green): <= 30 days
     * Group 3 (yellow):      <= 90 days
     * Group 4 (orange):      <= 180 days
     * Group 5 (red):         >  180 days
     */
    @Transient
    public int getGroup() {
        if (lastInteractionDate == null) {
            return 5;
        }
        long daysSince = ChronoUnit.DAYS.between(lastInteractionDate, LocalDateTime.now());
        if (daysSince <= 14) return 1;
        if (daysSince <= 30) return 2;
        if (daysSince <= 90) return 3;
        if (daysSince <= 180) return 4;
        return 5;
    }
}
