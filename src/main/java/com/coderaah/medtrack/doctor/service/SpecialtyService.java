package com.coderaah.medtrack.doctor.service;

import com.coderaah.medtrack.doctor.domain.Specialty;
import com.coderaah.medtrack.doctor.dto.CreateSpecialtyRequest;
import com.coderaah.medtrack.doctor.dto.SpecialtyResponse;
import com.coderaah.medtrack.doctor.exception.DuplicateSpecialtyCodeException;
import com.coderaah.medtrack.doctor.exception.DuplicateSpecialtyNameException;
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

    public SpecialtyResponse create(CreateSpecialtyRequest request) {
        if (specialtyRepository.existsByCodeIgnoreCase(request.code())) {
            throw new DuplicateSpecialtyCodeException("A specialty with code '" + request.code() + "' already exists");
        }
        if (specialtyRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateSpecialtyNameException("A specialty with name '" + request.name() + "' already exists");
        }
        Specialty saved = specialtyRepository.save(new Specialty(request.code(), request.name()));
        return SpecialtyResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<SpecialtyResponse> findAll() {
        return specialtyRepository.findAll().stream().map(SpecialtyResponse::from).toList();
    }
}