package com.ruoyi.emr.service;

import java.util.List;
import com.ruoyi.emr.domain.FusionAnalysisReport;
import com.ruoyi.emr.domain.dto.FusionReportDoctorUpdateDto;
import com.ruoyi.emr.domain.dto.FusionReportSaveAnalyzeDto;

public interface IFusionAnalysisReportService
{
    List<FusionAnalysisReport> selectFusionAnalysisReportList(FusionAnalysisReport query);

    FusionAnalysisReport selectFusionAnalysisReportById(Long reportId);

    /**
     * Cached report for viewer: same patient+image as chest_xray row.
     *
     * @return null if none
     */
    FusionAnalysisReport selectByImageIdForViewer(Long imageId);

    /**
     * Persist fusion output after successful fusion API call (insert or replace same patient+image).
     */
    FusionAnalysisReport saveAnalyzeResult(Long imageId, FusionReportSaveAnalyzeDto body);

    int updateDoctorFields(FusionReportDoctorUpdateDto dto);

    int updateFusionAnalysisReport(FusionAnalysisReport row);

    int deleteFusionAnalysisReportByIds(Long[] ids);
}
