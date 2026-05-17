package com.ruoyi.emr.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.emr.domain.AiImageAnalysisResult;
import com.ruoyi.emr.domain.ChestXray;
import com.ruoyi.emr.domain.MedicalPatient;
import com.ruoyi.emr.domain.vo.AiDetectResponseVo;
import com.ruoyi.emr.domain.vo.DiagnosisReportStructVo;
import com.ruoyi.emr.mapper.AiImageAnalysisResultMapper;
import com.ruoyi.emr.mapper.MedicalPatientMapper;
import com.ruoyi.emr.service.IAiImageAnalysisResultService;
import com.ruoyi.emr.util.AiDetectSnapshotBuilder;
import com.ruoyi.emr.util.DiagnosisReportParser;

@Service
public class AiImageAnalysisResultServiceImpl implements IAiImageAnalysisResultService
{
    @Autowired
    private AiImageAnalysisResultMapper aiImageAnalysisResultMapper;

    @Autowired
    private MedicalPatientMapper medicalPatientMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<AiImageAnalysisResult> selectAiImageAnalysisResultList(AiImageAnalysisResult query)
    {
        if (!SecurityUtils.isAdmin())
        {
            query.setPatientAttendingDoctorIdScope(SecurityUtils.getUserId());
        }
        else
        {
            query.setPatientAttendingDoctorIdScope(null);
        }
        return aiImageAnalysisResultMapper.selectAiImageAnalysisResultList(query);
    }

    @Override
    public AiImageAnalysisResult selectAiImageAnalysisResultById(Long analysisId)
    {
        AiImageAnalysisResult row = aiImageAnalysisResultMapper.selectAiImageAnalysisResultById(analysisId);
        checkResultAccess(row);
        return row;
    }

    @Override
    public AiImageAnalysisResult insertFromAnalyze(ChestXray xray, AiDetectResponseVo detect)
    {
        if (xray == null || xray.getId() == null)
        {
            throw new ServiceException("\u5f71\u50cf\u4e0d\u5b58\u5728\uff0c\u65e0\u6cd5\u4fdd\u5b58\u5206\u6790\u7ed3\u679c");
        }
        if (detect == null)
        {
            throw new ServiceException("\u5206\u6790\u7ed3\u679c\u4e3a\u7a7a");
        }
        AiImageAnalysisResult row = new AiImageAnalysisResult();
        row.setImageId(xray.getId());
        row.setPatientId(xray.getPatientId());
        row.setLesionCount(detect.getLesionCount() != null ? detect.getLesionCount() : 0);
        if (detect.getInfectionRate() != null)
        {
            row.setInfectionRate(BigDecimal.valueOf(detect.getInfectionRate()));
        }
        if (detect.getTotalInfectionArea() != null)
        {
            row.setTotalInfectionArea(BigDecimal.valueOf(detect.getTotalInfectionArea()));
        }
        row.setDetectDiagnosis(detect.getDiagnosis());
        row.setAiResultPath(detect.getAiResultPath());
        row.setReportCreateTime(detect.getReportCreateTime());
        row.setDiagnosisReportRaw(detect.getDiagnosisReport());
        try
        {
            row.setDetectResultJson(objectMapper.writeValueAsString(AiDetectSnapshotBuilder.build(detect)));
            DiagnosisReportStructVo struct = DiagnosisReportParser.parse(detect.getDiagnosisReport());
            row.setDiagnosisReportJson(objectMapper.writeValueAsString(struct));
        }
        catch (JsonProcessingException e)
        {
            throw new ServiceException("\u5206\u6790\u7ed3\u679c\u5e8f\u5217\u5316\u5931\u8d25\uff1a" + e.getMessage());
        }
        row.setCreateBy(SecurityUtils.getUsername());
        row.setCreateTime(new Date());
        aiImageAnalysisResultMapper.insertAiImageAnalysisResult(row);
        return row;
    }

    @Override
    public int deleteAiImageAnalysisResultByIds(Long[] analysisIds)
    {
        if (analysisIds == null || analysisIds.length == 0)
        {
            return 0;
        }
        for (Long id : analysisIds)
        {
            AiImageAnalysisResult row = aiImageAnalysisResultMapper.selectAiImageAnalysisResultById(id);
            checkResultAccess(row);
        }
        return aiImageAnalysisResultMapper.logicalDeleteAiImageAnalysisResultByIds(analysisIds);
    }

    private void checkResultAccess(AiImageAnalysisResult row)
    {
        if (row == null)
        {
            throw new ServiceException("\u5206\u6790\u8bb0\u5f55\u4e0d\u5b58\u5728");
        }
        if (SecurityUtils.isAdmin() || row.getPatientId() == null)
        {
            return;
        }
        Long userId = SecurityUtils.getUserId();
        if (userId == null)
        {
            throw new ServiceException("\u65e0\u8bbf\u95ee\u6743\u9650");
        }
        MedicalPatient patient = medicalPatientMapper.selectMedicalPatientByPatientId(row.getPatientId());
        if (patient == null || patient.getAttendingDoctorId() == null || !patient.getAttendingDoctorId().equals(userId))
        {
            throw new ServiceException("\u65e0\u6743\u8bbf\u95ee\u5176\u4ed6\u4e3b\u6cbb\u533b\u751f\u60a3\u8005\u7684\u5206\u6790\u8bb0\u5f55");
        }
    }
}
