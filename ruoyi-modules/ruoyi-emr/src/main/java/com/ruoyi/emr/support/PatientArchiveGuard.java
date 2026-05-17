package com.ruoyi.emr.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.emr.domain.MedicalPatient;
import com.ruoyi.emr.mapper.MedicalPatientMapper;

/**
 * 患者病历已归档时，拦截写操作。
 */
@Component
public class PatientArchiveGuard
{
    @Autowired
    private MedicalPatientMapper medicalPatientMapper;

    public void rejectIfArchived(Long patientId)
    {
        if (patientId == null || SecurityUtils.isAdmin())
        {
            return;
        }
        MedicalPatient p = medicalPatientMapper.selectMedicalPatientByPatientId(patientId);
        if (p != null && p.getIsArchived() != null && p.getIsArchived() == 1)
        {
            throw new ServiceException("该患者已归档，暂不允许进行此项操作。");
        }
    }
}
