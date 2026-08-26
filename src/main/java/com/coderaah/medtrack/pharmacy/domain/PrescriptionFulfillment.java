package com.coderaah.medtrack.pharmacy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "prescription_fulfillments")
public class PrescriptionFulfillment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private PrescriptionSubmission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_membership_id", nullable = false)
    private PharmacyStaffMembership processedByMembership;

    @Column(name = "dispensed_at", nullable = false)
    private LocalDateTime dispensedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PrescriptionFulfillmentStatus status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PrescriptionFulfillment() {
    }

    public PrescriptionFulfillment(PrescriptionSubmission submission, PharmacyStaffMembership processedByMembership, LocalDateTime dispensedAt, PrescriptionFulfillmentStatus status) {
        this.submission = submission;
        this.processedByMembership = processedByMembership;
        this.dispensedAt = dispensedAt;
        this.status = status;
    }

    public PrescriptionFulfillment(PrescriptionSubmission submission, PharmacyStaffMembership processedByMembership, LocalDateTime dispensedAt, PrescriptionFulfillmentStatus status, String notes) {
        this.submission = submission;
        this.processedByMembership = processedByMembership;
        this.dispensedAt = dispensedAt;
        this.status = status;
        this.notes = notes;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PrescriptionSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(PrescriptionSubmission submission) {
        this.submission = submission;
    }

    public PharmacyStaffMembership getProcessedByMembership() {
        return processedByMembership;
    }

    public void setProcessedByMembership(PharmacyStaffMembership processedByMembership) {
        this.processedByMembership = processedByMembership;
    }

    public LocalDateTime getDispensedAt() {
        return dispensedAt;
    }

    public void setDispensedAt(LocalDateTime dispensedAt) {
        this.dispensedAt = dispensedAt;
    }

    public PrescriptionFulfillmentStatus getStatus() {
        return status;
    }

    public void setStatus(PrescriptionFulfillmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrescriptionFulfillment that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
