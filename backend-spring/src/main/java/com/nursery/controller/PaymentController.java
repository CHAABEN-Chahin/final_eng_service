package com.nursery.controller;

import com.nursery.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncPayments() {
        return ResponseEntity.ok(paymentService.syncPayments());
    }

    @GetMapping("/parent/{parentId}/status")
    public ResponseEntity<Map<String, Object>> getParentStatus(@PathVariable String parentId) {
        return ResponseEntity.ok(paymentService.getParentStatus(parentId));
    }

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody Map<String, Object> body) {
        String enrollmentId = (String) body.get("enrollmentId");
        String cardNumber = (String) body.get("cardNumber");
        String expiryDate = (String) body.get("expiryDate");
        String cvv = (String) body.get("cvv");
        Map<String, Object> result = paymentService.processPayment(enrollmentId, cardNumber, expiryDate, cvv);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return ResponseEntity.status(success ? 200 : 400).body(result);
    }

    @GetMapping("/nursery/{nurseryId}")
    public ResponseEntity<Map<String, Object>> getNurseryPayments(@PathVariable String nurseryId) {
        return ResponseEntity.ok(paymentService.getNurseryPayments(nurseryId));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<Map<String, Object>> getOwnerPayments(@PathVariable String ownerId) {
        return ResponseEntity.ok(paymentService.getOwnerPayments(ownerId));
    }

    @GetMapping("/nursery/{nurseryId}/stats")
    public ResponseEntity<Map<String, Object>> getNurseryStats(@PathVariable String nurseryId) {
        return ResponseEntity.ok(paymentService.getNurseryStats(nurseryId));
    }

    @GetMapping("/owner/{ownerId}/stats")
    public ResponseEntity<Map<String, Object>> getOwnerStats(@PathVariable String ownerId) {
        return ResponseEntity.ok(paymentService.getOwnerStats(ownerId));
    }

    @GetMapping("/parent/{parentId}/history")
    public ResponseEntity<Map<String, Object>> getParentHistory(
            @PathVariable String parentId,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(paymentService.getParentHistory(parentId, limit));
    }
}
