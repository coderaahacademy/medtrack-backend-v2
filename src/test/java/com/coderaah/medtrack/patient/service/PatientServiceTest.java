package com.coderaah.medtrack.patient.service;

import com.coderaah.medtrack.identity.domain.Person;
import com.coderaah.medtrack.identity.repository.PersonRepository;
import com.coderaah.medtrack.patient.domain.PatientProfile;
import com.coderaah.medtrack.patient.dto.PatientRequest;
import com.coderaah.medtrack.patient.dto.PatientResponse;
import com.coderaah.medtrack.patient.exception.DuplicateMedicalRecordNumberException;
import com.coderaah.medtrack.patient.exception.PatientNotFoundException;
import com.coderaah.medtrack.patient.repository.PatientProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class PatientServiceTest {

    private PersonRepository personRepository;
    private PatientProfileRepository patientProfileRepository;
    private PatientService patientService;

    @BeforeEach
    void setUp(){
        personRepository=mock(PersonRepository.class);
        patientProfileRepository=mock(PatientProfileRepository.class);

        patientService= new PatientService(personRepository,patientProfileRepository);

    }

    @Test
    void getPatientById_returnsPatient(){
        Person person= new Person(
                "John",
                "Smith",
                LocalDate.of(1995,2,10),
                "5551234567"
        );

        PatientProfile patient=new PatientProfile(
                person,
                "MRN-001"
        );

        when(patientProfileRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        PatientResponse result= patientService.getPatientById(1L);

        assertEquals("John",result.getFirstName());
        assertEquals("Smith",result.getLastName());
        assertEquals("MRN-001",result.getMedicalRecordNumber());

    }

    @Test
    void getPatientById_throwsException_whenPatientNotFound(){

        when(patientProfileRepository.findById(999L))
                .thenReturn(Optional.empty());

        PatientNotFoundException exception= assertThrows(PatientNotFoundException.class, ()->patientService.getPatientById(999L));

        assertEquals("Patient not found",exception.getMessage());
    }

    @Test
    void getAllPatients_returnsPatients(){
        Person person1 = new Person("John","Smith",LocalDate.of(1995,2,10),"5551234567");

        PatientProfile patient1= new PatientProfile(person1,"MRN-001");

        Person person2= new Person("Sara","Jones",LocalDate.of(2005,5,30),"5557777777");

        PatientProfile patient2= new PatientProfile(person2,"MRN-002");

        when(patientProfileRepository.findAll())
                .thenReturn(List.of(patient1,patient2));

        List<PatientResponse> result= patientService.getAllPatients();

        assertEquals(2,result.size());
        assertEquals("John",result.get(0).getFirstName());
        assertEquals("Smith",result.get(0).getLastName());
        assertEquals("MRN-001", result.get(0).getMedicalRecordNumber());

        assertEquals("Sara",result.get(1).getFirstName());
        assertEquals("Jones",result.get(1).getLastName());
        assertEquals("MRN-002", result.get(1).getMedicalRecordNumber());
    }

    @Test
    void createPatient_returnsCreatedPatient(){

        PatientRequest request= new PatientRequest();
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setBirthDate(LocalDate.of(1995,2,10));
        request.setPhone("5551234567");
        request.setMedicalRecordNumber("MRN-001");

        when(patientProfileRepository.existsByMedicalRecordNumber("MRN-001"))
                .thenReturn(false);

        when(personRepository.save(any(Person.class)))
                .thenAnswer(invocation-> invocation.getArgument(0));

        when(patientProfileRepository.save(any(PatientProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PatientResponse result=patientService.createPatient(request);

        assertEquals("John", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("MRN-001", result.getMedicalRecordNumber());

        verify(personRepository).save(any(Person.class));
        verify(patientProfileRepository).save(any(PatientProfile.class));
    }

    @Test
    void createPatient_throwsException_whenMedicalRecordNumberExists(){

        PatientRequest request= new PatientRequest();
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setMedicalRecordNumber("MRN-001");

        when(patientProfileRepository.existsByMedicalRecordNumber("MRN-001"))
                .thenReturn(true);

        DuplicateMedicalRecordNumberException exception=assertThrows(DuplicateMedicalRecordNumberException.class,()-> patientService.createPatient(request));

        assertEquals("Medical record number already exists",exception.getMessage());
    }

    @Test
    void updatePatient_returnsUpdatedPatient(){

        Person person = new Person("John","Smith",LocalDate.of(1995,2,10),"5551234567");

        PatientProfile patient= new PatientProfile(person,"MRN-001");

        PatientRequest updateRequest= new PatientRequest();
        updateRequest.setFirstName("John");
        updateRequest.setLastName("Updated");
        updateRequest.setBirthDate(LocalDate.of(1995, 2, 10));
        updateRequest.setPhone("5559999999");
        updateRequest.setMedicalRecordNumber("MRN-001");

        when(patientProfileRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientProfileRepository.existsByMedicalRecordNumberAndIdNot(
                "MRN-001",
                1L
        )).thenReturn(false);

        when(personRepository.save(any(Person.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(patientProfileRepository.save(any(PatientProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PatientResponse result =
                patientService.updatePatient(1L, updateRequest);

        assertEquals("John", result.getFirstName());
        assertEquals("Updated", result.getLastName());
        assertEquals("5559999999", result.getPhone());
        assertEquals("MRN-001", result.getMedicalRecordNumber());

        verify(personRepository).save(any(Person.class));
        verify(patientProfileRepository).save(any(PatientProfile.class));

    }

    @Test
    void updatePatient_throwsException_whenPatientDoesNotExist(){
        PatientRequest updateRequest = new PatientRequest();
        updateRequest.setFirstName("John");
        updateRequest.setLastName("Smith");
        updateRequest.setMedicalRecordNumber("MRN-001");

        when(patientProfileRepository.findById(99999L))
                .thenReturn(Optional.empty());

        PatientNotFoundException exception = assertThrows(
                PatientNotFoundException.class,
                () -> patientService.updatePatient(99999L, updateRequest)
        );

        assertEquals("Patient not found", exception.getMessage());
    }

    @Test
    void updatePatient_throwsException_whenMedicalRecordNumberExists(){
        Person person = new Person(
                "John",
                "Smith",
                LocalDate.of(1995, 5, 10),
                "5551234567"
        );

        PatientProfile patient = new PatientProfile(
                person,
                "MRN-001"
        );

        PatientRequest updateRequest = new PatientRequest();
        updateRequest.setFirstName("John");
        updateRequest.setLastName("Smith");
        updateRequest.setMedicalRecordNumber("MRN-002");

        when(patientProfileRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientProfileRepository.existsByMedicalRecordNumberAndIdNot(
                "MRN-002",
                1L
        )).thenReturn(true);

        DuplicateMedicalRecordNumberException exception = assertThrows(
                DuplicateMedicalRecordNumberException.class,
                () -> patientService.updatePatient(1L, updateRequest)
        );

        assertEquals("Medical record number already exists", exception.getMessage());
    }

}
