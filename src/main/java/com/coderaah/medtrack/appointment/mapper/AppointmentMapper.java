package com.coderaah.medtrack.appointment.mapper;

import com.coderaah.medtrack.appointment.domain.Appointment;
import com.coderaah.medtrack.appointment.dto.AppointmentRequest;
import com.coderaah.medtrack.appointment.dto.AppointmentResponse;
import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.patient.domain.PatientProfile;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public Appointment toEntity(AppointmentRequest request, PatientProfile patient, DoctorProfile doctor) {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentType(request.getAppointmentType());
        appointment.setScheduledStart(request.getScheduledStart());
        appointment.setScheduledEnd(request.getScheduledEnd());
        appointment.setLocation(request.getLocation());
        appointment.setReason(request.getReason());
        return appointment;
    }

    public AppointmentResponse toResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setPatientId(appointment.getPatient().getId());
        response.setDoctorId(appointment.getDoctor().getId());
        response.setAppointmentType(appointment.getAppointmentType());
        response.setScheduledStart(appointment.getScheduledStart());
        response.setScheduledEnd(appointment.getScheduledEnd());
        response.setLocation(appointment.getLocation());
        response.setReason(appointment.getReason());
        response.setStatus(appointment.getStatus());
        response.setCancellationReason(appointment.getCancellationReason());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());
        return response;
    }
}
