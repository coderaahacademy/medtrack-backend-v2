package com.coderaah.medtrack.doctor.service;

import com.coderaah.medtrack.common.exception.ConflictException;
import com.coderaah.medtrack.common.exception.ResourceNotFoundException;
import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.domain.DoctorSpecialty;
import com.coderaah.medtrack.doctor.domain.Specialty;
import com.coderaah.medtrack.doctor.dto.AssignDoctorSpecialtyRequest;
import com.coderaah.medtrack.doctor.repository.DoctorSpecialtyRepository;
import com.coderaah.medtrack.doctor.repository.SpecialtyRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DoctorSpecialtyService {

    private final DoctorSpecialtyRepository doctorSpecialtyRepository;
    private final SpecialtyRepository specialtyRepository;
    private final EntityManager entityManager;

    public DoctorSpecialtyService(DoctorSpecialtyRepository doctorSpecialtyRepository,
                                  SpecialtyRepository specialtyRepository,
                                  EntityManager entityManager) {
        this.doctorSpecialtyRepository = doctorSpecialtyRepository;
        this.specialtyRepository = specialtyRepository;
        this.entityManager = entityManager;
    }

    public DoctorSpecialty assign(Long doctorId, AssignDoctorSpecialtyRequest request) {
        DoctorProfile doctor = findDoctorOrThrow(doctorId);
        Specialty specialty = specialtyRepository.findById(request.specialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialty " + request.specialtyId() + " not found"));

        if (doctorSpecialtyRepository.existsByDoctor_IdAndSpecialty_Id(doctorId, specialty.getId())) {
            throw new ConflictException("Doctor " + doctorId + " already has specialty " + specialty.getId() + " assigned");
        }

        if (request.primarySpecialty()) {
            clearExistingPrimary(doctorId);
        }

        return doctorSpecialtyRepository.save(new DoctorSpecialty(doctor, specialty, request.primarySpecialty()));
    }

    @Transactional(readOnly = true)
    public List<DoctorSpecialty> findByDoctor(Long doctorId) {
        findDoctorOrThrow(doctorId);
        return doctorSpecialtyRepository.findByDoctor_Id(doctorId);
    }

    public void remove(Long doctorId, Long specialtyId) {
        DoctorSpecialty doctorSpecialty = doctorSpecialtyRepository.findByDoctor_IdAndSpecialty_Id(doctorId, specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor " + doctorId + " has no assignment for specialty " + specialtyId));
        doctorSpecialtyRepository.delete(doctorSpecialty);
    }

    private void clearExistingPrimary(Long doctorId) {
        doctorSpecialtyRepository.findByDoctor_IdAndPrimarySpecialtyTrue(doctorId)
                .forEach(existingPrimary -> existingPrimary.setPrimarySpecialty(false));
    }

    private DoctorProfile findDoctorOrThrow(Long doctorId) {
        DoctorProfile doctor = entityManager.find(DoctorProfile.class, doctorId);
        if (doctor == null) {
            throw new ResourceNotFoundException("Doctor " + doctorId + " not found");
        }
        return doctor;
    }
}
