package com.coderaah.medtrack.doctor.service;

import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.domain.DoctorRelationshipType;
import com.coderaah.medtrack.doctor.domain.PatientDoctorRelationship;
import com.coderaah.medtrack.doctor.dto.CreatePatientDoctorRelationshipRequest;
import com.coderaah.medtrack.doctor.dto.PatientDoctorRelationshipResponse;
import com.coderaah.medtrack.doctor.exception.ActiveFamilyDoctorAlreadyExistsException;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.doctor.exception.PatientDoctorRelationshipNotFoundException;
import com.coderaah.medtrack.doctor.exception.RelationshipAlreadyEndedException;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
import com.coderaah.medtrack.doctor.repository.PatientDoctorRelationshipRepository;
import com.coderaah.medtrack.patient.domain.PatientProfile;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import com.coderaah.medtrack.patient.repository.PatientProfileRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PatientDoctorRelationshipService {

    private final PatientDoctorRelationshipRepository relationshipRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    public PatientDoctorRelationshipService(PatientDoctorRelationshipRepository relationshipRepository,
                                            PatientProfileRepository patientProfileRepository,
                                            DoctorProfileRepository doctorProfileRepository) {
        this.relationshipRepository = relationshipRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
    }

    public PatientDoctorRelationshipResponse assign(Long patientId, CreatePatientDoctorRelationshipRequest request) {
        PatientProfile patient = patientProfileRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient " + patientId + " not found"));
        DoctorProfile doctor = doctorProfileRepository.findById(request.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Doctor " + request.doctorId() + " not found"));

        if (request.relationshipType() == DoctorRelationshipType.FAMILY_DOCTOR && hasActiveFamilyDoctor(patientId)) {
            throw new ActiveFamilyDoctorAlreadyExistsException("Patient " + patientId + " already has an active family doctor");
        }

        PatientDoctorRelationship saved = relationshipRepository.save(
                new PatientDoctorRelationship(patient, doctor, request.relationshipType(), LocalDateTime.now()));
        return PatientDoctorRelationshipResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<PatientDoctorRelationshipResponse> findActiveDoctorsForPatient(Long patientId) {
        if (!patientProfileRepository.existsById(patientId)) {
            throw new PatientNotFoundException("Patient " + patientId + " not found");
        }
        return relationshipRepository.findByPatient_IdAndActiveTrue(patientId).stream()
                .map(PatientDoctorRelationshipResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PatientDoctorRelationshipResponse> findActivePatientsForDoctor(Long doctorId) {
        if (!doctorProfileRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor " + doctorId + " not found");
        }
        return relationshipRepository.findByDoctor_IdAndActiveTrue(doctorId).stream()
                .map(PatientDoctorRelationshipResponse::from)
                .toList();
    }

    public PatientDoctorRelationshipResponse end(Long relationshipId) {
        PatientDoctorRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new PatientDoctorRelationshipNotFoundException(
                        "Patient-doctor relationship " + relationshipId + " not found"));
        if (!relationship.isActive()) {
            throw new RelationshipAlreadyEndedException(
                    "Patient-doctor relationship " + relationshipId + " has already ended");
        }
        relationship.setActive(false);
        relationship.setEndedAt(LocalDateTime.now());
        return PatientDoctorRelationshipResponse.from(relationshipRepository.save(relationship));
    }

    private boolean hasActiveFamilyDoctor(Long patientId) {
        return relationshipRepository
                .findByPatient_IdAndRelationshipTypeAndActiveTrue(patientId, DoctorRelationshipType.FAMILY_DOCTOR)
                .isPresent();
    }
}