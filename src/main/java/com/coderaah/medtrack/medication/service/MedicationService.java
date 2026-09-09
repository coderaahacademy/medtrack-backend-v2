package com.coderaah.medtrack.medication.service;


import com.coderaah.medtrack.medication.domain.Medication;
import com.coderaah.medtrack.medication.dto.MedicationResponse;
import com.coderaah.medtrack.medication.exception.MedicationNotFoundException;
import com.coderaah.medtrack.medication.repository.MedicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.coderaah.medtrack.medication.dto.MedicationRequest;

import java.util.List;


@Service
@Transactional
public class MedicationService {
    private final MedicationRepository medicationRepository;
    public MedicationService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    public MedicationResponse createMedication(MedicationRequest newMedication) {

        Medication medication = new Medication(
                newMedication.getGenericName(),
                newMedication.getBrandName(),
                newMedication.getStrength(),
                newMedication.getStrengthUnit(),
                newMedication.getDosageForm(),
                newMedication.getManufacturer()
        );

        Medication savedMedication = medicationRepository.save(medication);

        return toResponse(savedMedication);
    }
    private MedicationResponse toResponse(Medication medication) {

        MedicationResponse response = new MedicationResponse();

        response.setId(medication.getId());
        response.setGenericName(medication.getGenericName());
        response.setBrandName(medication.getBrandName());
        response.setStrength(medication.getStrength());
        response.setStrengthUnit(medication.getStrengthUnit());
        response.setDosageForm(medication.getDosageForm());
        response.setManufacturer(medication.getManufacturer());
        response.setActive(medication.isActive());

        return response;
    }
    public MedicationResponse getMedicationById(Long id) {

        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new MedicationNotFoundException("Medication not found"));

        return toResponse(medication);
    }
    public List<MedicationResponse> listMedications(Boolean active, String search) {

        List<Medication> medications;

        if (search != null && !search.isBlank()) {
            medications = medicationRepository.findByGenericNameContainingIgnoreCaseOrBrandNameContainingIgnoreCase(search, search);
        } else if (active != null) {
            medications = medicationRepository.findByActive(active);
        } else {
            medications = medicationRepository.findAll();
        }

        return medications.stream()
                .map(this::toResponse)
                .toList();
    }
    public MedicationResponse updateMedication(Long id, MedicationRequest medicationUpdate) {

        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new MedicationNotFoundException("Medication not found"));

        medication.setGenericName(medicationUpdate.getGenericName());
        medication.setBrandName(medicationUpdate.getBrandName());
        medication.setStrength(medicationUpdate.getStrength());
        medication.setStrengthUnit(medicationUpdate.getStrengthUnit());
        medication.setDosageForm(medicationUpdate.getDosageForm());
        medication.setManufacturer(medicationUpdate.getManufacturer());

        Medication updatedMedication = medicationRepository.save(medication);

        return toResponse(updatedMedication);
    }
    public MedicationResponse updateStatus(Long id, boolean active) {

        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new MedicationNotFoundException("Medication not found"));

        medication.setActive(active);

        Medication updatedMedication = medicationRepository.save(medication);

        return toResponse(updatedMedication);
    }

}
