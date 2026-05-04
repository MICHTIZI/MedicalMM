package com.ruoyi.emr.service;

import com.ruoyi.emr.domain.MedicalRecord;
import com.ruoyi.emr.domain.vo.AiDetectResponseVo;

public interface IAiImageAnalysisService
{
    AiDetectResponseVo analyze(Long imageId);

    MedicalRecord getGeneratedRecord(Long imageId);
}
