package com.ruoyi.emr.service;

import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.emr.domain.MedicalRecord;
import com.ruoyi.emr.domain.vo.AiDetectResponseVo;

public interface IAiImageAnalysisService
{
    AiDetectResponseVo analyze(Long imageId);

    MedicalRecord getGeneratedRecord(Long imageId);

    /**
     * 将用户绘制的标注图写入 MinIO，覆盖与 AI 分析相同的 {@code result/文件名} 对象；
     * 并同步 {@code chest_xray.ai_result_path}、病历 {@code ai_result_path}（若存在病历行）。
     */
    String uploadUserOverlay(Long imageId, MultipartFile file);
}
