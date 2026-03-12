package com.nursery.service;

import com.nursery.model.*;
import com.nursery.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.logging.Logger;

@Service
public class NurseryService {

    private static final Logger log = Logger.getLogger(NurseryService.class.getName());

    private final NurseryRepository nurseryRepository;
    private final ReviewRepository reviewRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ChildRepository childRepository;
    private final UserRepository userRepository;
    private final DailyScheduleRepository dailyScheduleRepository;
    private final PaymentRepository paymentRepository;

    public NurseryService(NurseryRepository nurseryRepository,
                          ReviewRepository reviewRepository,
                          EnrollmentRepository enrollmentRepository,
                          ChildRepository childRepository,
                          UserRepository userRepository,
                          DailyScheduleRepository dailyScheduleRepository,
                          PaymentRepository paymentRepository) {
        this.nurseryRepository = nurseryRepository;
        this.reviewRepository = reviewRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.childRepository = childRepository;
        this.userRepository = userRepository;
        this.dailyScheduleRepository = dailyScheduleRepository;
        this.paymentRepository = paymentRepository;
    }

    public Map<String, Object> createNursery(Map<String, Object> body) {
        Nursery nursery = new Nursery();
        nursery.setOwnerId((String) body.get("owner_id"));
        nursery.setName((String) body.get("name"));
        nursery.setDescription((String) body.get("description"));
        nursery.setAddress((String) body.get("address"));
        nursery.setCity((String) body.get("city"));
        nursery.setPostalCode((String) body.get("postal_code"));
        nursery.setPhone((String) body.get("phone"));
        nursery.setEmail((String) body.get("email"));
        nursery.setHours((String) body.get("hours"));
        nursery.setAgeRange((String) body.get("age_range"));
        nursery.setPhotoUrl((String) body.get("photo_url"));

        if (body.get("latitude") != null) nursery.setLatitude(toDouble(body.get("latitude")));
        if (body.get("longitude") != null) nursery.setLongitude(toDouble(body.get("longitude")));
        if (body.get("price_per_month") != null) nursery.setPricePerMonth(toDouble(body.get("price_per_month")));
        if (body.get("total_spots") != null) nursery.setTotalSpots(toInt(body.get("total_spots")));
        if (body.get("total_spots") != null) nursery.setAvailableSpots(toInt(body.get("total_spots")));

        Object fac = body.get("facilities");
        if (fac instanceof List) nursery.setFacilities((List<String>) fac);
        Object act = body.get("activities");
        if (act instanceof List) nursery.setActivities((List<String>) act);

        Nursery saved = nurseryRepository.save(nursery);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("nursery", buildNurseryDto(saved));
        return result;
    }

    public Map<String, Object> getNurseries(String city, Double maxPrice, Double minRating, Integer minSpots) {
        List<Nursery> nurseries = nurseryRepository.findAll();

        if (city != null && !city.isBlank()) {
            nurseries = nurseries.stream()
                    .filter(n -> city.equalsIgnoreCase(n.getCity()))
                    .collect(Collectors.toList());
        }
        if (maxPrice != null) {
            nurseries = nurseries.stream()
                    .filter(n -> n.getPricePerMonth() != null && n.getPricePerMonth() <= maxPrice)
                    .collect(Collectors.toList());
        }
        if (minRating != null) {
            nurseries = nurseries.stream()
                    .filter(n -> n.getRating() != null && n.getRating() >= minRating)
                    .collect(Collectors.toList());
        }
        if (minSpots != null) {
            nurseries = nurseries.stream()
                    .filter(n -> n.getAvailableSpots() != null && n.getAvailableSpots() >= minSpots)
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> dtos = nurseries.stream()
                .map(this::buildNurseryDto)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("nurseries", dtos);
        return result;
    }

    public Map<String, Object> getNurseriesByOwner(String ownerId) {
        List<Nursery> nurseries = nurseryRepository.findByOwnerId(ownerId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("nurseries", nurseries.stream().map(this::buildNurseryDto).collect(Collectors.toList()));
        return result;
    }

    public Map<String, Object> getNursery(String id) {
        Map<String, Object> result = new HashMap<>();
        Optional<Nursery> opt = nurseryRepository.findById(id);
        if (opt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Nursery not found");
            return result;
        }
        result.put("success", true);
        result.put("nursery", buildNurseryDto(opt.get()));
        return result;
    }

    public Map<String, Object> getReviews(String nurseryId) {
        List<Review> reviews = reviewRepository.findByNurseryIdOrderByCreatedAtDesc(nurseryId);
        List<Map<String, Object>> dtos = reviews.stream().map(r -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", r.getId());
            dto.put("nursery_id", r.getNurseryId());
            dto.put("parent_id", r.getParentId());
            dto.put("rating", r.getRating());
            dto.put("comment", r.getComment());
            dto.put("created_at", r.getCreatedAt());
            userRepository.findById(r.getParentId())
                    .ifPresent(u -> dto.put("parent_name", u.getName()));
            return dto;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("reviews", dtos);
        return result;
    }

    public Map<String, Object> createReview(String nurseryId, String parentId, Integer rating, String comment) {
        Review review = new Review();
        review.setNurseryId(nurseryId);
        review.setParentId(parentId);
        review.setRating(rating);
        review.setComment(comment);
        Review saved = reviewRepository.save(review);

        // Update nursery rating
        updateNurseryRating(nurseryId);

        Map<String, Object> reviewDto = new HashMap<>();
        reviewDto.put("id", saved.getId());
        reviewDto.put("nursery_id", saved.getNurseryId());
        reviewDto.put("parent_id", saved.getParentId());
        reviewDto.put("rating", saved.getRating());
        reviewDto.put("comment", saved.getComment());
        reviewDto.put("created_at", saved.getCreatedAt());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("review", reviewDto);
        return result;
    }

    public void updateNurseryRating(String nurseryId) {
        List<Review> reviews = reviewRepository.findByNurseryId(nurseryId);
        if (!reviews.isEmpty()) {
            double avg = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            nurseryRepository.findById(nurseryId).ifPresent(nursery -> {
                nursery.setRating(Math.round(avg * 10.0) / 10.0);
                nursery.setReviewCount(reviews.size());
                nurseryRepository.save(nursery);
            });
        }
    }

    public Map<String, Object> getStats(String nurseryId) {
        Map<String, Object> result = new HashMap<>();
        Optional<Nursery> optNursery = nurseryRepository.findById(nurseryId);
        if (optNursery.isEmpty()) {
            result.put("success", false);
            result.put("message", "Nursery not found");
            return result;
        }
        Nursery nursery = optNursery.get();

        long activeEnrollments = enrollmentRepository.countByNurseryIdAndStatus(nurseryId, "active");
        long pendingEnrollments = enrollmentRepository.countByNurseryIdAndStatus(nurseryId, "pending");
        List<Review> reviews = reviewRepository.findByNurseryId(nurseryId);
        double rating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        List<Payment> paidPayments = paymentRepository.findByNurseryIdAndPaymentStatus(nurseryId, "paid");
        double monthlyRevenue = paidPayments.stream().mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0).sum();

        // Recent reviews
        List<Map<String, Object>> recentReviews = reviews.stream()
                .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                .limit(5)
                .map(r -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", r.getId());
                    dto.put("rating", r.getRating());
                    dto.put("comment", r.getComment());
                    dto.put("created_at", r.getCreatedAt());
                    userRepository.findById(r.getParentId()).ifPresent(u -> dto.put("parent_name", u.getName()));
                    return dto;
                }).collect(Collectors.toList());

        Map<String, Object> stats = new HashMap<>();
        stats.put("enrolledChildren", activeEnrollments);
        stats.put("totalSpots", nursery.getTotalSpots());
        stats.put("availableSpots", nursery.getAvailableSpots());
        stats.put("monthlyRevenue", monthlyRevenue);
        stats.put("pendingEnrollments", pendingEnrollments);
        stats.put("rating", Math.round(rating * 10.0) / 10.0);
        stats.put("reviewCount", reviews.size());
        stats.put("recentReviews", recentReviews);

        result.put("success", true);
        result.put("stats", stats);
        return result;
    }

    public Map<String, Object> getEnrolledChildren(String nurseryId) {
        List<Enrollment> enrollments = enrollmentRepository.findByNurseryIdAndStatus(nurseryId, "active");

        // Group by parent
        Map<String, List<Enrollment>> byParent = enrollments.stream()
                .collect(Collectors.groupingBy(Enrollment::getParentId));

        List<Map<String, Object>> parentsList = new ArrayList<>();

        byParent.forEach((parentId, parentEnrollments) -> {
            Map<String, Object> parentDto = new HashMap<>();
            userRepository.findById(parentId).ifPresent(u -> {
                parentDto.put("parentId", u.getId());
                parentDto.put("parentName", u.getName());
                parentDto.put("parentEmail", u.getEmail());
                parentDto.put("parentPhone", u.getPhone());
            });

            List<Map<String, Object>> childrenList = parentEnrollments.stream().map(e -> {
                Map<String, Object> childDto = new HashMap<>();
                childDto.put("enrollmentId", e.getId());
                childDto.put("enrollmentStatus", e.getStatus());
                childDto.put("startDate", e.getStartDate());
                childDto.put("enrollmentDate", e.getCreatedAt());
                childRepository.findById(e.getChildId()).ifPresent(c -> {
                    childDto.put("childId", c.getId());
                    childDto.put("childName", c.getName());
                    childDto.put("age", c.getAge());
                    childDto.put("birthDate", c.getDateOfBirth());
                });
                return childDto;
            }).collect(Collectors.toList());

            parentDto.put("children", childrenList);
            if (!parentDto.isEmpty()) parentsList.add(parentDto);
        });

        long totalChildren = enrollments.size();
        long totalParents = byParent.size();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("nurseryId", nurseryId);
        result.put("totalParents", totalParents);
        result.put("totalChildren", totalChildren);
        result.put("parents", parentsList);
        return result;
    }

    public Map<String, Object> getSchedule(String nurseryId) {
        List<DailySchedule> schedules = dailyScheduleRepository.findByNurseryIdOrderByTimeSlotAsc(nurseryId);
        List<Map<String, Object>> dtos = schedules.stream().map(s -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", s.getId());
            dto.put("timeSlot", s.getTimeSlot());
            dto.put("activityName", s.getActivityName());
            dto.put("description", s.getDescription());
            dto.put("participantCount", s.getParticipantCount());
            return dto;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("schedule", dtos);
        return result;
    }

    public Map<String, Object> createSchedule(String nurseryId, String timeSlot, String activityName,
                                               String description, Integer participantCount) {
        DailySchedule schedule = new DailySchedule();
        schedule.setNurseryId(nurseryId);
        schedule.setTimeSlot(timeSlot);
        schedule.setActivityName(activityName);
        schedule.setDescription(description);
        schedule.setParticipantCount(participantCount);
        DailySchedule saved = dailyScheduleRepository.save(schedule);

        Map<String, Object> dto = new HashMap<>();
        dto.put("id", saved.getId());
        dto.put("timeSlot", saved.getTimeSlot());
        dto.put("activityName", saved.getActivityName());
        dto.put("description", saved.getDescription());
        dto.put("participantCount", saved.getParticipantCount());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("schedule", dto);
        return result;
    }

    public Map<String, Object> getStatistics(String nurseryId) {
        Optional<Nursery> optNursery = nurseryRepository.findById(nurseryId);
        Nursery nursery = optNursery.orElse(null);

        List<Enrollment> allEnrollments = enrollmentRepository.findByNurseryId(nurseryId);
        long totalEnrollments = allEnrollments.size();
        long activeEnrollments = allEnrollments.stream().filter(e -> "active".equals(e.getStatus())).count();
        long pendingEnrollments = allEnrollments.stream().filter(e -> "pending".equals(e.getStatus())).count();

        List<Payment> payments = paymentRepository.findByNurseryId(nurseryId);
        double totalRevenue = payments.stream()
                .filter(p -> "paid".equals(p.getPaymentStatus()))
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0).sum();
        // Monthly revenue: payments paid in the current calendar month
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        double monthlyRevenue = payments.stream()
                .filter(p -> "paid".equals(p.getPaymentStatus())
                        && p.getPaymentMonth() != null && p.getPaymentMonth() == currentMonth
                        && p.getPaymentYear() != null && p.getPaymentYear() == currentYear)
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0).sum();

        List<Review> reviews = reviewRepository.findByNurseryId(nurseryId);
        double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        int totalCapacity = nursery != null && nursery.getTotalSpots() != null ? nursery.getTotalSpots() : 0;
        long capacityUsed = activeEnrollments;

        // Age group breakdown
        Map<String, Long> childrenByAgeGroup = new HashMap<>();
        allEnrollments.stream()
                .filter(e -> "active".equals(e.getStatus()))
                .forEach(e -> childRepository.findById(e.getChildId()).ifPresent(c -> {
                    int age = c.getAge() != null ? c.getAge() : 0;
                    String group = age < 1 ? "0-1" : age <= 2 ? "1-2" : age <= 3 ? "2-3" : "3+";
                    childrenByAgeGroup.merge(group, 1L, Long::sum);
                }));

        long paidCount = payments.stream().filter(p -> "paid".equals(p.getPaymentStatus())).count();
        long unpaidCount = payments.stream().filter(p -> "unpaid".equals(p.getPaymentStatus())).count();

        Map<String, Object> paymentStats = new HashMap<>();
        paymentStats.put("paidCount", paidCount);
        paymentStats.put("unpaidCount", unpaidCount);
        paymentStats.put("totalAmount", totalRevenue);

        // Recent reviews
        List<Map<String, Object>> recentReviews = reviews.stream()
                .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                .limit(5)
                .map(r -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", r.getId());
                    dto.put("rating", r.getRating());
                    dto.put("comment", r.getComment());
                    dto.put("created_at", r.getCreatedAt());
                    userRepository.findById(r.getParentId()).ifPresent(u -> dto.put("parent_name", u.getName()));
                    return dto;
                }).collect(Collectors.toList());

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEnrollments", totalEnrollments);
        stats.put("activeEnrollments", activeEnrollments);
        stats.put("pendingEnrollments", pendingEnrollments);
        stats.put("totalRevenue", totalRevenue);
        stats.put("monthlyRevenue", monthlyRevenue);
        stats.put("averageRating", Math.round(avgRating * 10.0) / 10.0);
        stats.put("totalReviews", reviews.size());
        stats.put("recentReviews", recentReviews);
        stats.put("capacityUsed", capacityUsed);
        stats.put("totalCapacity", totalCapacity);
        stats.put("childrenByAgeGroup", childrenByAgeGroup);
        stats.put("paymentStats", paymentStats);
        return stats;
    }

    private Map<String, Object> buildNurseryDto(Nursery nursery) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", nursery.getId());
        dto.put("ownerId", nursery.getOwnerId());
        dto.put("name", nursery.getName());
        dto.put("description", nursery.getDescription());
        dto.put("address", nursery.getAddress());
        dto.put("city", nursery.getCity());
        dto.put("postalCode", nursery.getPostalCode());
        dto.put("latitude", nursery.getLatitude());
        dto.put("longitude", nursery.getLongitude());
        dto.put("phone", nursery.getPhone());
        dto.put("email", nursery.getEmail());
        dto.put("hours", nursery.getHours());
        dto.put("pricePerMonth", nursery.getPricePerMonth());
        dto.put("totalSpots", nursery.getTotalSpots());
        dto.put("availableSpots", nursery.getAvailableSpots());
        dto.put("ageRange", nursery.getAgeRange());
        dto.put("rating", nursery.getRating());
        dto.put("photoUrl", nursery.getPhotoUrl());
        dto.put("reviewCount", nursery.getReviewCount());
        dto.put("staffCount", nursery.getStaffCount());
        dto.put("facilities", nursery.getFacilities());
        dto.put("activities", nursery.getActivities());
        dto.put("createdAt", nursery.getCreatedAt());
        return dto;
    }

    private Double toDouble(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (NumberFormatException e) {
            log.warning("Cannot parse double from: " + val);
            return null;
        }
    }

    private Integer toInt(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); } catch (NumberFormatException e) {
            log.warning("Cannot parse integer from: " + val);
            return null;
        }
    }
}
