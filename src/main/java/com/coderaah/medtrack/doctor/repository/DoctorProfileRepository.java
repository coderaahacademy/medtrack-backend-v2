package com.coderaah.medtrack.doctor.repository;

import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {
}
