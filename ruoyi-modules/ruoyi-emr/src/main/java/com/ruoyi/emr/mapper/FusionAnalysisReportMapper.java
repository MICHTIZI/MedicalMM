package com.ruoyi.emr.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.emr.domain.FusionAnalysisReport;

public interface FusionAnalysisReportMapper
{
    List<FusionAnalysisReport> selectFusionAnalysisReportList(FusionAnalysisReport query);

    FusionAnalysisReport selectFusionAnalysisReportById(Long reportId);

    FusionAnalysisReport selectByPatientIdAndImageId(@Param("patientId") Long patientId, @Param("imageId") Long imageId);

    int insertFusionAnalysisReport(FusionAnalysisReport row);

    int updateFusionAnalysisReport(FusionAnalysisReport row);

    int logicalDeleteFusionAnalysisReportByIds(@Param("ids") Long[] ids);
}
