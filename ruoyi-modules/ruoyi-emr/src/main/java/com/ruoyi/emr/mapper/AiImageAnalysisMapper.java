package com.ruoyi.emr.mapper;

import org.apache.ibatis.annotations.Param;
import com.ruoyi.emr.domain.MedicalRecord;

/**
 * Mapper helpers for AI image analysis.
 */
public interface AiImageAnalysisMapper
{
    int updateXrayAiResult(@Param("id") Long id,
                           @Param("aiResultPath") String aiResultPath,
                           @Param("lesionCount") Integer lesionCount,
                           @Param("diagnosis") String diagnosis);

    MedicalRecord selectMedicalRecordByImageId(Long imageId);

    int insertGeneratedRecord(MedicalRecord record);

    int updateGeneratedRecord(MedicalRecord record);
}
