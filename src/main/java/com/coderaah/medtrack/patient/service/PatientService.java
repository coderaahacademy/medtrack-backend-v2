package com.coderaah.medtrack.patient.service;

import com.coderaah.medtrack.identity.domain.Person;
import com.coderaah.medtrack.identity.repository.PersonRepository;
import com.coderaah.medtrack.patient.domain.PatientProfile;
import com.coderaah.medtrack.patient.dto.PatientRequest;
import com.coderaah.medtrack.patient.dto.PatientResponse;
import com.coderaah.medtrack.patient.repository.PatientProfileRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class PatientService {

    private final PersonRepository personRepository;

    private final PatientProfileRepository patientProfileRepository;

    public PatientService(PersonRepository personRepository, PatientProfileRepository patientProfileRepository){
        this.personRepository=personRepository;
        this.patientProfileRepository=patientProfileRepository;
    }

    public PatientResponse createPatient(PatientRequest newPatient){

        if(patientProfileRepository.existsByMedicalRecordNumber(newPatient.getMedicalRecordNumber())){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Medical record number already exists."
            );
        }

        Person person= new Person(
                newPatient.getFirstName(),
                newPatient.getLastName(),
                newPatient.getBirthDate(),
                newPatient.getPhone()
        );
       Person savedPerson= personRepository.save(person);

        PatientProfile patientProfile=new PatientProfile(
          savedPerson,
          newPatient.getMedicalRecordNumber(),
          newPatient.getBloodType(),
          newPatient.getInsuranceProvider(),
          newPatient.getInsuranceNumber()
        );

        PatientProfile savedPatient=patientProfileRepository.save(patientProfile);


        return toResponse(savedPatient);
    }

    public PatientResponse getPatientById(Long id){

        PatientProfile patient= patientProfileRepository.findById(id)
                .orElseThrow(()->new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "patient not found"
                ));

        return toResponse(patient);
    }

    public List<PatientResponse> getAllPatients(){

        return patientProfileRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PatientResponse updatePatient(Long id, PatientRequest patientUpdate){

        PatientProfile patient=patientProfileRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "patient not found"
                ));

        if(patientProfileRepository.existsByMedicalRecordNumberAndIdNot(                 // Check whether another patient already uses this MRN
                patientUpdate.getMedicalRecordNumber(),
                id)){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Medical record number already exists"
            );
        }


        Person person= patient.getPerson();

        person.setFirstName(patientUpdate.getFirstName());
        person.setLastName(patientUpdate.getLastName());
        person.setBirthDate(patientUpdate.getBirthDate());
        person.setPhone(patientUpdate.getPhone());

        patient.setMedicalRecordNumber(patientUpdate.getMedicalRecordNumber());
        patient.setBloodType(patientUpdate.getBloodType());
        patient.setInsuranceProvider(patientUpdate.getInsuranceProvider());
        patient.setInsuranceNumber(patientUpdate.getInsuranceNumber());

        personRepository.save(person);
        PatientProfile updatedPatient = patientProfileRepository.save(patient);

        return toResponse(updatedPatient);

    }





    //helper method to convert to response//
    private PatientResponse toResponse(PatientProfile patient) {

        PatientResponse response = new PatientResponse();

        response.setId(patient.getId());
        response.setFirstName(patient.getPerson().getFirstName());
        response.setLastName(patient.getPerson().getLastName());
        response.setBirthDate(patient.getPerson().getBirthDate());
        response.setPhone(patient.getPerson().getPhone());

        response.setMedicalRecordNumber(patient.getMedicalRecordNumber());
        response.setBloodType(patient.getBloodType());
        response.setInsuranceProvider(patient.getInsuranceProvider());
        response.setInsuranceNumber(patient.getInsuranceNumber());

        return response;
    }


}
