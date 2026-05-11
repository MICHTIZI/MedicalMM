package com.ruoyi.emr.service;

import java.util.Date;
import com.ruoyi.emr.domain.MedicalPatientDiagnosis;

/**
 * medical_patient_diagnosis lifecycle and binding refresh.
 */
public interface IMedicalPatientDiagnosisService
{
    void ensureInitialRow(Long patientId);

    void refreshMultimodal(Long patientId);

    void markAiCompleted(Long patientId);

    void markReportExported(Long patientId);

    MedicalPatientDiagnosis selectByPatientId(Long patientId);

    Date firstStructuredRecordTime(Long patientId);

    Date firstLabTestTime(Long patientId);
}
