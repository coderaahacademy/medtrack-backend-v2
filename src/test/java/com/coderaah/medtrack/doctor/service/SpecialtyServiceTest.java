package com.coderaah.medtrack.doctor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.coderaah.medtrack.common.exception.ConflictException;
import com.coderaah.medtrack.doctor.domain.Specialty;
import com.coderaah.medtrack.doctor.dto.CreateSpecialtyRequest;
import com.coderaah.medtrack.doctor.repository.SpecialtyRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpecialtyServiceTest {

    private final SpecialtyRepository specialtyRepository = mock(SpecialtyRepository.class);

    private SpecialtyService service;

    @BeforeEach
    void setUp() {
        service = new SpecialtyService(specialtyRepository);
    }

    @Test
    void create_rejectsDuplicateCode() {
        when(specialtyRepository.existsByCodeIgnoreCase("CARDIO")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateSpecialtyRequest("CARDIO", "Cardiology")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void create_savesNewSpecialty() {
        when(specialtyRepository.existsByCodeIgnoreCase("CARDIO")).thenReturn(false);
        when(specialtyRepository.save(any(Specialty.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Specialty result = service.create(new CreateSpecialtyRequest("CARDIO", "Cardiology"));

        assertThat(result.getCode()).isEqualTo("CARDIO");
        assertThat(result.getName()).isEqualTo("Cardiology");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void findAll_returnsEverySpecialty() {
        Specialty cardio = new Specialty("CARDIO", "Cardiology");
        Specialty peds = new Specialty("PEDS", "Pediatrics");
        when(specialtyRepository.findAll()).thenReturn(List.of(cardio, peds));

        assertThat(service.findAll()).containsExactly(cardio, peds);
    }
}