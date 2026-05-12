package com.ruoyi.emr.service;

import com.ruoyi.emr.domain.dto.FusionAnalyzeRequest;
import com.ruoyi.emr.domain.dto.FusionExportRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

public interface IFusionReportService
{
    Map<String, Object> fusionAnalyze(Long imageId, FusionAnalyzeRequest body);

    void exportFusionWord(Long imageId, FusionExportRequest request, HttpServletResponse response);
}
