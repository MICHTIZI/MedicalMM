package com.ruoyi.emr.service.impl;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.emr.domain.ChestXray;
import com.ruoyi.emr.domain.FusionAnalysisReport;
import com.ruoyi.emr.domain.MedicalPatient;
import com.ruoyi.emr.domain.dto.FusionReportDoctorUpdateDto;
import com.ruoyi.emr.domain.dto.FusionReportSaveAnalyzeDto;
import com.ruoyi.emr.mapper.FusionAnalysisReportMapper;
import com.ruoyi.emr.mapper.MedicalPatientMapper;
import com.ruoyi.emr.service.IChestXrayService;
import com.ruoyi.emr.service.IFusionAnalysisReportService;
import com.ruoyi.emr.support.PatientArchiveGuard;

@Service
public class FusionAnalysisReportServiceImpl implements IFusionAnalysisReportService
{
    @Autowired
    private FusionAnalysisReportMapper fusionAnalysisReportMapper;

    @Autowired
    private IChestXrayService chestXrayService;

    @Autowired
    private MedicalPatientMapper medicalPatientMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientArchiveGuard patientArchiveGuard;

    @Override
    public List<FusionAnalysisReport> selectFusionAnalysisReportList(FusionAnalysisReport query)
    {
        if (!SecurityUtils.isAdmin())
        {
            query.setPatientAttendingDoctorIdScope(SecurityUtils.getUserId());
        }
        else
        {
            query.setPatientAttendingDoctorIdScope(null);
        }
        return fusionAnalysisReportMapper.selectFusionAnalysisReportList(query);
    }

    @Override
    public FusionAnalysisReport selectFusionAnalysisReportById(Long reportId)
    {
        FusionAnalysisReport row = fusionAnalysisReportMapper.selectFusionAnalysisReportById(reportId);
        checkReportAccess(row);
        return row;
    }

    @Override
    public FusionAnalysisReport selectByImageIdForViewer(Long imageId)
    {
        ChestXray xray = chestXrayService.getById(imageId);
        checkXrayAccess(xray);
        return fusionAnalysisReportMapper.selectByPatientIdAndImageId(xray.getPatientId(), imageId);
    }

    @Override
    public FusionAnalysisReport saveAnalyzeResult(Long imageId, FusionReportSaveAnalyzeDto body)
    {
        if (body == null || body.getFusionResponse() == null || body.getFusionResponse().isEmpty())
        {
            throw new ServiceException("fusionResponse is required");
        }
        if (body.getImageResult() == null || body.getImageResult().isEmpty())
        {
            throw new ServiceException("imageResult is required");
        }
        if (StringUtils.isEmpty(body.getCaseText()))
        {
            throw new ServiceException("caseText is required");
        }
        ChestXray xray = chestXrayService.getById(imageId);
        checkXrayAccess(xray);
        patientArchiveGuard.rejectIfArchived(xray.getPatientId());
        FusionAnalysisReport existing = fusionAnalysisReportMapper.selectByPatientIdAndImageId(xray.getPatientId(), imageId);
        FusionAnalysisReport row = new FusionAnalysisReport();
        row.setPatientId(xray.getPatientId());
        row.setImageId(imageId);
        row.setMedicalRecordId(body.getMedicalRecordId());
        row.setLabResultId(body.getLabResultId());
        try
        {
            row.setFusionResponseJson(objectMapper.writeValueAsString(body.getFusionResponse()));
            row.setImageResultJson(objectMapper.writeValueAsString(body.getImageResult()));
            row.setLabDataJson(body.getLabData() == null || body.getLabData().isEmpty()
                ? null
                : objectMapper.writeValueAsString(body.getLabData()));
        }
        catch (JsonProcessingException e)
        {
            throw new ServiceException("JSON serialize failed: " + e.getMessage());
        }
        row.setCaseText(body.getCaseText());
        row.setUpdateBy(SecurityUtils.getUsername());
        row.setUpdateTime(new Date());
        if (existing != null)
        {
            row.setReportId(existing.getReportId());
            row.setCreateBy(existing.getCreateBy());
            row.setCreateTime(existing.getCreateTime());
            row.setDoctorSignature(existing.getDoctorSignature());
            row.setDoctorAdvice(existing.getDoctorAdvice());
            row.setRemark(existing.getRemark());
            fusionAnalysisReportMapper.updateFusionAnalysisReport(row);
            return fusionAnalysisReportMapper.selectFusionAnalysisReportById(existing.getReportId());
        }
        row.setCreateBy(SecurityUtils.getUsername());
        row.setCreateTime(new Date());
        fusionAnalysisReportMapper.insertFusionAnalysisReport(row);
        return fusionAnalysisReportMapper.selectFusionAnalysisReportById(row.getReportId());
    }

    @Override
    public int updateDoctorFields(FusionReportDoctorUpdateDto dto)
    {
        if (dto == null || dto.getReportId() == null)
        {
            throw new ServiceException("reportId is required");
        }
        FusionAnalysisReport cur = fusionAnalysisReportMapper.selectFusionAnalysisReportById(dto.getReportId());
        checkReportAccess(cur);
        patientArchiveGuard.rejectIfArchived(cur.getPatientId());
        FusionAnalysisReport row = new FusionAnalysisReport();
        row.setReportId(dto.getReportId());
        row.setDoctorSignature(dto.getDoctorSignature());
        row.setDoctorAdvice(dto.getDoctorAdvice());
        row.setUpdateBy(SecurityUtils.getUsername());
        row.setUpdateTime(new Date());
        return fusionAnalysisReportMapper.updateFusionAnalysisReport(row);
    }

    @Override
    public int updateFusionAnalysisReport(FusionAnalysisReport row)
    {
        if (row == null || row.getReportId() == null)
        {
            throw new ServiceException("reportId is required");
        }
        FusionAnalysisReport cur = fusionAnalysisReportMapper.selectFusionAnalysisReportById(row.getReportId());
        checkReportAccess(cur);
        patientArchiveGuard.rejectIfArchived(cur.getPatientId());
        row.setUpdateBy(SecurityUtils.getUsername());
        row.setUpdateTime(new Date());
        return fusionAnalysisReportMapper.updateFusionAnalysisReport(row);
    }

    @Override
    public int deleteFusionAnalysisReportByIds(Long[] ids)
    {
        if (ids == null || ids.length == 0)
        {
            return 0;
        }
        for (Long id : ids)
        {
            FusionAnalysisReport row = fusionAnalysisReportMapper.selectFusionAnalysisReportById(id);
            checkReportAccess(row);
            if (row != null && row.getPatientId() != null)
            {
                patientArchiveGuard.rejectIfArchived(row.getPatientId());
            }
        }
        return fusionAnalysisReportMapper.logicalDeleteFusionAnalysisReportByIds(ids);
    }

    private void checkReportAccess(FusionAnalysisReport row)
    {
        if (row == null)
        {
            throw new ServiceException("Report not found");
        }
        ChestXray xray = chestXrayService.getById(row.getImageId());
        checkXrayAccess(xray);
    }

    private void checkXrayAccess(ChestXray xray)
    {
        if (xray == null)
        {
            throw new ServiceException("Image not found");
        }
        if (xray.getPatientId() == null)
        {
            throw new ServiceException("Image not bound to patient");
        }
        if (!SecurityUtils.isAdmin())
        {
            Long userId = SecurityUtils.getUserId();
            if (userId == null)
            {
                throw new ServiceException("No permission");
            }
            MedicalPatient patient = medicalPatientMapper.selectMedicalPatientByPatientId(xray.getPatientId());
            if (patient == null || patient.getAttendingDoctorId() == null || !patient.getAttendingDoctorId().equals(userId))
            {
                throw new ServiceException("No permission for this patient image");
            }
        }
    }
}
