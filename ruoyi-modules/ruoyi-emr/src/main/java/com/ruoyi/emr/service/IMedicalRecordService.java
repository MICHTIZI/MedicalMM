package com.ruoyi.emr.service;

import java.util.List;
import com.ruoyi.emr.domain.MedicalPatient;
import com.ruoyi.emr.domain.MedicalRecord;
import com.ruoyi.emr.domain.vo.XrayOptionVo;

/**
 * Structured medical record service.
 */
public interface IMedicalRecordService
{
    List<MedicalRecord> selectMedicalRecordList(MedicalRecord record);

    MedicalRecord selectMedicalRecordByRecordId(Long recordId);

    int insertMedicalRecord(MedicalRecord record);

    int updateMedicalRecord(MedicalRecord record);

    int deleteMedicalRecordByRecordIds(Long[] recordIds);

    int deleteMedicalRecordByRecordId(Long recordId);

    List<MedicalPatient> selectPatientOptions(MedicalPatient patient);

    List<XrayOptionVo> selectXrayOptions(String keyword);
}
