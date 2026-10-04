package com.coderaah.medtrack.appointment.service;

import com.coderaah.medtrack.appointment.domain.Appointment;
import com.coderaah.medtrack.appointment.domain.AppointmentStatus;
import com.coderaah.medtrack.appointment.domain.AppointmentStatusHistory;
import com.coderaah.medtrack.appointment.dto.AppointmentStatusHistoryResponse;
import com.coderaah.medtrack.appointment.mapper.AppointmentStatusHistoryMapper;
import com.coderaah.medtrack.appointment.repository.AppointmentStatusHistoryRepository;
import com.coderaah.medtrack.identity.domain.UserAccount;
import com.coderaah.medtrack.identity.exception.UserAccountNotFoundException;
import com.coderaah.medtrack.identity.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    @Test
    void record_savesHistoryWithOldStatusNewStatusReasonAndActor() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        UserAccount actor = new UserAccount();
        when(userAccountRepository.findById(9L)).thenReturn(Optional.of(actor));
        when(historyRepository.save(any(AppointmentStatusHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        historyService.record(appointment, AppointmentStatus.CONFIRMED, AppointmentStatus.CANCELLED, 9L, "No longer needed");

        ArgumentCaptor<AppointmentStatusHistory> captor = ArgumentCaptor.forClass(AppointmentStatusHistory.class);
        verify(historyRepository).save(captor.capture());
        AppointmentStatusHistory saved = captor.getValue();
        assertThat(saved.getAppointment()).isSameAs(appointment);
        assertThat(saved.getOldStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(saved.getNewStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(saved.getChangedByUser()).isSameAs(actor);
        assertThat(saved.getReason()).isEqualTo("No longer needed");
    }

    @Test
    void record_allowsNullReason() {
        when(userAccountRepository.findById(9L)).thenReturn(Optional.of(new UserAccount()));
        when(historyRepository.save(any(AppointmentStatusHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentStatusHistory saved = historyService.record(
                new Appointment(), AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED, 9L, null);

        assertThat(saved.getReason()).isNull();
    }

    @Test
    void record_throwsUserAccountNotFound_whenActorDoesNotExist() {
        when(userAccountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> historyService.record(
                new Appointment(), AppointmentStatus.SCHEDULED, AppointmentStatus.CANCELLED, 99L, "reason"))
                .isInstanceOf(UserAccountNotFoundException.class);

        verify(historyRepository, never()).save(any());
    }

    @Test
    void getHistoryForAppointment_returnsMappedRecordsInRepositoryOrder() {
        AppointmentStatusHistory first = new AppointmentStatusHistory();
        AppointmentStatusHistory second = new AppointmentStatusHistory();
        AppointmentStatusHistoryResponse firstResponse = new AppointmentStatusHistoryResponse();
        AppointmentStatusHistoryResponse secondResponse = new AppointmentStatusHistoryResponse();
        when(historyRepository.findByAppointmentIdOrderByChangedAtAsc(5L)).thenReturn(List.of(first, second));
        when(historyMapper.toResponse(first)).thenReturn(firstResponse);
        when(historyMapper.toResponse(second)).thenReturn(secondResponse);

        assertThat(historyService.getHistoryForAppointment(5L)).containsExactly(firstResponse, secondResponse);
    }

    @Test
    void getHistoryForAppointment_returnsEmptyList_whenNoHistoryExists() {
        when(historyRepository.findByAppointmentIdOrderByChangedAtAsc(6L)).thenReturn(List.of());

        assertThat(historyService.getHistoryForAppointment(6L)).isEmpty();
    }
}
