package com.ruoyi.emr.service.impl;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.emr.domain.MedicalPatientDiagnosis;
import com.ruoyi.emr.mapper.MedicalPatientDiagnosisMapper;
import com.ruoyi.emr.service.IMedicalPatientDiagnosisService;

@Service
public class MedicalPatientDiagnosisServiceImpl implements IMedicalPatientDiagnosisService
{
    @Autowired
    private MedicalPatientDiagnosisMapper medicalPatientDiagnosisMapper;

    @Override
    public void ensureInitialRow(Long patientId)
    {
        if (patientId == null)
        {
            return;
        }
        MedicalPatientDiagnosis existing = medicalPatientDiagnosisMapper.selectByPatientId(patientId);
        if (existing != null)
        {
            return;
        }
        MedicalPatientDiagnosis row = new MedicalPatientDiagnosis();
        row.setPatientId(patientId);
        row.setHasImage(0);
        row.setHasMedicalRecord(0);
        row.setHasLabResult(0);
        row.setDiagnosisStatus(0);
        row.setHasDiagnosisReport(0);
        medicalPatientDiagnosisMapper.insert(row);
    }

    @Override
    public void refreshMultimodal(Long patientId)
    {
        if (patientId == null)
        {
            return;
        }
        ensureInitialRow(patientId);
        medicalPatientDiagnosisMapper.updateMultimodalByPatientId(patientId);
    }

    @Override
    public void markAiCompleted(Long patientId)
    {
        if (patientId == null)
        {
            return;
        }
        ensureInitialRow(patientId);
        medicalPatientDiagnosisMapper.updateDiagnosisAfterAi(patientId);
    }

    @Override
    public void markReportExported(Long patientId)
    {
        if (patientId == null)
        {
            return;
        }
        ensureInitialRow(patientId);
        medicalPatientDiagnosisMapper.updateReportExported(patientId);
    }

    @Override
    public MedicalPatientDiagnosis selectByPatientId(Long patientId)
    {
        return patientId == null ? null : medicalPatientDiagnosisMapper.selectByPatientId(patientId);
    }

    @Override
    public Date firstStructuredRecordTime(Long patientId)
    {
        return patientId == null ? null : medicalPatientDiagnosisMapper.selectFirstRecordTime(patientId);
    }

    @Override
    public Date firstLabTestTime(Long patientId)
    {
        return patientId == null ? null : medicalPatientDiagnosisMapper.selectFirstLabTime(patientId);
    }
}
