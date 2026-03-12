package com.nursery.service;

import com.nursery.model.Notification;
import com.nursery.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void createNotification(String userId, String type, String title, String message, String relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedId(relatedId);
        notificationRepository.save(notification);
    }

    public Map<String, Object> getNotifications(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderBySentAtDesc(userId);
        List<Map<String, Object>> dtos = notifications.stream().map(this::buildDto).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("notifications", dtos);
        return result;
    }

    public Map<String, Object> getUnreadCount(String userId) {
        long count = notificationRepository.countByUserIdAndIsRead(userId, false);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("unreadCount", count);
        return result;
    }

    public Map<String, Object> markRead(String notificationId) {
        Map<String, Object> result = new HashMap<>();
        Optional<Notification> opt = notificationRepository.findById(notificationId);
        if (opt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Notification not found");
            return result;
        }
        Notification n = opt.get();
        n.setIsRead(true);
        notificationRepository.save(n);
        result.put("success", true);
        result.put("message", "Notification marked as read");
        return result;
    }

    public Map<String, Object> markAllRead(String userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsRead(userId, false);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "All notifications marked as read");
        result.put("count", unread.size());
        return result;
    }

    public Map<String, Object> deleteNotification(String notificationId) {
        Map<String, Object> result = new HashMap<>();
        if (!notificationRepository.existsById(notificationId)) {
            result.put("success", false);
            result.put("message", "Notification not found");
            return result;
        }
        notificationRepository.deleteById(notificationId);
        result.put("success", true);
        result.put("message", "Notification deleted");
        return result;
    }

    private Map<String, Object> buildDto(Notification n) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", n.getId());
        dto.put("type", n.getType());
        dto.put("title", n.getTitle());
        dto.put("message", n.getMessage());
        dto.put("isRead", n.getIsRead());
        dto.put("relatedId", n.getRelatedId());
        dto.put("sentAt", n.getSentAt());
        return dto;
    }
}
