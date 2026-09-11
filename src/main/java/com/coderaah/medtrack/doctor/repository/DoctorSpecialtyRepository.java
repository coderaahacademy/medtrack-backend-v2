package com.coderaah.medtrack.doctor.repository;

import com.coderaah.medtrack.doctor.domain.DoctorSpecialty;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DoctorSpecialtyRepository extends JpaRepository<DoctorSpecialty, Long> {

    boolean existsByDoctor_IdAndSpecialty_Id(Long doctorId, Long specialtyId);

    List<DoctorSpecialty> findByDoctor_Id(Long doctorId);

    Optional<DoctorSpecialty> findByDoctor_IdAndSpecialty_Id(Long doctorId, Long specialtyId);

    List<DoctorSpecialty> findByDoctor_IdAndPrimarySpecialtyTrue(Long doctorId);
}
