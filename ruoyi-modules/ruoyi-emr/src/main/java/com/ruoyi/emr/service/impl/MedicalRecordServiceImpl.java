package com.ruoyi.emr.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.emr.domain.MedicalPatient;
import com.ruoyi.emr.domain.MedicalRecord;
import com.ruoyi.emr.domain.vo.XrayOptionVo;
import com.ruoyi.emr.mapper.MedicalRecordMapper;
import com.ruoyi.emr.service.IMedicalRecordService;
import com.ruoyi.system.api.model.LoginUser;

/**
 * Structured medical record service implementation.
 */
@Service
public class MedicalRecordServiceImpl implements IMedicalRecordService
{
    @Autowired
    private MedicalRecordMapper medicalRecordMapper;

    @Override
    public List<MedicalRecord> selectMedicalRecordList(MedicalRecord record)
    {
        if (!SecurityUtils.isAdmin())
        {
            record.setOperateDoctorId(SecurityUtils.getUserId());
        }
        return medicalRecordMapper.selectMedicalRecordList(record);
    }

    @Override
    public MedicalRecord selectMedicalRecordByRecordId(Long recordId)
    {
        MedicalRecord record = medicalRecordMapper.selectMedicalRecordByRecordId(recordId);
        checkRecordAccess(record);
        return record;
    }

    @Override
    public int insertMedicalRecord(MedicalRecord record)
    {
        MedicalPatient patient = requirePatient(record.getPatientId());
        requireAttendingDoctor(patient);
        fillOperator(record);
        fillImage(record);
        checkImageUnique(null, record.getImageId());
        record.setCreateBy(SecurityUtils.getUsername());
        record.setCreateTime(new Date());
        return medicalRecordMapper.insertMedicalRecord(record);
    }

    @Override
    public int updateMedicalRecord(MedicalRecord record)
    {
        MedicalRecord old = medicalRecordMapper.selectMedicalRecordByRecordId(record.getRecordId());
        checkRecordAccess(old);
        if (old == null)
        {
            return 0;
        }
        MedicalPatient patient = requirePatient(record.getPatientId());
        if (!SecurityUtils.isAdmin())
        {
            requireAttendingDoctor(patient);
        }
        record.setOperateDoctorId(old.getOperateDoctorId());
        record.setOperateDoctor(old.getOperateDoctor());
        fillImage(record);
        checkImageUnique(record.getRecordId(), record.getImageId());
        record.setUpdateBy(SecurityUtils.getUsername());
        record.setUpdateTime(new Date());
        return medicalRecordMapper.updateMedicalRecord(record);
    }

    @Override
    public int deleteMedicalRecordByRecordIds(Long[] recordIds)
    {
        for (Long recordId : recordIds)
        {
            checkRecordAccess(medicalRecordMapper.selectMedicalRecordByRecordId(recordId));
        }
        return medicalRecordMapper.deleteMedicalRecordByRecordIds(recordIds);
    }

    @Override
    public int deleteMedicalRecordByRecordId(Long recordId)
    {
        checkRecordAccess(medicalRecordMapper.selectMedicalRecordByRecordId(recordId));
        return medicalRecordMapper.deleteMedicalRecordByRecordId(recordId);
    }

    @Override
    public List<MedicalPatient> selectPatientOptions(MedicalPatient patient)
    {
        if (!SecurityUtils.isAdmin())
        {
            patient.setAttendingDoctorId(SecurityUtils.getUserId());
        }
        return medicalRecordMapper.selectPatientOptions(patient);
    }

    @Override
    public List<XrayOptionVo> selectXrayOptions(String keyword)
    {
        return medicalRecordMapper.selectXrayOptions(keyword);
    }

    private MedicalPatient requirePatient(Long patientId)
    {
        MedicalPatient patient = medicalRecordMapper.selectPatientById(patientId);
        if (patient == null)
        {
            throw new ServiceException("Patient does not exist");
        }
        return patient;
    }

    private void requireAttendingDoctor(MedicalPatient patient)
    {
        Long userId = SecurityUtils.getUserId();
        if (patient.getAttendingDoctorId() == null || !patient.getAttendingDoctorId().equals(userId))
        {
            throw new ServiceException("Only the attending doctor can create or edit records for this patient");
        }
    }

    private void checkRecordAccess(MedicalRecord record)
    {
        if (record == null || SecurityUtils.isAdmin())
        {
            return;
        }
        if (record.getOperateDoctorId() == null || !record.getOperateDoctorId().equals(SecurityUtils.getUserId()))
        {
            throw new ServiceException("No permission to access records operated by another doctor");
        }
    }

    private void fillOperator(MedicalRecord record)
    {
        record.setOperateDoctorId(SecurityUtils.getUserId());
        record.setOperateDoctor(currentNickName());
    }

    private String currentNickName()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getSysUser() != null && StringUtils.isNotEmpty(loginUser.getSysUser().getNickName()))
        {
            return loginUser.getSysUser().getNickName();
        }
        return SecurityUtils.getUsername();
    }

    private void fillImage(MedicalRecord record)
    {
        if (record.getImageId() == null)
        {
            throw new ServiceException("Chest X-ray is required");
        }
        XrayOptionVo xray = medicalRecordMapper.selectXrayById(record.getImageId());
        if (xray == null)
        {
            throw new ServiceException("Chest X-ray does not exist");
        }
        record.setImagePath(xray.getImagePath());
    }

    private void checkImageUnique(Long recordId, Long imageId)
    {
        if (imageId == null)
        {
            return;
        }
        if (medicalRecordMapper.countImageUsed(recordId, imageId) > 0)
        {
            throw new ServiceException("This chest X-ray is already bound to another record");
        }
    }
}
