package com.coderaah.medtrack.doctor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.domain.DoctorSpecialty;
import com.coderaah.medtrack.doctor.domain.Specialty;
import com.coderaah.medtrack.doctor.dto.AssignDoctorSpecialtyRequest;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.doctor.exception.DoctorSpecialtyNotFoundException;
import com.coderaah.medtrack.doctor.exception.DuplicateDoctorSpecialtyException;
import com.coderaah.medtrack.doctor.dto.DoctorSpecialtyResponse;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
import com.coderaah.medtrack.doctor.repository.DoctorSpecialtyRepository;
import com.coderaah.medtrack.doctor.repository.SpecialtyRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DoctorSpecialtyServiceTest {

    private final DoctorSpecialtyRepository doctorSpecialtyRepository = mock(DoctorSpecialtyRepository.class);
    private final DoctorProfileRepository doctorProfileRepository = mock(DoctorProfileRepository.class);
    private final SpecialtyRepository specialtyRepository = mock(SpecialtyRepository.class);

    private DoctorSpecialtyService service;

    @BeforeEach
    void setUp() {
        service = new DoctorSpecialtyService(doctorSpecialtyRepository, doctorProfileRepository, specialtyRepository);
    }

    @Test
    void assign_rejectsDuplicateAssignment() {
        DoctorProfile doctor = doctorWithId(1L);
        Specialty specialty = specialtyWithId(2L);
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(specialtyRepository.findById(2L)).thenReturn(Optional.of(specialty));
        when(doctorSpecialtyRepository.existsByDoctor_IdAndSpecialty_Id(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> service.assign(1L, new AssignDoctorSpecialtyRequest(2L, false)))
                .isInstanceOf(DuplicateDoctorSpecialtyException.class);
    }

    @Test
    void assign_allowsMultipleSpecialtiesForOneDoctor() {
        DoctorProfile doctor = doctorWithId(1L);
        Specialty specialty = specialtyWithId(2L);
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(specialtyRepository.findById(2L)).thenReturn(Optional.of(specialty));
        when(doctorSpecialtyRepository.existsByDoctor_IdAndSpecialty_Id(1L, 2L)).thenReturn(false);
        when(doctorSpecialtyRepository.save(any(DoctorSpecialty.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorSpecialtyResponse result = service.assign(1L, new AssignDoctorSpecialtyRequest(2L, false));

        assertThat(result.doctorId()).isEqualTo(1L);
        assertThat(result.specialtyId()).isEqualTo(2L);
        assertThat(result.primarySpecialty()).isFalse();
    }

    @Test
    void assign_clearsPreviousPrimaryWhenNewPrimaryAssigned() {
        DoctorProfile doctor = doctorWithId(1L);
        Specialty specialty = specialtyWithId(2L);
        DoctorSpecialty existingPrimary = new DoctorSpecialty(doctor, specialtyWithId(3L), true);

        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(specialtyRepository.findById(2L)).thenReturn(Optional.of(specialty));
        when(doctorSpecialtyRepository.existsByDoctor_IdAndSpecialty_Id(1L, 2L)).thenReturn(false);
        when(doctorSpecialtyRepository.findByDoctor_IdAndPrimarySpecialtyTrue(1L)).thenReturn(List.of(existingPrimary));
        when(doctorSpecialtyRepository.save(any(DoctorSpecialty.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DoctorSpecialtyResponse result = service.assign(1L, new AssignDoctorSpecialtyRequest(2L, true));

        assertThat(existingPrimary.isPrimarySpecialty()).isFalse();
        assertThat(result.primarySpecialty()).isTrue();
    }

    @Test
    void assign_throwsWhenDoctorMissing() {
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assign(1L, new AssignDoctorSpecialtyRequest(2L, false)))
                .isInstanceOf(DoctorNotFoundException.class);
    }

    @Test
    void remove_deletesExistingAssignment() {
        DoctorSpecialty assignment = new DoctorSpecialty(doctorWithId(1L), specialtyWithId(2L), false);
        when(doctorSpecialtyRepository.findByDoctor_IdAndSpecialty_Id(1L, 2L)).thenReturn(Optional.of(assignment));

        service.remove(1L, 2L);

        verify(doctorSpecialtyRepository).delete(assignment);
    }

    @Test
    void remove_throwsWhenAssignmentMissing() {
        when(doctorSpecialtyRepository.findByDoctor_IdAndSpecialty_Id(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remove(1L, 2L)).isInstanceOf(DoctorSpecialtyNotFoundException.class);
    }

    private DoctorProfile doctorWithId(Long id) {
        DoctorProfile doctor = new DoctorProfile();
        doctor.setId(id);
        return doctor;
    }

    private Specialty specialtyWithId(Long id) {
        Specialty specialty = new Specialty("CODE" + id, "Name " + id);
        specialty.setId(id);
        return specialty;
    }
}