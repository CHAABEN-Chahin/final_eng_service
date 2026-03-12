package com.nursery.service;

import com.nursery.model.*;
import com.nursery.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final ChildRepository childRepository;
    private final NurseryRepository nurseryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PaymentRepository paymentRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                              ChildRepository childRepository,
                              NurseryRepository nurseryRepository,
                              UserRepository userRepository,
                              NotificationService notificationService,
                              PaymentRepository paymentRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.childRepository = childRepository;
        this.nurseryRepository = nurseryRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.paymentRepository = paymentRepository;
    }

    public Map<String, Object> createEnrollment(Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();

        String childName = (String) body.get("childName");
        String birthDate = (String) body.get("birthDate");
        String parentId = (String) body.get("parentId");
        String nurseryId = (String) body.get("nurseryId");
        String startDate = (String) body.get("startDate");
        String notes = (String) body.get("notes");

        // Calculate age from birthDate
        int age = 0;
        if (birthDate != null && birthDate.length() >= 10) {
            try {
                java.time.LocalDate dob = java.time.LocalDate.parse(birthDate.substring(0, 10));
                age = java.time.Period.between(dob, java.time.LocalDate.now()).getYears();
            } catch (Exception ignored) {}
        }

        // Create child
        Child child = new Child();
        child.setName(childName);
        child.setDateOfBirth(birthDate);
        child.setAge(age);
        child.setParentId(parentId);
        child.setMedicalNotes(notes);
        Child savedChild = childRepository.save(child);

        // Create enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setChildId(savedChild.getId());
        enrollment.setNurseryId(nurseryId);
        enrollment.setParentId(parentId);
        enrollment.setStartDate(startDate);
        enrollment.setStatus("pending");
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // Create payment record
        nurseryRepository.findById(nurseryId).ifPresent(nursery -> {
            if (paymentRepository.findByEnrollmentId(savedEnrollment.getId()).isEmpty()) {
                Payment payment = new Payment();
                payment.setEnrollmentId(savedEnrollment.getId());
                payment.setParentId(parentId);
                payment.setNurseryId(nurseryId);
                payment.setChildId(savedChild.getId());
                payment.setAmount(nursery.getPricePerMonth());
                payment.setPaymentStatus("unpaid");
                payment.setDescription("Monthly fee for " + childName);
                paymentRepository.save(payment);
            }
        });

        Map<String, Object> enrollmentDto = new HashMap<>();
        enrollmentDto.put("id", savedEnrollment.getId());
        enrollmentDto.put("childId", savedEnrollment.getChildId());
        enrollmentDto.put("parentId", savedEnrollment.getParentId());
        enrollmentDto.put("nurseryId", savedEnrollment.getNurseryId());
        enrollmentDto.put("createdAt", savedEnrollment.getCreatedAt());

        result.put("success", true);
        result.put("enrollment", enrollmentDto);
        return result;
    }

    public Map<String, Object> getByNursery(String nurseryId) {
        List<Enrollment> enrollments = enrollmentRepository.findByNurseryId(nurseryId);
        List<Map<String, Object>> dtos = enrollments.stream().map(e -> buildEnrollmentDto(e, true, true, false))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("enrollments", dtos);
        return result;
    }

    public Map<String, Object> getByParent(String parentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByParentId(parentId);
        List<Map<String, Object>> dtos = enrollments.stream().map(e -> buildEnrollmentDto(e, true, true, true))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("count", dtos.size());
        result.put("enrollments", dtos);
        return result;
    }

    public Map<String, Object> getAll() {
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        List<Map<String, Object>> dtos = enrollments.stream().map(e -> buildEnrollmentDto(e, true, true, true))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("count", dtos.size());
        result.put("enrollments", dtos);
        return result;
    }

    public Map<String, Object> updateStatus(String enrollmentId, String status) {
        Map<String, Object> result = new HashMap<>();
        Optional<Enrollment> opt = enrollmentRepository.findById(enrollmentId);
        if (opt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Enrollment not found");
            return result;
        }
        Enrollment enrollment = opt.get();
        enrollment.setStatus(status);
        enrollment.setUpdatedAt(java.time.Instant.now());
        enrollmentRepository.save(enrollment);

        Map<String, Object> dto = new HashMap<>();
        dto.put("id", enrollment.getId());
        dto.put("status", enrollment.getStatus());
        dto.put("updatedAt", enrollment.getUpdatedAt());

        result.put("success", true);
        result.put("enrollment", dto);
        return result;
    }

    public Map<String, Object> accept(String enrollmentId) {
        Map<String, Object> result = new HashMap<>();
        Optional<Enrollment> opt = enrollmentRepository.findById(enrollmentId);
        if (opt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Enrollment not found");
            return result;
        }
        Enrollment enrollment = opt.get();
        enrollment.setStatus("active");
        enrollment.setUpdatedAt(java.time.Instant.now());
        enrollmentRepository.save(enrollment);

        // Update nursery available spots
        nurseryRepository.findById(enrollment.getNurseryId()).ifPresent(nursery -> {
            if (nursery.getAvailableSpots() != null && nursery.getAvailableSpots() > 0) {
                nursery.setAvailableSpots(nursery.getAvailableSpots() - 1);
                nurseryRepository.save(nursery);
            }
        });

        // Ensure payment record exists
        if (paymentRepository.findByEnrollmentId(enrollmentId).isEmpty()) {
            nurseryRepository.findById(enrollment.getNurseryId()).ifPresent(nursery -> {
                Payment payment = new Payment();
                payment.setEnrollmentId(enrollmentId);
                payment.setParentId(enrollment.getParentId());
                payment.setNurseryId(enrollment.getNurseryId());
                payment.setChildId(enrollment.getChildId());
                payment.setAmount(nursery.getPricePerMonth());
                payment.setPaymentStatus("unpaid");
                paymentRepository.save(payment);
            });
        }

        // Notify parent
        String nurseryName = nurseryRepository.findById(enrollment.getNurseryId())
                .map(Nursery::getName).orElse("the nursery");
        notificationService.createNotification(
                enrollment.getParentId(),
                "enrollment_accepted",
                "Enrollment Accepted",
                "Your enrollment request at " + nurseryName + " has been accepted!",
                enrollmentId
        );

        result.put("success", true);
        result.put("message", "Enrollment accepted");
        return result;
    }

    public Map<String, Object> reject(String enrollmentId) {
        Map<String, Object> result = new HashMap<>();
        Optional<Enrollment> opt = enrollmentRepository.findById(enrollmentId);
        if (opt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Enrollment not found");
            return result;
        }
        Enrollment enrollment = opt.get();
        enrollment.setStatus("cancelled");
        enrollment.setUpdatedAt(java.time.Instant.now());
        enrollmentRepository.save(enrollment);

        // Notify parent
        String nurseryName = nurseryRepository.findById(enrollment.getNurseryId())
                .map(Nursery::getName).orElse("the nursery");
        notificationService.createNotification(
                enrollment.getParentId(),
                "enrollment_rejected",
                "Enrollment Rejected",
                "Your enrollment request at " + nurseryName + " has been rejected.",
                enrollmentId
        );

        result.put("success", true);
        result.put("message", "Enrollment rejected");
        return result;
    }

    private Map<String, Object> buildEnrollmentDto(Enrollment e, boolean includeChild,
                                                     boolean includeParent, boolean includeNursery) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", e.getId());
        dto.put("enrollmentId", e.getId());
        dto.put("startDate", e.getStartDate());
        dto.put("status", e.getStatus());
        dto.put("createdAt", e.getCreatedAt());

        if (includeChild) {
            childRepository.findById(e.getChildId()).ifPresent(c -> {
                Map<String, Object> childDto = new HashMap<>();
                childDto.put("id", c.getId());
                childDto.put("childName", c.getName());
                childDto.put("name", c.getName());
                childDto.put("birthDate", c.getDateOfBirth());
                childDto.put("age", c.getAge());
                dto.put("child", childDto);
            });
        }

        if (includeParent) {
            userRepository.findById(e.getParentId()).ifPresent(u -> {
                Map<String, Object> parentDto = new HashMap<>();
                parentDto.put("id", u.getId());
                parentDto.put("name", u.getName());
                parentDto.put("phone", u.getPhone());
                dto.put("parent", parentDto);
            });
        }

        if (includeNursery) {
            nurseryRepository.findById(e.getNurseryId()).ifPresent(n -> {
                Map<String, Object> nurseryDto = new HashMap<>();
                nurseryDto.put("id", n.getId());
                nurseryDto.put("name", n.getName());
                nurseryDto.put("address", n.getAddress());
                nurseryDto.put("city", n.getCity());
                dto.put("nursery", nurseryDto);
            });
        }

        return dto;
    }
}
