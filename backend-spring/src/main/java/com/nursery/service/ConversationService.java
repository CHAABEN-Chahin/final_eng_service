package com.nursery.service;

import com.nursery.model.*;
import com.nursery.repository.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final NurseryRepository nurseryRepository;
    private final NotificationService notificationService;

    public ConversationService(ConversationRepository conversationRepository,
                                MessageRepository messageRepository,
                                UserRepository userRepository,
                                NurseryRepository nurseryRepository,
                                NotificationService notificationService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.nurseryRepository = nurseryRepository;
        this.notificationService = notificationService;
    }

    public Map<String, Object> getOrCreate(String parentId, String nurseryId) {
        Conversation conversation = conversationRepository
                .findByParentIdAndNurseryId(parentId, nurseryId)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setParentId(parentId);
                    c.setNurseryId(nurseryId);
                    return conversationRepository.save(c);
                });

        Map<String, Object> dto = buildConversationDto(conversation, parentId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("conversation", dto);
        return result;
    }

    public Map<String, Object> getUserConversations(String userId) {
        // Could be parent or nursery owner
        List<Conversation> parentConvs = conversationRepository.findByParentId(userId);
        List<Conversation> nurseryConvs = new ArrayList<>();

        // Find nurseries owned by user
        nurseryRepository.findByOwnerId(userId).forEach(n ->
                nurseryConvs.addAll(conversationRepository.findByNurseryId(n.getId())));

        Set<String> seen = new HashSet<>();
        List<Conversation> all = new ArrayList<>();
        for (Conversation c : parentConvs) {
            if (seen.add(c.getId())) all.add(c);
        }
        for (Conversation c : nurseryConvs) {
            if (seen.add(c.getId())) all.add(c);
        }

        List<Map<String, Object>> dtos = all.stream()
                .map(c -> buildConversationDtoWithUnread(c, userId))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("conversations", dtos);
        return result;
    }

    public Map<String, Object> getMessages(String conversationId) {
        List<Message> messages = messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);
        List<Map<String, Object>> dtos = messages.stream().map(m -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", m.getId());
            dto.put("senderId", m.getSenderId());
            dto.put("recipientId", m.getRecipientId());
            dto.put("content", m.getContent());
            dto.put("isRead", m.getIsRead());
            dto.put("sentAt", m.getSentAt());
            userRepository.findById(m.getSenderId()).ifPresent(u -> dto.put("senderName", u.getName()));
            return dto;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("conversationId", conversationId);
        result.put("messages", dtos);
        
        // Include conversation details so frontend knows the participants
        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            result.put("parentId", conversation.getParentId());
            result.put("nurseryId", conversation.getNurseryId());
            // Get owner ID from nursery
            nurseryRepository.findById(conversation.getNurseryId()).ifPresent(n -> {
                result.put("ownerId", n.getOwnerId());
            });
        });
        
        return result;
    }

    public Map<String, Object> sendMessage(String conversationId, String senderId,
                                            String recipientId, String content) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setRecipientId(recipientId);
        message.setContent(content);
        Message saved = messageRepository.save(message);

        // Update conversation lastMessageAt
        conversationRepository.findById(conversationId).ifPresent(c -> {
            c.setLastMessageAt(Instant.now());
            conversationRepository.save(c);
        });

        // Notify recipient
        String senderName = userRepository.findById(senderId).map(User::getName).orElse("Someone");
        notificationService.createNotification(
                recipientId,
                "new_message",
                "New Message",
                senderName + " sent you a message",
                conversationId
        );

        Map<String, Object> dto = new HashMap<>();
        dto.put("id", saved.getId());
        dto.put("senderId", saved.getSenderId());
        dto.put("recipientId", saved.getRecipientId());
        dto.put("content", saved.getContent());
        dto.put("isRead", saved.getIsRead());
        dto.put("sentAt", saved.getSentAt());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", dto);
        return result;
    }

    public Map<String, Object> markRead(String conversationId, String userId) {
        List<Message> unread = messageRepository
                .findByConversationIdAndRecipientIdAndIsRead(conversationId, userId, false);
        unread.forEach(m -> m.setIsRead(true));
        messageRepository.saveAll(unread);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Messages marked as read");
        return result;
    }

    private Map<String, Object> buildConversationDto(Conversation c, String userId) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", c.getId());
        dto.put("parentId", c.getParentId());
        dto.put("nurseryId", c.getNurseryId());
        userRepository.findById(c.getParentId()).ifPresent(u -> dto.put("parentName", u.getName()));
        nurseryRepository.findById(c.getNurseryId()).ifPresent(n -> dto.put("nurseryName", n.getName()));
        return dto;
    }

    private Map<String, Object> buildConversationDtoWithUnread(Conversation c, String userId) {
        Map<String, Object> dto = buildConversationDto(c, userId);
        dto.put("lastMessageAt", c.getLastMessageAt());

        // Last message
        List<Message> messages = messageRepository.findByConversationIdOrderBySentAtAsc(c.getId());
        if (!messages.isEmpty()) {
            Message last = messages.get(messages.size() - 1);
            dto.put("lastMessage", last.getContent());
        }

        long unreadCount = messageRepository.countByConversationIdAndRecipientIdAndIsRead(c.getId(), userId, false);
        dto.put("unreadCount", unreadCount);

        // Owner info from nursery
        nurseryRepository.findById(c.getNurseryId()).ifPresent(n -> {
            dto.put("ownerId", n.getOwnerId());
            userRepository.findById(n.getOwnerId()).ifPresent(u -> dto.put("ownerName", u.getName()));
        });

        return dto;
    }
}
