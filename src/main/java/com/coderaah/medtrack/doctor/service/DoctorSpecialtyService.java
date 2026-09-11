package com.coderaah.medtrack.doctor.service;

import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.domain.DoctorSpecialty;
import com.coderaah.medtrack.doctor.domain.Specialty;
import com.coderaah.medtrack.doctor.dto.AssignDoctorSpecialtyRequest;
import com.coderaah.medtrack.doctor.dto.DoctorSpecialtyResponse;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.doctor.exception.DoctorSpecialtyNotFoundException;
import com.coderaah.medtrack.doctor.exception.DuplicateDoctorSpecialtyException;
import com.coderaah.medtrack.doctor.exception.SpecialtyNotFoundException;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
import com.coderaah.medtrack.doctor.repository.DoctorSpecialtyRepository;
import com.coderaah.medtrack.doctor.repository.SpecialtyRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DoctorSpecialtyService {

    private final DoctorSpecialtyRepository doctorSpecialtyRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final SpecialtyRepository specialtyRepository;

    public DoctorSpecialtyService(DoctorSpecialtyRepository doctorSpecialtyRepository,
                                  DoctorProfileRepository doctorProfileRepository,
                                  SpecialtyRepository specialtyRepository) {
        this.doctorSpecialtyRepository = doctorSpecialtyRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.specialtyRepository = specialtyRepository;
    }

    public DoctorSpecialtyResponse assign(Long doctorId, AssignDoctorSpecialtyRequest request) {
        DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor " + doctorId + " not found"));
        Specialty specialty = specialtyRepository.findById(request.specialtyId())
                .orElseThrow(() -> new SpecialtyNotFoundException("Specialty " + request.specialtyId() + " not found"));

        if (doctorSpecialtyRepository.existsByDoctor_IdAndSpecialty_Id(doctorId, specialty.getId())) {
            throw new DuplicateDoctorSpecialtyException(
                    "Doctor " + doctorId + " already has specialty " + specialty.getId() + " assigned");
        }

        if (request.primarySpecialty()) {
            clearExistingPrimary(doctorId);
        }

        DoctorSpecialty saved = doctorSpecialtyRepository.save(new DoctorSpecialty(doctor, specialty, request.primarySpecialty()));
        return DoctorSpecialtyResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<DoctorSpecialtyResponse> findByDoctor(Long doctorId) {
        if (!doctorProfileRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor " + doctorId + " not found");
        }
        return doctorSpecialtyRepository.findByDoctor_Id(doctorId).stream().map(DoctorSpecialtyResponse::from).toList();
    }

    public void remove(Long doctorId, Long specialtyId) {
        DoctorSpecialty doctorSpecialty = doctorSpecialtyRepository.findByDoctor_IdAndSpecialty_Id(doctorId, specialtyId)
                .orElseThrow(() -> new DoctorSpecialtyNotFoundException(
                        "Doctor " + doctorId + " has no assignment for specialty " + specialtyId));
        doctorSpecialtyRepository.delete(doctorSpecialty);
    }

    private void clearExistingPrimary(Long doctorId) {
        doctorSpecialtyRepository.findByDoctor_IdAndPrimarySpecialtyTrue(doctorId)
                .forEach(existingPrimary -> existingPrimary.setPrimarySpecialty(false));
    }
}