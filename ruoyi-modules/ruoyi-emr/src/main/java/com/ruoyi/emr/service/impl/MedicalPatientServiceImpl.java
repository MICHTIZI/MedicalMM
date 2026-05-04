package com.ruoyi.emr.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.emr.domain.MedicalPatient;
import com.ruoyi.emr.domain.vo.DoctorOptionVo;
import com.ruoyi.emr.mapper.MedicalPatientMapper;
import com.ruoyi.emr.service.IMedicalPatientService;
import com.ruoyi.system.api.model.LoginUser;

/**
 * ??????? Service ??????
 */
@Service
public class MedicalPatientServiceImpl implements IMedicalPatientService
{
    @Autowired
    private MedicalPatientMapper medicalPatientMapper;

    @Override
    public List<MedicalPatient> selectMedicalPatientList(MedicalPatient patient)
    {
        if (!SecurityUtils.isAdmin())
        {
            patient.setAttendingDoctorId(SecurityUtils.getUserId());
        }
        return medicalPatientMapper.selectMedicalPatientList(patient);
    }

    @Override
    public MedicalPatient selectMedicalPatientByPatientId(Long patientId)
    {
        MedicalPatient patient = medicalPatientMapper.selectMedicalPatientByPatientId(patientId);
        checkPatientOwner(patient);
        return patient;
    }

    @Override
    public int insertMedicalPatient(MedicalPatient patient)
    {
        fillDoctorForInsert(patient);
        patient.setCreateBy(SecurityUtils.getUsername());
        patient.setCreateTime(new Date());
        return medicalPatientMapper.insertMedicalPatient(patient);
    }

    @Override
    public int updateMedicalPatient(MedicalPatient patient)
    {
        MedicalPatient old = medicalPatientMapper.selectMedicalPatientByPatientId(patient.getPatientId());
        checkPatientOwner(old);
        if (old == null)
        {
            return 0;
        }
        fillDoctorForUpdate(patient, old);
        patient.setUpdateBy(SecurityUtils.getUsername());
        patient.setUpdateTime(new Date());
        return medicalPatientMapper.updateMedicalPatient(patient);
    }

    @Override
    public int deleteMedicalPatientByPatientIds(Long[] patientIds)
    {
        for (Long patientId : patientIds)
        {
            checkPatientOwner(medicalPatientMapper.selectMedicalPatientByPatientId(patientId));
        }
        return medicalPatientMapper.deleteMedicalPatientByPatientIds(patientIds);
    }

    @Override
    public int deleteMedicalPatientByPatientId(Long patientId)
    {
        checkPatientOwner(medicalPatientMapper.selectMedicalPatientByPatientId(patientId));
        return medicalPatientMapper.deleteMedicalPatientByPatientId(patientId);
    }

    @Override
    public List<DoctorOptionVo> selectDoctorOptions()
    {
        if (!SecurityUtils.isAdmin())
        {
            throw new ServiceException("Only admins can view doctor assignment options");
        }
        return medicalPatientMapper.selectDoctorOptions();
    }

    private void fillDoctorForInsert(MedicalPatient patient)
    {
        if (SecurityUtils.isAdmin() && patient.getAttendingDoctorId() != null)
        {
            fillDoctorNameById(patient);
            return;
        }
        patient.setAttendingDoctorId(SecurityUtils.getUserId());
        patient.setAttendingDoctor(currentNickName());
    }

    private void fillDoctorForUpdate(MedicalPatient patient, MedicalPatient old)
    {
        if (SecurityUtils.isAdmin())
        {
            if (patient.getAttendingDoctorId() == null)
            {
                patient.setAttendingDoctorId(old.getAttendingDoctorId());
                patient.setAttendingDoctor(old.getAttendingDoctor());
            }
            else
            {
                fillDoctorNameById(patient);
            }
            return;
        }
        patient.setAttendingDoctorId(old.getAttendingDoctorId());
        patient.setAttendingDoctor(old.getAttendingDoctor());
    }

    private void fillDoctorNameById(MedicalPatient patient)
    {
        String doctorName = medicalPatientMapper.selectDoctorNickNameById(patient.getAttendingDoctorId());
        if (StringUtils.isEmpty(doctorName))
        {
            throw new ServiceException("Attending doctor does not exist or is not active");
        }
        patient.setAttendingDoctor(doctorName);
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

    private void checkPatientOwner(MedicalPatient patient)
    {
        if (patient == null || SecurityUtils.isAdmin())
        {
            return;
        }
        Long userId = SecurityUtils.getUserId();
        if (patient.getAttendingDoctorId() == null || !patient.getAttendingDoctorId().equals(userId))
        {
            throw new ServiceException("No permission to access patients owned by another doctor");
        }
    }
}
