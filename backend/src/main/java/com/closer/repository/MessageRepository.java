package com.closer.repository;

import com.closer.model.Message;
import com.closer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("SELECT m FROM Message m WHERE " +
           "((m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1)) " +
           "AND m.isDeleted = false ORDER BY m.sentAt ASC")
    List<Message> findConversation(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT m FROM Message m WHERE m.receiver = :receiver AND m.isRead = false AND m.isDeleted = false")
    List<Message> findUnreadByReceiver(@Param("receiver") User receiver);

    long countByReceiverAndIsReadFalseAndIsDeletedFalse(User receiver);
}
