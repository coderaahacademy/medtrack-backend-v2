package com.coderaah.medtrack.doctor.service;

import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.domain.DoctorScheduleException;
import com.coderaah.medtrack.doctor.dto.DoctorScheduleExceptionRequest;
import com.coderaah.medtrack.doctor.dto.DoctorScheduleExceptionResponse;
import com.coderaah.medtrack.doctor.exception.CannotCancelPastScheduleException;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.doctor.exception.InvalidScheduleTimeRangeException;
import com.coderaah.medtrack.doctor.exception.ScheduleExceptionNotFoundException;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
import com.coderaah.medtrack.doctor.repository.DoctorScheduleExceptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DoctorScheduleExceptionService {

    private final DoctorScheduleExceptionRepository exceptionRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    public DoctorScheduleExceptionService(
            DoctorScheduleExceptionRepository exceptionRepository,
            DoctorProfileRepository doctorProfileRepository) {
        this.exceptionRepository = exceptionRepository;
        this.doctorProfileRepository = doctorProfileRepository;
    }

    public DoctorScheduleExceptionResponse createException(
            Long doctorId,
            DoctorScheduleExceptionRequest request) {

        validateTimeRange(request);

        DoctorProfile doctor = getDoctor(doctorId);

        DoctorScheduleException exception =
                new DoctorScheduleException(
                        doctor,
                        request.startsAt(),
                        request.endsAt(),
                        request.exceptionType(),
                        request.reasonType(),
                        request.reason()
                );

        DoctorScheduleException saved =
                exceptionRepository.save(exception);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DoctorScheduleExceptionResponse> getExceptions(
            Long doctorId) {

        getDoctor(doctorId);

        return exceptionRepository
                .findByDoctorIdOrderByStartsAtAsc(doctorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void cancelException(
            Long doctorId,
            Long exceptionId) {

        getDoctor(doctorId);

        DoctorScheduleException exception =
                exceptionRepository
                        .findByIdAndDoctorId(exceptionId, doctorId)
                        .orElseThrow(() ->
                                new ScheduleExceptionNotFoundException(
                                        "Schedule exception not found"));

        if (!exception.getStartsAt().isAfter(LocalDateTime.now())) {
            throw new CannotCancelPastScheduleException(
                    "Only future schedule exceptions can be cancelled");
        }

        exceptionRepository.delete(exception);
    }

    private DoctorProfile getDoctor(Long doctorId) {

        return doctorProfileRepository.findById(doctorId)
                .orElseThrow(() ->
                        new DoctorNotFoundException("Doctor not found"));
    }

    private void validateTimeRange(
            DoctorScheduleExceptionRequest request) {

        if (!request.startsAt().isBefore(request.endsAt())) {
            throw new InvalidScheduleTimeRangeException(
                    "startsAt must be before endsAt");
        }
    }

    private DoctorScheduleExceptionResponse toResponse(
            DoctorScheduleException exception) {

        return new DoctorScheduleExceptionResponse(
                exception.getId(),
                exception.getDoctor().getId(),
                exception.getStartsAt(),
                exception.getEndsAt(),
                exception.getExceptionType(),
                exception.getReasonType(),
                exception.getReason()
        );
    }
}