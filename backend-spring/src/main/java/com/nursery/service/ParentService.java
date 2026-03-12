package com.nursery.service;

import com.nursery.model.*;
import com.nursery.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ParentService {

    private final ChildRepository childRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NurseryRepository nurseryRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final DailyScheduleRepository dailyScheduleRepository;

    public ParentService(ChildRepository childRepository,
                         EnrollmentRepository enrollmentRepository,
                         NurseryRepository nurseryRepository,
                         ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         DailyScheduleRepository dailyScheduleRepository) {
        this.childRepository = childRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.nurseryRepository = nurseryRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.dailyScheduleRepository = dailyScheduleRepository;
    }

    public Map<String, Object> getChildren(String parentId) {
        Map<String, Object> result = new HashMap<>();
        List<Child> children = childRepository.findByParentId(parentId);

        List<Map<String, Object>> childDtos = children.stream().map(child -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", child.getId());
            dto.put("name", child.getName());
            dto.put("age", child.getAge());
            dto.put("dateOfBirth", child.getDateOfBirth());
            dto.put("photoUrl", child.getPhotoUrl());
            dto.put("medicalNotes", child.getMedicalNotes());
            dto.put("createdAt", child.getCreatedAt());

            // Find nursery from active enrollment
            enrollmentRepository.findByChildId(child.getId()).stream()
                    .filter(e -> "active".equals(e.getStatus()))
                    .findFirst()
                    .ifPresent(enrollment -> {
                        nurseryRepository.findById(enrollment.getNurseryId()).ifPresent(nursery -> {
                            dto.put("nurseryId", nursery.getId());
                            dto.put("nurseryName", nursery.getName());
                        });
                    });

            return dto;
        }).collect(Collectors.toList());

        result.put("success", true);
        result.put("count", childDtos.size());
        result.put("children", childDtos);
        return result;
    }

    public Map<String, Object> getNurseries(String parentId) {
        Map<String, Object> result = new HashMap<>();

        List<Enrollment> enrollments = enrollmentRepository.findByParentId(parentId);
        Set<String> nurseryIds = enrollments.stream()
                .map(Enrollment::getNurseryId)
                .collect(Collectors.toSet());

        List<Map<String, Object>> nurseryDtos = nurseryIds.stream().map(nurseryId -> {
            Map<String, Object> dto = new HashMap<>();
            nurseryRepository.findById(nurseryId).ifPresent(nursery -> {
                dto.put("id", nursery.getId());
                dto.put("name", nursery.getName());
                dto.put("description", nursery.getDescription());
                dto.put("address", nursery.getAddress());
                dto.put("city", nursery.getCity());
                dto.put("phone", nursery.getPhone());
                dto.put("email", nursery.getEmail());
                dto.put("rating", nursery.getRating());
                dto.put("pricePerMonth", nursery.getPricePerMonth());
                dto.put("photoUrl", nursery.getPhotoUrl());

                long childCount = enrollments.stream()
                        .filter(e -> nurseryId.equals(e.getNurseryId()))
                        .count();
                dto.put("childCount", childCount);
            });
            return dto;
        }).filter(dto -> !dto.isEmpty()).collect(Collectors.toList());

        result.put("success", true);
        result.put("parentId", parentId);
        result.put("nurseries", nurseryDtos);
        return result;
    }

    public Map<String, Object> getTodayProgram(String parentId) {
        Map<String, Object> result = new HashMap<>();

        List<Enrollment> enrollments = enrollmentRepository.findByParentId(parentId);
        if (enrollments.isEmpty()) {
            result.put("success", true);
            result.put("program", Collections.emptyList());
            result.put("nurseryName", null);
            return result;
        }

        // Use the first active enrollment's nursery
        Optional<Enrollment> activeEnrollment = enrollments.stream()
                .filter(e -> "active".equals(e.getStatus()))
                .findFirst();

        if (activeEnrollment.isEmpty()) {
            result.put("success", true);
            result.put("program", Collections.emptyList());
            result.put("nurseryName", null);
            return result;
        }

        String nurseryId = activeEnrollment.get().getNurseryId();
        List<DailySchedule> schedules = dailyScheduleRepository.findByNurseryIdOrderByTimeSlotAsc(nurseryId);

        List<Map<String, Object>> program = schedules.stream().map(s -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", s.getId());
            dto.put("timeSlot", s.getTimeSlot());
            dto.put("activityName", s.getActivityName());
            dto.put("description", s.getDescription());
            dto.put("participantCount", s.getParticipantCount());
            return dto;
        }).collect(Collectors.toList());

        String nurseryName = nurseryRepository.findById(nurseryId)
                .map(Nursery::getName)
                .orElse(null);

        result.put("success", true);
        result.put("program", program);
        result.put("nurseryName", nurseryName);
        return result;
    }

    public Map<String, Object> getNurseryReviews(String parentId) {
        Map<String, Object> result = new HashMap<>();

        List<Review> reviews = reviewRepository.findByParentId(parentId);

        List<Map<String, Object>> reviewDtos = reviews.stream().map(r -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", r.getId());
            dto.put("rating", r.getRating());
            dto.put("comment", r.getComment());
            dto.put("created_at", r.getCreatedAt());

            userRepository.findById(parentId).ifPresent(u -> dto.put("parent_name", u.getName()));
            return dto;
        }).collect(Collectors.toList());

        result.put("success", true);
        result.put("reviews", reviewDtos);
        return result;
    }
}
