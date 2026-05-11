package com.ruoyi.emr.mapper;

import org.apache.ibatis.annotations.Param;
import com.ruoyi.emr.domain.MedicalPatientDiagnosis;

/**
 * medical_patient_diagnosis.
 */
public interface MedicalPatientDiagnosisMapper
{
    MedicalPatientDiagnosis selectByPatientId(@Param("patientId") Long patientId);

    int insert(MedicalPatientDiagnosis row);

    int updateMultimodalByPatientId(@Param("patientId") Long patientId);

    int updateDiagnosisAfterAi(@Param("patientId") Long patientId);

    int updateReportExported(@Param("patientId") Long patientId);

    java.util.Date selectFirstRecordTime(@Param("patientId") Long patientId);

    java.util.Date selectFirstLabTime(@Param("patientId") Long patientId);
}
