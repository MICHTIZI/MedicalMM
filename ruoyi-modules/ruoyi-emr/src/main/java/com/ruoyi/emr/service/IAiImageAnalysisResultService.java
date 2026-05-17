package com.ruoyi.emr.service;

import java.util.List;
import com.ruoyi.emr.domain.AiImageAnalysisResult;
import com.ruoyi.emr.domain.ChestXray;
import com.ruoyi.emr.domain.vo.AiDetectResponseVo;

public interface IAiImageAnalysisResultService
{
    List<AiImageAnalysisResult> selectAiImageAnalysisResultList(AiImageAnalysisResult query);

    AiImageAnalysisResult selectAiImageAnalysisResultById(Long analysisId);

    /**
     * Insert one analysis record after successful YOLO + report pipeline (no update).
     */
    AiImageAnalysisResult insertFromAnalyze(ChestXray xray, AiDetectResponseVo detect);

    int deleteAiImageAnalysisResultByIds(Long[] analysisIds);
}
