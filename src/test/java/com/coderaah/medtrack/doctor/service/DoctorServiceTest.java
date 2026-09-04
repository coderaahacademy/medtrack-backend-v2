package com.coderaah.medtrack.doctor.service;

import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.dto.DoctorPersonRequest;
import com.coderaah.medtrack.doctor.dto.DoctorRequest;
import com.coderaah.medtrack.doctor.dto.DoctorResponse;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.doctor.exception.DuplicateLicenseNumberException;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
import com.coderaah.medtrack.identity.domain.Person;
import com.coderaah.medtrack.identity.repository.PersonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {

    @Mock
    private DoctorProfileRepository doctorProfileRepository;
    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private DoctorService doctorService;


    @Test
    void getDoctorById_shouldReturnDoctor_whenDoctorExists() {
        // Arrange
        DoctorProfile doctor = new DoctorProfile();
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(doctor));

        // Act
        DoctorProfile result = doctorService.getDoctorById(1L);

        // Assert
        assertSame(doctor, result);
    }


    @Test
    void getDoctorById_shouldThrowException_whenDoctorDoesNotExist() {
        // Arrange
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(DoctorNotFoundException.class, () -> doctorService.getDoctorById(1L));
    }

    @Test
    void validateLicenseNumber_shouldThrowException_whenLicenseNumberAlreadyExists() {

        // Arrange
        when(doctorProfileRepository.existsByLicenseNumber("Lic-No-123")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateLicenseNumberException.class, () -> doctorService.validateLicenseNumber("Lic-No-123"));
    }

    @Test
    void validateLicenseNumber_shouldNotThrowException_whenLicenseNumberDoesNotExist() {

        // Arrange
        when(doctorProfileRepository.existsByLicenseNumber("Lic-No-123")).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> doctorService.validateLicenseNumber("Lic-No-123"));
    }

    @Test
    void updateDoctorStatus_shouldUpdateActiveStatus_whenDoctorExists() {

        // Arrange
        Person person = new Person();
        person.setFirstName("Nastaran");
        person.setLastName("Seife");

        DoctorProfile doctor = new DoctorProfile();
        doctor.setPerson(person);
        doctor.setActive(true);

        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(doctor));

        when(doctorProfileRepository.save(doctor)).thenReturn(doctor);

        // Act
        DoctorResponse response = doctorService.updateDoctorStatus(1L, false);

        // Assert
        assertFalse(response.isActive());
        verify(doctorProfileRepository).save(doctor);
    }

    @Test
    void updateDoctor_shouldUpdateProfile_whenLicenseNumberIsUnchanged() {

        // Arrange
        Person person = new Person();
        person.setFirstName("Nastaran");
        person.setLastName("Seife");

        DoctorProfile doctor = new DoctorProfile();
        doctor.setLicenseNumber("Lic-No-123");

        doctor.setPerson(person);

        DoctorRequest request = new DoctorRequest();
        request.setLicenseNumber("Lic-No-123");
        request.setProfessionalPhone("+982144807811");
        request.setTimeZone("Iran/Tehran");

        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(doctor));

        when(doctorProfileRepository.save(doctor)).thenReturn(doctor);

        // Act
        DoctorResponse response = doctorService.updateDoctor(1L, request);

        // Assert
        assertEquals("+982144807811", response.getProfessionalPhone());
        assertEquals("Iran/Tehran", response.getTimeZone());
        assertEquals("Lic-No-123", response.getLicenseNumber());
    }

    @Test
    void registerDoctor_shouldCreateDoctorForExistingPerson() {

        // Arrange
        Person person = new Person();
        person.setFirstName("Nastaran");
        person.setLastName("Seife");

        DoctorRequest request = new DoctorRequest();
        request.setPersonId(1L);
        request.setLicenseNumber("Lic-No-123");
        request.setProfessionalPhone("+982144807811");
        request.setTimeZone("Iran/Tehran");

        when(doctorProfileRepository.existsByLicenseNumber("Lic-No-123"))
                .thenReturn(false);

        when(personRepository.findById(1L))
                .thenReturn(Optional.of(person));

        when(doctorProfileRepository.save(any(DoctorProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DoctorResponse response = doctorService.registerDoctor(request);

        // Assert
        assertEquals("Lic-No-123", response.getLicenseNumber());
        assertEquals("Nastaran", response.getFirstName());
        assertEquals("Seife", response.getLastName());

        verify(personRepository, never()).save(any(Person.class));
        verify(doctorProfileRepository).save(any(DoctorProfile.class));
    }


    @Test
    void registerDoctor_shouldCreateDoctorWithNewPerson() {

        // Arrange
        DoctorPersonRequest personRequest = new DoctorPersonRequest();
        personRequest.setFirstName("Nastaran");
        personRequest.setLastName("Seife");
        personRequest.setPhone("+49123456789");

        DoctorRequest request = new DoctorRequest();
        request.setPerson(personRequest);
        request.setLicenseNumber("Lic-No-123");
        request.setProfessionalPhone("+982144807811");
        request.setTimeZone("Iran/Tehran");

        when(doctorProfileRepository.existsByLicenseNumber("Lic-No-123"))
                .thenReturn(false);

        when(personRepository.save(any(Person.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(doctorProfileRepository.save(any(DoctorProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DoctorResponse response = doctorService.registerDoctor(request);

        // Assert
        assertEquals("Nastaran", response.getFirstName());
        assertEquals("Seife", response.getLastName());
        assertEquals("Lic-No-123", response.getLicenseNumber());
        assertEquals("+982144807811", response.getProfessionalPhone());
        assertEquals("Iran/Tehran", response.getTimeZone());

        verify(personRepository).save(any(Person.class));
        verify(doctorProfileRepository).save(any(DoctorProfile.class));
    }
}
