package com.closer.service;

import com.closer.dto.MessageRequest;
import com.closer.dto.MessageResponse;
import com.closer.exception.ResourceNotFoundException;
import com.closer.exception.UnauthorizedException;
import com.closer.model.Message;
import com.closer.model.User;
import com.closer.repository.MessageRepository;
import com.closer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public MessageResponse sendMessage(UUID senderId, MessageRequest request) {
        User sender = getUser(senderId);
        User receiver = getUser(request.getReceiverId());

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(request.getContent())
                .isRead(false)
                .isDeleted(false)
                .build();

        return toResponse(messageRepository.save(message));
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getConversation(UUID userId, UUID contactId) {
        User user = getUser(userId);
        User contact = getUser(contactId);
        return messageRepository.findConversation(user, contact).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageResponse markAsRead(UUID messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", messageId));

        if (!message.getReceiver().getId().equals(userId)) {
            throw new UnauthorizedException("You can only mark your own messages as read");
        }

        message.setRead(true);
        return toResponse(messageRepository.save(message));
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        User user = getUser(userId);
        return messageRepository.countByReceiverAndIsReadFalseAndIsDeletedFalse(user);
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private MessageResponse toResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getName())
                .receiverId(message.getReceiver().getId())
                .receiverName(message.getReceiver().getName())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .isRead(message.isRead())
                .build();
    }
}
