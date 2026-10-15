package com.coderaah.medtrack.pharmacy.service;

import com.coderaah.medtrack.pharmacy.domain.Pharmacy;
import com.coderaah.medtrack.pharmacy.dto.PharmacyRequest;
import com.coderaah.medtrack.pharmacy.dto.PharmacyResponse;
import com.coderaah.medtrack.pharmacy.exception.DuplicatePharmacyLicenseNumberException;
import com.coderaah.medtrack.pharmacy.exception.PharmacyNotFoundException;
import com.coderaah.medtrack.pharmacy.repository.PharmacyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PharmacyService {

    private final PharmacyRepository pharmacyRepository;

    public PharmacyService(PharmacyRepository pharmacyRepository) {
        this.pharmacyRepository = pharmacyRepository;
    }

    public PharmacyResponse createPharmacy(PharmacyRequest request) {

        if (pharmacyRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new DuplicatePharmacyLicenseNumberException(
                    "Pharmacy with license number " + request.getLicenseNumber() + " already exists");
        }

        Pharmacy pharmacy = new Pharmacy();
        applyRequest(pharmacy, request);

        Pharmacy savedPharmacy = pharmacyRepository.save(pharmacy);

        return toResponse(savedPharmacy);
    }

    @Transactional(readOnly = true)
    public PharmacyResponse getPharmacyById(Long id) {
        return toResponse(findPharmacyOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<PharmacyResponse> listPharmacies() {
        return pharmacyRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PharmacyResponse updatePharmacy(Long id, PharmacyRequest request) {

        Pharmacy pharmacy = findPharmacyOrThrow(id);

        if (pharmacyRepository.existsByLicenseNumberAndIdNot(request.getLicenseNumber(), id)) {
            throw new DuplicatePharmacyLicenseNumberException(
                    "Pharmacy with license number " + request.getLicenseNumber() + " already exists");
        }

        applyRequest(pharmacy, request);

        Pharmacy updatedPharmacy = pharmacyRepository.save(pharmacy);

        return toResponse(updatedPharmacy);
    }

    public PharmacyResponse updatePharmacyStatus(Long id, boolean active) {

        Pharmacy pharmacy = findPharmacyOrThrow(id);

        pharmacy.setActive(active);

        Pharmacy updatedPharmacy = pharmacyRepository.save(pharmacy);

        return toResponse(updatedPharmacy);
    }

    private void applyRequest(Pharmacy pharmacy, PharmacyRequest request) {
        pharmacy.setName(request.getName());
        pharmacy.setLicenseNumber(request.getLicenseNumber());
        pharmacy.setPhone(request.getPhone());
        pharmacy.setEmail(request.getEmail());
        pharmacy.setStreet(request.getStreet());
        pharmacy.setCity(request.getCity());
        pharmacy.setPostalCode(request.getPostalCode());
        pharmacy.setCountry(request.getCountry());
    }

    private PharmacyResponse toResponse(Pharmacy pharmacy) {
        PharmacyResponse response = new PharmacyResponse();
        response.setId(pharmacy.getId());
        response.setName(pharmacy.getName());
        response.setLicenseNumber(pharmacy.getLicenseNumber());
        response.setPhone(pharmacy.getPhone());
        response.setEmail(pharmacy.getEmail());
        response.setStreet(pharmacy.getStreet());
        response.setCity(pharmacy.getCity());
        response.setPostalCode(pharmacy.getPostalCode());
        response.setCountry(pharmacy.getCountry());
        response.setActive(pharmacy.isActive());
        return response;
    }

    private Pharmacy findPharmacyOrThrow(Long id) {
        return pharmacyRepository.findById(id)
                .orElseThrow(() -> new PharmacyNotFoundException(
                        "Pharmacy with id " + id + " not found"));
    }
}