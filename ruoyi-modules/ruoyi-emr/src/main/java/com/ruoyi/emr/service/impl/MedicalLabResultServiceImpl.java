package com.ruoyi.emr.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.emr.domain.MedicalLabResult;
import com.ruoyi.emr.domain.MedicalPatient;
import com.ruoyi.emr.mapper.MedicalLabResultMapper;
import com.ruoyi.emr.mapper.MedicalPatientMapper;
import com.ruoyi.emr.service.IMedicalLabResultService;
import com.ruoyi.emr.service.IMedicalPatientDiagnosisService;
import com.ruoyi.emr.support.PatientArchiveGuard;
import com.ruoyi.emr.util.LabResultTxtParser;

/**
 * Lab results CRUD + TXT import (patient-bound).
 */
@Service
public class MedicalLabResultServiceImpl implements IMedicalLabResultService
{
    @Autowired
    private MedicalLabResultMapper medicalLabResultMapper;

    @Autowired
    private MedicalPatientMapper medicalPatientMapper;

    @Autowired
    private IMedicalPatientDiagnosisService medicalPatientDiagnosisService;

    @Autowired
    private PatientArchiveGuard patientArchiveGuard;

    @Override
    public List<MedicalLabResult> selectMedicalLabResultList(MedicalLabResult query)
    {
        if (!SecurityUtils.isAdmin())
        {
            query.setPatientAttendingDoctorIdScope(SecurityUtils.getUserId());
        }
        else
        {
            query.setPatientAttendingDoctorIdScope(null);
        }
        return medicalLabResultMapper.selectMedicalLabResultList(query);
    }

    @Override
    public MedicalLabResult selectMedicalLabResultById(Long id)
    {
        MedicalLabResult row = medicalLabResultMapper.selectMedicalLabResultById(id);
        checkLabAccess(row);
        return row;
    }

    @Override
    public int insertMedicalLabResult(MedicalLabResult row)
    {
        if (row.getPatientId() == null)
        {
            throw new ServiceException("请选择患者");
        }
        MedicalPatient patient = requirePatient(row.getPatientId());
        if (!SecurityUtils.isAdmin())
        {
            requireAttendingDoctor(patient);
        }
        patientArchiveGuard.rejectIfArchived(row.getPatientId());
        Date now = new Date();
        row.setCreateBy(SecurityUtils.getUsername());
        row.setCreateTime(now);
        row.setUpdateBy(row.getCreateBy());
        row.setUpdateTime(now);
        if (row.getTestDate() == null)
        {
            row.setTestDate(now);
        }
        int n = medicalLabResultMapper.insertMedicalLabResult(row);
        if (n > 0 && row.getPatientId() != null)
        {
            medicalPatientDiagnosisService.refreshMultimodal(row.getPatientId());
        }
        return n;
    }

    @Override
    public int updateMedicalLabResult(MedicalLabResult row)
    {
        MedicalLabResult old = medicalLabResultMapper.selectMedicalLabResultById(row.getId());
        checkLabAccess(old);
        if (old == null)
        {
            return 0;
        }
        MedicalPatient patient = requirePatient(old.getPatientId());
        if (!SecurityUtils.isAdmin())
        {
            requireAttendingDoctor(patient);
        }
        patientArchiveGuard.rejectIfArchived(old.getPatientId());
        row.setPatientId(old.getPatientId());
        row.setUpdateBy(SecurityUtils.getUsername());
        row.setUpdateTime(new Date());
        int n = medicalLabResultMapper.updateMedicalLabResult(row);
        if (n > 0 && row.getPatientId() != null)
        {
            medicalPatientDiagnosisService.refreshMultimodal(row.getPatientId());
        }
        return n;
    }

    @Override
    public int deleteMedicalLabResultByIds(Long[] ids)
    {
        Set<Long> patientIds = new HashSet<>();
        for (Long id : ids)
        {
            MedicalLabResult row = medicalLabResultMapper.selectMedicalLabResultById(id);
            checkLabAccess(row);
            if (row != null && row.getPatientId() != null)
            {
                patientArchiveGuard.rejectIfArchived(row.getPatientId());
                patientIds.add(row.getPatientId());
            }
        }
        int n = medicalLabResultMapper.logicalDeleteMedicalLabResultByIds(ids);
        if (n > 0)
        {
            for (Long pid : patientIds)
            {
                medicalPatientDiagnosisService.refreshMultimodal(pid);
            }
        }
        return n;
    }

    @Override
    public int importFromTxt(Long patientId, MultipartFile file)
    {
        if (patientId == null)
        {
            throw new ServiceException("导入检验须指定患者");
        }
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("请上传 TXT 检验文件");
        }
        MedicalPatient patient = requirePatient(patientId);
        if (!SecurityUtils.isAdmin())
        {
            requireAttendingDoctor(patient);
        }
        patientArchiveGuard.rejectIfArchived(patientId);
        MedicalLabResult parsed;
        try
        {
            parsed = LabResultTxtParser.parse(file.getBytes());
        }
        catch (IOException e)
        {
            throw new ServiceException("读取上传文件失败：" + e.getMessage());
        }
        catch (Exception e)
        {
            throw new ServiceException(e.getMessage() == null ? "TXT parse failed" : e.getMessage());
        }
        parsed.setPatientId(patientId);
        parsed.setRemark(StringUtils.trimToNull(parsed.getRemark()));
        Date now = new Date();
        parsed.setCreateBy(SecurityUtils.getUsername());
        parsed.setCreateTime(now);
        parsed.setUpdateBy(parsed.getCreateBy());
        parsed.setUpdateTime(now);
        int n = medicalLabResultMapper.insertMedicalLabResult(parsed);
        if (n > 0)
        {
            medicalPatientDiagnosisService.refreshMultimodal(patientId);
        }
        return n;
    }

    @Override
    public MedicalLabResult parseTxtPreview(MultipartFile file)
    {
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("请上传 TXT 检验文件");
        }
        MedicalLabResult parsed;
        try
        {
            parsed = LabResultTxtParser.parse(file.getBytes());
        }
        catch (IOException e)
        {
            throw new ServiceException("读取上传文件失败：" + e.getMessage());
        }
        catch (Exception e)
        {
            throw new ServiceException(e.getMessage() == null ? "TXT parse failed" : e.getMessage());
        }
        parsed.setId(null);
        parsed.setPatientId(null);
        parsed.setPatientName(null);
        parsed.setPatientAttendingDoctorIdScope(null);
        parsed.setIsDeleted(null);
        parsed.setRemark(StringUtils.trimToNull(parsed.getRemark()));
        parsed.setCreateBy(null);
        parsed.setCreateTime(null);
        parsed.setUpdateBy(null);
        parsed.setUpdateTime(null);
        return parsed;
    }

    private MedicalPatient requirePatient(Long patientId)
    {
        MedicalPatient patient = medicalPatientMapper.selectMedicalPatientByPatientId(patientId);
        if (patient == null)
        {
            throw new ServiceException("患者不存在");
        }
        return patient;
    }

    private void requireAttendingDoctor(MedicalPatient patient)
    {
        Long userId = SecurityUtils.getUserId();
        if (patient.getAttendingDoctorId() == null || !patient.getAttendingDoctorId().equals(userId))
        {
            throw new ServiceException("仅主治医生可操作该患者的检验数据");
        }
    }

    private void checkLabAccess(MedicalLabResult row)
    {
        if (row == null)
        {
            throw new ServiceException("检验记录不存在");
        }
        if (SecurityUtils.isAdmin())
        {
            return;
        }
        MedicalPatient patient = medicalPatientMapper.selectMedicalPatientByPatientId(row.getPatientId());
        if (patient == null || patient.getAttendingDoctorId() == null || !patient.getAttendingDoctorId().equals(SecurityUtils.getUserId()))
        {
            throw new ServiceException("无权访问该检验记录");
        }
    }

}
