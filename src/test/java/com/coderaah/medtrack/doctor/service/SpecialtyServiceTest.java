package com.coderaah.medtrack.doctor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.coderaah.medtrack.doctor.domain.Specialty;
import com.coderaah.medtrack.doctor.dto.CreateSpecialtyRequest;
import com.coderaah.medtrack.doctor.exception.DuplicateSpecialtyCodeException;
import com.coderaah.medtrack.doctor.dto.SpecialtyResponse;
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
                .isInstanceOf(DuplicateSpecialtyCodeException.class);
    }

    @Test
    void create_savesNewSpecialty() {
        when(specialtyRepository.existsByCodeIgnoreCase("CARDIO")).thenReturn(false);
        when(specialtyRepository.save(any(Specialty.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SpecialtyResponse result = service.create(new CreateSpecialtyRequest("CARDIO", "Cardiology"));

        assertThat(result.code()).isEqualTo("CARDIO");
        assertThat(result.name()).isEqualTo("Cardiology");
        assertThat(result.active()).isTrue();
    }

    @Test
    void findAll_returnsEverySpecialty() {
        Specialty cardio = new Specialty("CARDIO", "Cardiology");
        Specialty peds = new Specialty("PEDS", "Pediatrics");
        when(specialtyRepository.findAll()).thenReturn(List.of(cardio, peds));

        List<SpecialtyResponse> result = service.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).code()).isEqualTo("CARDIO");
        assertThat(result.get(1).code()).isEqualTo("PEDS");
    }
}