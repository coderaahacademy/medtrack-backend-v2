# MedTrack Backend

MedTrack is a modern healthcare and clinical practice management backend platform built with Spring Boot. This repository serves as the foundational domain skeleton and architectural starter for internship projects.

---

## Technology Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.4.3
- **Data Persistence:** Spring Data JPA / Hibernate 6.x
- **Validation:** Jakarta Validation (`jakarta.validation-api` / Hibernate Validator)
- **Build System:** Apache Maven (Maven Wrapper included)

---

## Package-by-Feature Architecture

The codebase strictly adheres to a package-by-feature architecture with predefined architectural layers. At this stage, only the `domain` packages contain implementations (entities and enums), while `dto`, `repository`, `service`, and `controller` packages serve as committed placeholders (`package-info.java`) for future tickets.

```text
com.coderaah.medtrack
├── MedTrackApplication.java
├── common
│   ├── audit
│   │   └── AuditLog.java
│   ├── config
│   │   └── package-info.java
│   └── exception
│       └── package-info.java
├── identity
│   ├── domain (Person, UserAccount, UserRole, UserAccountStatus, Role)
│   ├── dto (package-info.java)
│   ├── repository (package-info.java)
│   ├── service (package-info.java)
│   └── controller (package-info.java)
├── patient
│   ├── domain (PatientProfile, PatientAllergy, PatientCondition, BloodType, AllergySeverity, AllergyStatus, PatientConditionStatus)
│   ├── dto (package-info.java)
│   ├── repository (package-info.java)
│   ├── service (package-info.java)
│   └── controller (package-info.java)
├── doctor
│   ├── domain (DoctorProfile, Specialty, DoctorSpecialty, PatientDoctorRelationship, DoctorAvailabilityRule, DoctorScheduleException, DoctorRelationshipType, ScheduleExceptionType, ScheduleExceptionReason)
│   ├── dto (package-info.java)
│   ├── repository (package-info.java)
│   ├── service (package-info.java)
│   └── controller (package-info.java)
├── appointment
│   ├── domain (Appointment, AppointmentStatusHistory, AppointmentStatus, AppointmentType)
│   ├── dto (package-info.java)
│   ├── repository (package-info.java)
│   ├── service (package-info.java)
│   └── controller (package-info.java)
├── visit
│   ├── domain (Visit, MedicalReport, VisitStatus, MedicalReportType)
│   ├── dto (package-info.java)
│   ├── repository (package-info.java)
│   ├── service (package-info.java)
│   └── controller (package-info.java)
├── medication
│   ├── domain (Medication, DosageForm)
│   ├── dto (package-info.java)
│   ├── repository (package-info.java)
│   ├── service (package-info.java)
│   └── controller (package-info.java)
├── prescription
│   ├── domain (Prescription, PrescriptionItem, PrescriptionStatusHistory, PrescriptionStatus)
│   ├── dto (package-info.java)
│   ├── repository (package-info.java)
│   ├── service (package-info.java)
│   └── controller (package-info.java)
├── pharmacy
│   ├── domain (Pharmacy, PharmacyStaffMembership, PrescriptionSubmission, PrescriptionFulfillment, PrescriptionFulfillmentItem, PharmacyInventory, InventoryMovement, PharmacyStaffRole, PrescriptionSubmissionStatus, PrescriptionFulfillmentStatus, InventoryMovementType)
│   ├── dto (package-info.java)
│   ├── repository (package-info.java)
│   ├── service (package-info.java)
│   └── controller (package-info.java)
├── notification
│   ├── domain (Notification, NotificationType, NotificationStatus)
│   ├── dto (package-info.java)
│   ├── repository (package-info.java)
│   ├── service (package-info.java)
│   └── controller (package-info.java)
└── security
    └── package-info.java
```

---

## Building and Compiling

Using Maven Wrapper:
```bash
./mvnw clean compile
```

Using Maven:
```bash
mvn clean compile
```
