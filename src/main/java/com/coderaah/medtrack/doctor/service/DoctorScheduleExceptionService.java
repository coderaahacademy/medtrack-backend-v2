package com.coderaah.medtrack.doctor.service;

import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.domain.DoctorScheduleException;
import com.coderaah.medtrack.doctor.dto.DoctorScheduleExceptionRequest;
import com.coderaah.medtrack.doctor.dto.DoctorScheduleExceptionResponse;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
import com.coderaah.medtrack.doctor.repository.DoctorScheduleExceptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private DoctorProfile getDoctor(Long doctorId) {
        return doctorProfileRepository.findById(doctorId)
                .orElseThrow(() ->
                        new RuntimeException("Doctor not found"));
    }

    private void validateTimeRange(
            DoctorScheduleExceptionRequest request) {

        if (!request.startsAt().isBefore(request.endsAt())) {
            throw new IllegalArgumentException(
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