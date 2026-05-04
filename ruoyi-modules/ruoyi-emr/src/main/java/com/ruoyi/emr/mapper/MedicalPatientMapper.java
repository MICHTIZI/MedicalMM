package com.ruoyi.emr.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.emr.domain.MedicalPatient;
import com.ruoyi.emr.domain.vo.DoctorOptionVo;

/**
 * Patient mapper.
 */
public interface MedicalPatientMapper
{
    List<MedicalPatient> selectMedicalPatientList(MedicalPatient patient);

    MedicalPatient selectMedicalPatientByPatientId(Long patientId);

    int insertMedicalPatient(MedicalPatient patient);

    int updateMedicalPatient(MedicalPatient patient);

    int deleteMedicalPatientByPatientId(Long patientId);

    int deleteMedicalPatientByPatientIds(Long[] patientIds);

    String selectDoctorNickNameById(@Param("userId") Long userId);

    List<DoctorOptionVo> selectDoctorOptions();
}
