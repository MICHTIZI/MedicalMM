package com.ruoyi.emr.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.emr.domain.MedicalPatient;
import com.ruoyi.emr.domain.MedicalRecord;
import com.ruoyi.emr.domain.vo.XrayOptionVo;

/**
 * Structured medical record mapper.
 */
public interface MedicalRecordMapper
{
    List<MedicalRecord> selectMedicalRecordList(MedicalRecord record);

    MedicalRecord selectMedicalRecordByRecordId(Long recordId);

    int insertMedicalRecord(MedicalRecord record);

    int updateMedicalRecord(MedicalRecord record);

    int deleteMedicalRecordByRecordId(Long recordId);

    int deleteMedicalRecordByRecordIds(Long[] recordIds);

    MedicalPatient selectPatientById(Long patientId);

    List<MedicalPatient> selectPatientOptions(MedicalPatient patient);

    XrayOptionVo selectXrayById(Long imageId);

    List<XrayOptionVo> selectXrayOptions(@Param("keyword") String keyword);

    int countImageUsed(@Param("recordId") Long recordId, @Param("imageId") Long imageId);
}
