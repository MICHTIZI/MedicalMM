package com.ruoyi.emr.service;

import java.util.List;
import com.ruoyi.emr.domain.MedicalPatient;
import com.ruoyi.emr.domain.vo.DoctorOptionVo;
import com.ruoyi.emr.domain.vo.PatientCardVo;
import com.ruoyi.emr.domain.vo.PatientDiagnosisDetailVo;

/**
 * Patient service.
 */
public interface IMedicalPatientService
{
    List<MedicalPatient> selectMedicalPatientList(MedicalPatient patient);

    List<PatientCardVo> selectPatientCardList(MedicalPatient patient);

    PatientDiagnosisDetailVo selectPatientDiagnosisDetail(Long patientId);

    MedicalPatient selectMedicalPatientByPatientId(Long patientId);

    int insertMedicalPatient(MedicalPatient patient);

    int updateMedicalPatient(MedicalPatient patient);

    int deleteMedicalPatientByPatientIds(Long[] patientIds);

    int deleteMedicalPatientByPatientId(Long patientId);

    List<DoctorOptionVo> selectDoctorOptions();
}
