package com.coderaah.medtrack.appointment.service;

import com.coderaah.medtrack.appointment.domain.Appointment;
import com.coderaah.medtrack.appointment.domain.AppointmentStatus;
import com.coderaah.medtrack.appointment.domain.AppointmentStatusHistory;
import com.coderaah.medtrack.appointment.dto.responeDto.AppointmentStatusHistoryResponeDto;
import com.coderaah.medtrack.appointment.mapper.AppointmentStatusHistoryMapper;
import com.coderaah.medtrack.appointment.repository.AppointmentStatusHistoryRepository;
import com.coderaah.medtrack.identity.domain.UserAccount;
import com.coderaah.medtrack.identity.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentStatusHistoryServiceTest {

    @Mock
    private AppointmentStatusHistoryRepository historyRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private AppointmentStatusHistoryMapper historyMapper;

    @InjectMocks
    private AppointmentStatusHistoryService historyService;


    // record


    @Test
    void record_savesHistory_withResolvedActor() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        UserAccount actor = new UserAccount();

        when(userAccountRepository.findById(9L)).thenReturn(Optional.of(actor));
        when(historyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentStatusHistory result = historyService.record(
                appointment, AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED, 9L, null);

        assertThat(result.getAppointment()).isEqualTo(appointment);
        assertThat(result.getOldStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(result.getNewStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(result.getChangedByUser()).isEqualTo(actor);

        verify(historyRepository).save(any(AppointmentStatusHistory.class));
    }

    @Test
    void record_throws_whenActorNotFound() {
        when(userAccountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> historyService.record(
                new Appointment(), AppointmentStatus.SCHEDULED, AppointmentStatus.CANCELLED, 99L, "reason"))
                .isInstanceOf(EntityNotFoundException.class);

        verify(historyRepository, org.mockito.Mockito.never()).save(any());
    }

    // getHistoryForAppointment



    @Test
    void getHistoryForAppointment_returnsMappedList() {
        AppointmentStatusHistory h1 = new AppointmentStatusHistory();
        AppointmentStatusHistory h2 = new AppointmentStatusHistory();

        when(historyRepository.findByAppointmentIdOrderByChangedAtAsc(5L))
                .thenReturn(List.of(h1, h2));
        when(historyMapper.convertToAppointmentStatusHistoryResponeDto(h1))
                .thenReturn(new AppointmentStatusHistoryResponeDto());
        when(historyMapper.convertToAppointmentStatusHistoryResponeDto(h2))
                .thenReturn(new AppointmentStatusHistoryResponeDto());

        List<AppointmentStatusHistoryResponeDto> result = historyService.getHistoryForAppointment(5L);

        assertThat(result).hasSize(2);
    }

    @Test
    void getHistoryForAppointment_returnsEmptyList_whenNoHistoryExists() {
        when(historyRepository.findByAppointmentIdOrderByChangedAtAsc(6L))
                .thenReturn(List.of());

        List<AppointmentStatusHistoryResponeDto> result = historyService.getHistoryForAppointment(6L);

        assertThat(result).isEmpty();
    }
}
