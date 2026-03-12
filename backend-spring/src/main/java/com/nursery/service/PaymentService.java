package com.nursery.service;

import com.nursery.model.*;
import com.nursery.repository.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NurseryRepository nurseryRepository;
    private final ChildRepository childRepository;
    private final UserRepository userRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          EnrollmentRepository enrollmentRepository,
                          NurseryRepository nurseryRepository,
                          ChildRepository childRepository,
                          UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.nurseryRepository = nurseryRepository;
        this.childRepository = childRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> syncPayments() {
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        int created = 0;
        for (Enrollment e : enrollments) {
            if (paymentRepository.findByEnrollmentId(e.getId()).isEmpty()) {
                nurseryRepository.findById(e.getNurseryId()).ifPresent(nursery -> {
                    Payment payment = new Payment();
                    payment.setEnrollmentId(e.getId());
                    payment.setParentId(e.getParentId());
                    payment.setNurseryId(e.getNurseryId());
                    payment.setChildId(e.getChildId());
                    payment.setAmount(nursery.getPricePerMonth());
                    payment.setPaymentStatus("unpaid");
                    paymentRepository.save(payment);
                });
                created++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Payment sync completed");
        result.put("paymentsCreated", created);
        return result;
    }

    public Map<String, Object> getParentStatus(String parentId) {
        // Ensure payments exist for all enrollments of this parent
        List<Enrollment> enrollments = enrollmentRepository.findByParentId(parentId);
        for (Enrollment e : enrollments) {
            if (paymentRepository.findByEnrollmentId(e.getId()).isEmpty()) {
                nurseryRepository.findById(e.getNurseryId()).ifPresent(nursery -> {
                    Payment payment = new Payment();
                    payment.setEnrollmentId(e.getId());
                    payment.setParentId(e.getParentId());
                    payment.setNurseryId(e.getNurseryId());
                    payment.setChildId(e.getChildId());
                    payment.setAmount(nursery.getPricePerMonth());
                    payment.setPaymentStatus("unpaid");
                    payment.setDescription("Monthly fee");
                    paymentRepository.save(payment);
                });
            }
        }

        List<Payment> pending = paymentRepository.findByParentIdAndPaymentStatus(parentId, "unpaid");
        List<Payment> paid = paymentRepository.findByParentIdAndPaymentStatus(parentId, "paid");

        double totalPending = pending.stream().mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0).sum();
        double totalPaid = paid.stream().mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0).sum();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("pendingPayments", pending.stream().map(this::buildDto).collect(Collectors.toList()));
        result.put("paidPayments", paid.stream().map(this::buildDto).collect(Collectors.toList()));
        result.put("totalPending", totalPending);
        result.put("totalPaid", totalPaid);
        return result;
    }

    public Map<String, Object> processPayment(String enrollmentId, String cardNumber,
                                               String expiryDate, String cvv) {
        Map<String, Object> result = new HashMap<>();
        Optional<Payment> optPayment = paymentRepository.findByEnrollmentId(enrollmentId);

        Payment payment;
        if (optPayment.isPresent()) {
            payment = optPayment.get();
        } else {
            Optional<Enrollment> optEnrollment = enrollmentRepository.findById(enrollmentId);
            if (optEnrollment.isEmpty()) {
                result.put("success", false);
                result.put("message", "Enrollment not found");
                return result;
            }
            Enrollment e = optEnrollment.get();
            payment = new Payment();
            payment.setEnrollmentId(enrollmentId);
            payment.setParentId(e.getParentId());
            payment.setNurseryId(e.getNurseryId());
            payment.setChildId(e.getChildId());
            nurseryRepository.findById(e.getNurseryId())
                    .ifPresent(n -> payment.setAmount(n.getPricePerMonth()));
        }

        if ("paid".equals(payment.getPaymentStatus())) {
            result.put("success", false);
            result.put("message", "Payment already processed");
            return result;
        }

        String transactionId = "TXN-" + UUID.randomUUID().toString().toUpperCase().replace("-", "");
        String lastDigits = cardNumber != null && cardNumber.length() >= 4
                ? cardNumber.substring(cardNumber.length() - 4) : "0000";

        java.time.LocalDate today = java.time.LocalDate.now();
        payment.setPaymentStatus("paid");
        payment.setPaymentDate(Instant.now());
        payment.setCardLastDigits(lastDigits);
        payment.setTransactionId(transactionId);
        payment.setPaymentMonth(today.getMonthValue());
        payment.setPaymentYear(today.getYear());

        paymentRepository.save(payment);

        result.put("success", true);
        result.put("message", "Payment processed successfully");
        result.put("payment", buildDto(payment));
        result.put("transactionId", transactionId);
        return result;
    }

    public Map<String, Object> getNurseryPayments(String nurseryId) {
        List<Payment> payments = paymentRepository.findByNurseryId(nurseryId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("payments", payments.stream().map(this::buildDto).collect(Collectors.toList()));
        return result;
    }

    public Map<String, Object> getOwnerPayments(String ownerId) {
        List<String> nurseryIds = nurseryRepository.findByOwnerId(ownerId).stream()
                .map(Nursery::getId).collect(Collectors.toList());

        List<Payment> payments = paymentRepository.findAll().stream()
                .filter(p -> nurseryIds.contains(p.getNurseryId()))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("payments", payments.stream().map(this::buildDto).collect(Collectors.toList()));
        return result;
    }

    public Map<String, Object> getNurseryStats(String nurseryId) {
        List<Enrollment> enrollments = enrollmentRepository.findByNurseryId(nurseryId);
        List<Payment> payments = paymentRepository.findByNurseryId(nurseryId);

        long totalEnrollments = enrollments.size();
        double totalExpected = payments.stream()
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0).sum();
        double totalReceived = payments.stream()
                .filter(p -> "paid".equals(p.getPaymentStatus()))
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0).sum();
        double totalPending = payments.stream()
                .filter(p -> "unpaid".equals(p.getPaymentStatus()))
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0).sum();
        long paidCount = payments.stream().filter(p -> "paid".equals(p.getPaymentStatus())).count();
        long unpaidCount = payments.stream().filter(p -> "unpaid".equals(p.getPaymentStatus())).count();
        double percentage = totalExpected > 0 ? (totalReceived / totalExpected) * 100 : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("total_enrollments", totalEnrollments);
        stats.put("total_expected", totalExpected);
        stats.put("total_received", totalReceived);
        stats.put("total_pending", totalPending);
        stats.put("paid_count", paidCount);
        stats.put("unpaid_count", unpaidCount);
        stats.put("payment_percentage", Math.round(percentage * 10.0) / 10.0);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("stats", stats);
        return result;
    }

    public Map<String, Object> getOwnerStats(String ownerId) {
        List<String> nurseryIds = nurseryRepository.findByOwnerId(ownerId).stream()
                .map(Nursery::getId).collect(Collectors.toList());

        List<Payment> payments = paymentRepository.findAll().stream()
                .filter(p -> nurseryIds.contains(p.getNurseryId()))
                .collect(Collectors.toList());

        double totalReceived = payments.stream()
                .filter(p -> "paid".equals(p.getPaymentStatus()))
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0).sum();
        double totalPending = payments.stream()
                .filter(p -> "unpaid".equals(p.getPaymentStatus()))
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0).sum();
        long paidCount = payments.stream().filter(p -> "paid".equals(p.getPaymentStatus())).count();
        long unpaidCount = payments.stream().filter(p -> "unpaid".equals(p.getPaymentStatus())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReceived", totalReceived);
        stats.put("totalPending", totalPending);
        stats.put("paidCount", paidCount);
        stats.put("unpaidCount", unpaidCount);
        stats.put("totalPayments", payments.size());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("stats", stats);
        return result;
    }

    public Map<String, Object> getParentHistory(String parentId, Integer limit) {
        List<Payment> payments = paymentRepository.findByParentIdOrderByCreatedAtDesc(parentId);
        if (limit != null && limit > 0 && payments.size() > limit) {
            payments = payments.subList(0, limit);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("payments", payments.stream().map(this::buildDto).collect(Collectors.toList()));
        return result;
    }

    private Map<String, Object> buildDto(Payment p) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", p.getId());
        dto.put("enrollmentId", p.getEnrollmentId());
        dto.put("parentId", p.getParentId());
        dto.put("nurseryId", p.getNurseryId());
        dto.put("childId", p.getChildId());
        dto.put("amount", p.getAmount());
        dto.put("paymentStatus", p.getPaymentStatus());
        dto.put("paymentDate", p.getPaymentDate());
        dto.put("cardLastDigits", p.getCardLastDigits());
        dto.put("transactionId", p.getTransactionId());
        dto.put("description", p.getDescription());
        dto.put("createdAt", p.getCreatedAt());

        nurseryRepository.findById(p.getNurseryId() != null ? p.getNurseryId() : "")
                .ifPresent(n -> dto.put("nurseryName", n.getName()));

        childRepository.findById(p.getChildId() != null ? p.getChildId() : "")
                .ifPresent(c -> dto.put("childName", c.getName()));

        return dto;
    }
}
