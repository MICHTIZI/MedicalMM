package com.ruoyi.emr.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.emr.domain.MedicalLabResult;

/**
 * medical_lab_results mapper.
 */
public interface MedicalLabResultMapper
{
    List<MedicalLabResult> selectMedicalLabResultList(MedicalLabResult query);

    MedicalLabResult selectMedicalLabResultById(Long id);

    int insertMedicalLabResult(MedicalLabResult row);

    int updateMedicalLabResult(MedicalLabResult row);

    int logicalDeleteMedicalLabResultByIds(@Param("ids") Long[] ids);
}
