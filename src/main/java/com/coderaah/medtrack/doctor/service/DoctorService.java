package com.coderaah.medtrack.doctor.service;

import com.coderaah.medtrack.doctor.domain.DoctorProfile;
import com.coderaah.medtrack.doctor.dto.DoctorPersonRequest;
import com.coderaah.medtrack.doctor.dto.DoctorRequest;
import com.coderaah.medtrack.doctor.dto.DoctorResponse;
import com.coderaah.medtrack.doctor.exception.DoctorNotFoundException;
import com.coderaah.medtrack.doctor.exception.DoctorPersonNotFoundException;
import com.coderaah.medtrack.doctor.exception.DuplicateLicenseNumberException;
import com.coderaah.medtrack.doctor.exception.InvalidDoctorRequestException;
import com.coderaah.medtrack.doctor.repository.DoctorProfileRepository;
import com.coderaah.medtrack.identity.domain.Person;
import com.coderaah.medtrack.identity.repository.PersonRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final PersonRepository personRepository;


    public DoctorService(DoctorProfileRepository doctorProfileRepository, PersonRepository personRepository) {
        this.doctorProfileRepository = doctorProfileRepository;
        this.personRepository = personRepository;
    }

    public void validateLicenseNumber(String licenseNumber) {

        if (doctorProfileRepository.existsByLicenseNumber(licenseNumber)) {
            throw new DuplicateLicenseNumberException("License number " + licenseNumber + " already exists");

        }
    }

    public DoctorProfile getDoctorById(Long id) {
        return doctorProfileRepository.findById(id).orElseThrow(() -> new DoctorNotFoundException("Doctor with id " + id + " not found"));
    }

    public List<DoctorResponse> getAllDoctors() {
        return doctorProfileRepository.findAll().stream().map(this::toResponse).toList();
    }

    private DoctorResponse toResponse(DoctorProfile doctor) {
        DoctorResponse doctorResponse = new DoctorResponse();
        doctorResponse.setId(doctor.getId());
        doctorResponse.setPersonId(doctor.getPerson().getId());
        doctorResponse.setFirstName(doctor.getPerson().getFirstName());
        doctorResponse.setLastName(doctor.getPerson().getLastName());
        doctorResponse.setLicenseNumber(doctor.getLicenseNumber());
        doctorResponse.setProfessionalPhone(doctor.getProfessionalPhone());
        doctorResponse.setTimeZone(doctor.getTimeZone());
        doctorResponse.setActive(doctor.isActive());
        return doctorResponse;
    }

    public DoctorResponse updateDoctorStatus(Long id, boolean active) {
        DoctorProfile doctor = getDoctorById(id);
        doctor.setActive(active);
        DoctorProfile savedDoctor = doctorProfileRepository.save(doctor);
        return toResponse(savedDoctor);

    }

    public DoctorResponse updateDoctor(Long id, DoctorRequest doctorRequest) {
        DoctorProfile doctorProfile = getDoctorById(id);

        if (!doctorProfile.getLicenseNumber().equals(doctorRequest.getLicenseNumber())) {
            validateLicenseNumber(doctorRequest.getLicenseNumber());
        }
        doctorProfile.setLicenseNumber(doctorRequest.getLicenseNumber());
        doctorProfile.setTimeZone(doctorRequest.getTimeZone());
        doctorProfile.setProfessionalPhone(doctorRequest.getProfessionalPhone());

        DoctorProfile savedDoctor = doctorProfileRepository.save(doctorProfile);
        return toResponse(savedDoctor);
    }


    public DoctorResponse getDoctorResponseById(Long id) {
        return toResponse(getDoctorById(id));
    }

    public DoctorResponse registerDoctor(DoctorRequest doctorRequest) {
        validateLicenseNumber(doctorRequest.getLicenseNumber());

        if (doctorRequest.getPersonId() != null
                && doctorRequest.getPerson() != null) {
            throw new InvalidDoctorRequestException(
                    "Only one of personId or person may be provided"
            );
        }
        Person person;

        if (doctorRequest.getPersonId() != null) {
            // Existing Person
            person = personRepository.findById(doctorRequest.getPersonId()).orElseThrow(()
                    -> new DoctorPersonNotFoundException("Person with id " + doctorRequest.getPersonId() + " not found"));
        } else {
            // New Person
            DoctorPersonRequest personRequest = doctorRequest.getPerson();

            if (personRequest == null) {
                throw new InvalidDoctorRequestException("Either personId or person must be provided");
            }

            person = new Person();
            person.setFirstName(personRequest.getFirstName());
            person.setLastName(personRequest.getLastName());
            person.setBirthDate(personRequest.getBirthDate());
            person.setPhone(personRequest.getPhone());

            person = personRepository.save(person);
        }

        DoctorProfile doctor = new DoctorProfile();
        doctor.setPerson(person);
        doctor.setLicenseNumber(doctorRequest.getLicenseNumber());
        doctor.setProfessionalPhone(doctorRequest.getProfessionalPhone());
        doctor.setTimeZone(doctorRequest.getTimeZone());

        DoctorProfile savedDoctor = doctorProfileRepository.save(doctor);

        return toResponse(savedDoctor);
    }
}
