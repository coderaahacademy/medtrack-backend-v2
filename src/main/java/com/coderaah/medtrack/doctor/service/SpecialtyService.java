package com.coderaah.medtrack.doctor.service;

import com.coderaah.medtrack.common.exception.ConflictException;
import com.coderaah.medtrack.common.exception.ResourceNotFoundException;
import com.coderaah.medtrack.doctor.domain.Specialty;
import com.coderaah.medtrack.doctor.dto.CreateSpecialtyRequest;
import com.coderaah.medtrack.doctor.repository.SpecialtyRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional
public class SpecialtyService {
    private final SpecialtyRepository specialtyRepository;

    public SpecialtyService(SpecialtyRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }

    public Specialty create(CreateSpecialtyRequest request) {
        if (specialtyRepository.existsByCodeIgnoreCase(request.code())) {
            throw new ConflictException("A specialty with code '" + request.code() + "' already exists");
        }
        return specialtyRepository.save(new Specialty(request.code(), request.name()));
    }

    @Transactional(readOnly = true)
    public List<Specialty> findAll() {
        return specialtyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Specialty getById(Long id) {
        return specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty " + id + " not found"));
    }
}
