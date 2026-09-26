package com.coderaah.medtrack.appointment.mapper;

import com.coderaah.medtrack.appointment.domain.Appointment;
import com.coderaah.medtrack.appointment.dto.requestDto.AppointmentRequestDto;
import com.coderaah.medtrack.appointment.dto.responeDto.AppointmentResponseDto;
import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.patient.domain.PatientProfile;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {
    public AppointmentMapper() {
    }

    public Appointment convertAppointmentRequestDtoToAppointment(
            AppointmentRequestDto appointmentRequestDto,
            PatientProfile patient,
            DoctorProfile doctor
    ) {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentType(appointmentRequestDto.getAppointmentType());
        appointment.setScheduledStart(appointmentRequestDto.getScheduledStart());
        appointment.setScheduledEnd(appointmentRequestDto.getScheduledEnd());
        appointment.setLocation(appointmentRequestDto.getLocation());
        appointment.setReason(appointmentRequestDto.getReason());
        return appointment;
    }

    public AppointmentResponseDto convertAppointmentToAppointmentResponseDto(Appointment appointment) {
        AppointmentResponseDto appointmentResponseDto = new AppointmentResponseDto();
        appointmentResponseDto.setId(appointment.getId());
        appointmentResponseDto.setPatientId(appointment.getPatient().getId());
        appointmentResponseDto.setDoctorId(appointment.getDoctor().getId());
        appointmentResponseDto.setAppointmentType(appointment.getAppointmentType());
        appointmentResponseDto.setScheduledStart(appointment.getScheduledStart());
        appointmentResponseDto.setScheduledEnd(appointment.getScheduledEnd());
        appointmentResponseDto.setLocation(appointment.getLocation());
        appointmentResponseDto.setReason(appointment.getReason());
        appointmentResponseDto.setStatus(appointment.getStatus());
        appointmentResponseDto.setCancellationReason(appointment.getCancellationReason());
        appointmentResponseDto.setCreatedAt(appointment.getCreatedAt());
        appointmentResponseDto.setUpdatedAt(appointment.getUpdatedAt());
        return appointmentResponseDto;

    }
}



