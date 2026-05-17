package com.ruoyi.emr.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.emr.domain.AiImageAnalysisResult;

public interface AiImageAnalysisResultMapper
{
    List<AiImageAnalysisResult> selectAiImageAnalysisResultList(AiImageAnalysisResult query);

    AiImageAnalysisResult selectAiImageAnalysisResultById(Long analysisId);

    int insertAiImageAnalysisResult(AiImageAnalysisResult row);

    int logicalDeleteAiImageAnalysisResultByIds(@Param("ids") Long[] analysisIds);
}
