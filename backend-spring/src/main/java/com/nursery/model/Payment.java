package com.nursery.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    @Indexed
    private String enrollmentId;

    private String parentId;
    private String nurseryId;
    private String childId;
    private Double amount;
    private String paymentStatus = "unpaid"; // paid, unpaid
    private Instant paymentDate;
    private Integer paymentMonth;
    private Integer paymentYear;
    private String cardLastDigits;
    private String transactionId;
    private String description;
    private Instant createdAt = Instant.now();

    public Payment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getNurseryId() { return nurseryId; }
    public void setNurseryId(String nurseryId) { this.nurseryId = nurseryId; }

    public String getChildId() { return childId; }
    public void setChildId(String childId) { this.childId = childId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Instant getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Instant paymentDate) { this.paymentDate = paymentDate; }

    public Integer getPaymentMonth() { return paymentMonth; }
    public void setPaymentMonth(Integer paymentMonth) { this.paymentMonth = paymentMonth; }

    public Integer getPaymentYear() { return paymentYear; }
    public void setPaymentYear(Integer paymentYear) { this.paymentYear = paymentYear; }

    public String getCardLastDigits() { return cardLastDigits; }
    public void setCardLastDigits(String cardLastDigits) { this.cardLastDigits = cardLastDigits; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
