package com.coderaah.medtrack.pharmacy.service;

import com.coderaah.medtrack.pharmacy.domain.Pharmacy;
import com.coderaah.medtrack.pharmacy.dto.PharmacyRequest;
import com.coderaah.medtrack.pharmacy.dto.PharmacyResponse;
import com.coderaah.medtrack.pharmacy.exception.DuplicatePharmacyLicenseNumberException;
import com.coderaah.medtrack.pharmacy.exception.PharmacyNotFoundException;
import com.coderaah.medtrack.pharmacy.repository.PharmacyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PharmacyServiceTest {

    private PharmacyRepository pharmacyRepository;
    private PharmacyService pharmacyService;

    @BeforeEach
    void setUp() {
        pharmacyRepository = mock(PharmacyRepository.class);
        pharmacyService = new PharmacyService(pharmacyRepository);
    }

    private PharmacyRequest validRequest() {
        PharmacyRequest request = new PharmacyRequest();
        request.setName("Apotheke Nord");
        request.setLicenseNumber("L-123");
        request.setCity("Hamburg");
        return request;
    }

    @Test
    void createPharmacy_returnsCreatedPharmacy() {

        when(pharmacyRepository.existsByLicenseNumber("L-123")).thenReturn(false);
        when(pharmacyRepository.save(any(Pharmacy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PharmacyResponse result = pharmacyService.createPharmacy(validRequest());

        assertEquals("Apotheke Nord", result.getName());
        assertEquals("L-123", result.getLicenseNumber());
        assertEquals("Hamburg", result.getCity());
        assertTrue(result.isActive());
        verify(pharmacyRepository).save(any(Pharmacy.class));
    }

    @Test
    void createPharmacy_throwsException_whenLicenseNumberExists() {

        when(pharmacyRepository.existsByLicenseNumber("L-123")).thenReturn(true);

        assertThrows(DuplicatePharmacyLicenseNumberException.class,
                () -> pharmacyService.createPharmacy(validRequest()));

        verify(pharmacyRepository, never()).save(any(Pharmacy.class));
    }

    @Test
    void getPharmacyById_returnsPharmacy() {

        Pharmacy pharmacy = new Pharmacy("Apotheke Nord", "L-123");
        when(pharmacyRepository.findById(1L)).thenReturn(Optional.of(pharmacy));

        PharmacyResponse result = pharmacyService.getPharmacyById(1L);

        assertEquals("Apotheke Nord", result.getName());
    }

    @Test
    void getPharmacyById_throwsException_whenNotFound() {

        when(pharmacyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(PharmacyNotFoundException.class,
                () -> pharmacyService.getPharmacyById(999L));
    }

    @Test
    void updatePharmacy_updatesFields() {

        Pharmacy pharmacy = new Pharmacy("Alter Name", "L-123");
        when(pharmacyRepository.findById(1L)).thenReturn(Optional.of(pharmacy));
        when(pharmacyRepository.existsByLicenseNumberAndIdNot("L-123", 1L)).thenReturn(false);
        when(pharmacyRepository.save(any(Pharmacy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PharmacyResponse result = pharmacyService.updatePharmacy(1L, validRequest());

        assertEquals("Apotheke Nord", result.getName());
        assertEquals("Hamburg", result.getCity());
    }

    @Test
    void updatePharmacy_throwsException_whenLicenseNumberUsedByOtherPharmacy() {

        Pharmacy pharmacy = new Pharmacy("Apotheke Nord", "L-999");
        when(pharmacyRepository.findById(1L)).thenReturn(Optional.of(pharmacy));
        when(pharmacyRepository.existsByLicenseNumberAndIdNot("L-123", 1L)).thenReturn(true);

        assertThrows(DuplicatePharmacyLicenseNumberException.class,
                () -> pharmacyService.updatePharmacy(1L, validRequest()));

        verify(pharmacyRepository, never()).save(any(Pharmacy.class));
    }

    @Test
    void updatePharmacyStatus_deactivatesWithoutDeleting() {

        Pharmacy pharmacy = new Pharmacy("Apotheke Nord", "L-123");
        when(pharmacyRepository.findById(1L)).thenReturn(Optional.of(pharmacy));
        when(pharmacyRepository.save(any(Pharmacy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PharmacyResponse result = pharmacyService.updatePharmacyStatus(1L, false);

        assertFalse(result.isActive());
        verify(pharmacyRepository, never()).delete(any(Pharmacy.class));
    }
}