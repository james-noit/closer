package com.closer.repository;

import com.closer.model.Reminder;
import com.closer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, UUID> {

    List<Reminder> findByUserAndIsCompletedFalseOrderByReminderDateAsc(User user);

    List<Reminder> findByUserAndReminderDateBetweenOrderByReminderDateAsc(
            User user, LocalDateTime start, LocalDateTime end);

    @Query("SELECT r FROM Reminder r WHERE r.user = :user AND r.isCompleted = false " +
           "AND r.reminderDate BETWEEN :now AND :future ORDER BY r.reminderDate ASC")
    List<Reminder> findUpcomingByUser(
            @Param("user") User user,
            @Param("now") LocalDateTime now,
            @Param("future") LocalDateTime future);

    @Query("SELECT r FROM Reminder r WHERE r.isCompleted = false " +
           "AND r.reminderDate BETWEEN :start AND :end")
    List<Reminder> findDueReminders(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    Optional<Reminder> findByIdAndUser(UUID id, User user);
}
