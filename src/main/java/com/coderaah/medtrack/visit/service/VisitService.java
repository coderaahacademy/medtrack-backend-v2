package com.coderaah.medtrack.visit.service;

import com.coderaah.medtrack.visit.dto.ClinicalNotesRequest;
import com.coderaah.medtrack.visit.dto.StartVisitRequest;
import com.coderaah.medtrack.visit.dto.VisitResponse;

import java.util.List;

public interface VisitService {

    VisitResponse startVisit(StartVisitRequest request);

    VisitResponse getVisitById(Long id);

    List<VisitResponse> getVisitsForPatient(Long patientId);

    List<VisitResponse> getVisitsForDoctor(Long doctorId);

    VisitResponse updateClinicalNotes(Long id, ClinicalNotesRequest request);

    VisitResponse completeVisit(Long id);

    VisitResponse cancelVisit(Long id);
}