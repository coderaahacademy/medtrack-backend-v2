package com.coderaah.medtrack.doctor.controller;

import com.coderaah.medtrack.common.exception.GlobalExceptionHandler;
import com.coderaah.medtrack.doctor.dto.DoctorRequest;
import com.coderaah.medtrack.doctor.dto.DoctorResponse;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.doctor.exception.DuplicateLicenseNumberException;
import com.coderaah.medtrack.doctor.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DoctorController.class)
@Import(GlobalExceptionHandler.class)
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DoctorService doctorService;

    @Test
    void getDoctorById_shouldReturn404_whenDoctorDoesNotExist() throws Exception {

        // Arrange
        when(doctorService.getDoctorResponseById(99L))
                .thenThrow(new DoctorNotFoundException("Doctor with id 99 not found"));

        // Act & Assert
        mockMvc.perform(get("/api/doctors/99")).andExpect(status().isNotFound());
    }

    @Test
    void registerDoctor_shouldReturn400_whenLicenseNumberAlreadyExists() throws Exception {

        // Arrange
        when(doctorService.registerDoctor(any(DoctorRequest.class)))
                .thenThrow(new DuplicateLicenseNumberException("License number Lic-No-123 already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/doctors").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "personId": 1,
                  "licenseNumber": "Lic-No-123",
                  "professionalPhone": "+49123456789",
                  "timeZone": "Iran/Tehran"}
                """)).andExpect(status().isBadRequest());
    }

    @Test
    void getDoctorById_shouldReturnDoctorResponse_whenDoctorExists()
            throws Exception {

        // Arrange
        DoctorResponse response = new DoctorResponse();
        response.setId(1L);
        response.setPersonId(10L);
        response.setFirstName("Nastaran");
        response.setLastName("Seife");
        response.setLicenseNumber("Lic-No-123");
        response.setProfessionalPhone("+49123456789");
        response.setTimeZone("Iran/Tehran");
        response.setActive(true);

        when(doctorService.getDoctorResponseById(1L))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/doctors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.personId").value(10))
                .andExpect(jsonPath("$.firstName").value("Nastaran"))
                .andExpect(jsonPath("$.lastName").value("Seife"))
                .andExpect(jsonPath("$.licenseNumber").value("Lic-No-123"))
                .andExpect(jsonPath("$.professionalPhone").value("+49123456789"))
                .andExpect(jsonPath("$.timeZone").value("Iran/Tehran"))
                .andExpect(jsonPath("$.active").value(true));
    }
}