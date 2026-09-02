package com.coderaah.medtrack.doctor.service;

import com.coderaah.medtrack.common.exception.ConflictException;
import com.coderaah.medtrack.common.exception.ResourceNotFoundException;
import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.domain.DoctorRelationshipType;
import com.coderaah.medtrack.doctor.domain.PatientDoctorRelationship;
import com.coderaah.medtrack.doctor.dto.CreatePatientDoctorRelationshipRequest;
import com.coderaah.medtrack.doctor.repository.PatientDoctorRelationshipRepository;
import com.coderaah.medtrack.patient.domain.PatientProfile;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PatientDoctorRelationshipService {

    private final PatientDoctorRelationshipRepository relationshipRepository;
    private final EntityManager entityManager;

    public PatientDoctorRelationshipService(PatientDoctorRelationshipRepository relationshipRepository, EntityManager entityManager) {
        this.relationshipRepository = relationshipRepository;
        this.entityManager = entityManager;
    }

    public PatientDoctorRelationship assign(Long patientId, CreatePatientDoctorRelationshipRequest request) {
        PatientProfile patient = findPatientOrThrow(patientId);
        DoctorProfile doctor = findDoctorOrThrow(request.doctorId());

        if (request.relationshipType() == DoctorRelationshipType.FAMILY_DOCTOR && hasActiveFamilyDoctor(patientId)) {
            throw new ConflictException("Patient " + patientId + " already has an active family doctor");
        }

        PatientDoctorRelationship relationship =
                new PatientDoctorRelationship(patient, doctor, request.relationshipType(), LocalDateTime.now());
        return relationshipRepository.save(relationship);
    }

    @Transactional(readOnly = true)
    public List<PatientDoctorRelationship> findActiveDoctorsForPatient(Long patientId) {
        findPatientOrThrow(patientId);
        return relationshipRepository.findByPatient_IdAndActiveTrue(patientId);
    }

    @Transactional(readOnly = true)
    public List<PatientDoctorRelationship> findActivePatientsForDoctor(Long doctorId) {
        findDoctorOrThrow(doctorId);
        return relationshipRepository.findByDoctor_IdAndActiveTrue(doctorId);
    }

    public PatientDoctorRelationship end(Long relationshipId) {
        PatientDoctorRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient-doctor relationship " + relationshipId + " not found"));
        if (!relationship.isActive()) {
            throw new ConflictException("Patient-doctor relationship " + relationshipId + " has already ended");
        }
        relationship.setActive(false);
        relationship.setEndedAt(LocalDateTime.now());
        return relationshipRepository.save(relationship);
    }

    private boolean hasActiveFamilyDoctor(Long patientId) {
        return relationshipRepository
                .findByPatient_IdAndRelationshipTypeAndActiveTrue(patientId, DoctorRelationshipType.FAMILY_DOCTOR)
                .isPresent();
    }

    private PatientProfile findPatientOrThrow(Long patientId) {
        PatientProfile patient = entityManager.find(PatientProfile.class, patientId);
        if (patient == null) {
            throw new ResourceNotFoundException("Patient " + patientId + " not found");
        }
        return patient;
    }

    private DoctorProfile findDoctorOrThrow(Long doctorId) {
        DoctorProfile doctor = entityManager.find(DoctorProfile.class, doctorId);
        if (doctor == null) {
            throw new ResourceNotFoundException("Doctor " + doctorId + " not found");
        }
        return doctor;
    }
}
