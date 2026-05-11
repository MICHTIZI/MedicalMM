package com.ruoyi.emr.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.emr.domain.ChestXray;
import com.ruoyi.emr.domain.MedicalPatient;
import com.ruoyi.emr.domain.MedicalPatientDiagnosis;
import com.ruoyi.emr.domain.vo.DoctorOptionVo;
import com.ruoyi.emr.domain.vo.PatientCardVo;
import com.ruoyi.emr.domain.vo.PatientDiagnosisDetailVo;
import com.ruoyi.emr.mapper.MedicalPatientMapper;
import com.ruoyi.emr.service.IChestXrayService;
import com.ruoyi.emr.service.IMedicalPatientDiagnosisService;
import com.ruoyi.emr.service.IMedicalPatientService;
import com.ruoyi.system.api.model.LoginUser;

@Service
public class MedicalPatientServiceImpl implements IMedicalPatientService
{
    private static final Pattern INFECTION_HINT = Pattern.compile("感染[^\\r\\n.。;；]*");

    @Autowired
    private MedicalPatientMapper medicalPatientMapper;

    @Autowired
    private IMedicalPatientDiagnosisService medicalPatientDiagnosisService;

    @Autowired
    private IChestXrayService chestXrayService;

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
    public List<PatientCardVo> selectPatientCardList(MedicalPatient patient)
    {
        if (!SecurityUtils.isAdmin())
        {
            patient.setAttendingDoctorId(SecurityUtils.getUserId());
        }
        return medicalPatientMapper.selectPatientCardVoList(patient);
    }

    @Override
    public PatientDiagnosisDetailVo selectPatientDiagnosisDetail(Long patientId)
    {
        if (patientId == null)
        {
            throw new ServiceException("patientId required");
        }
        MedicalPatient p = medicalPatientMapper.selectMedicalPatientByPatientId(patientId);
        if (p == null)
        {
            throw new ServiceException("Patient does not exist");
        }
        checkPatientOwner(p);
        medicalPatientDiagnosisService.ensureInitialRow(patientId);
        medicalPatientDiagnosisService.refreshMultimodal(patientId);
        MedicalPatientDiagnosis d = medicalPatientDiagnosisService.selectByPatientId(patientId);
        ChestXray latestXray = chestXrayService.lambdaQuery()
            .eq(ChestXray::getPatientId, patientId)
            .orderByDesc(ChestXray::getCreateTime)
            .last("LIMIT 1")
            .one();
        PatientCardVo card = buildCardVo(p, d, latestXray);
        PatientDiagnosisDetailVo vo = new PatientDiagnosisDetailVo();
        vo.setCard(card);

        PatientDiagnosisDetailVo.MedicalPatientSnapshot snap = new PatientDiagnosisDetailVo.MedicalPatientSnapshot();
        snap.setPatientId(p.getPatientId());
        snap.setPatientName(p.getPatientName());
        snap.setGender(p.getGender());
        snap.setAge(p.getAge());
        snap.setPhone(p.getPhone());
        snap.setAddress(p.getAddress());
        snap.setAttendingDoctor(p.getAttendingDoctor());
        snap.setCreateTime(p.getCreateTime());
        vo.setPatientSnapshot(snap);
        vo.setDiagnosisSnapshot(d);
        vo.setTimeline(buildTimeline(p, d, patientId));

        PatientDiagnosisDetailVo.AiSummaryVo ai = new PatientDiagnosisDetailVo.AiSummaryVo();
        if (latestXray != null)
        {
            ai.setDiagnosisText(latestXray.getDiagnosis());
            ai.setLesionCount(latestXray.getLesionCount());
            ai.setImageTime(latestXray.getCreateTime());
            ai.setInfectionHint(extractInfectionHint(latestXray.getDiagnosis()));
        }
        vo.setLatestAi(ai);
        return vo;
    }

    @Override
    public MedicalPatient selectMedicalPatientByPatientId(Long patientId)
    {
        MedicalPatient patient = medicalPatientMapper.selectMedicalPatientByPatientId(patientId);
        checkPatientOwner(patient);
        return patient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMedicalPatient(MedicalPatient patient)
    {
        fillDoctorForInsert(patient);
        patient.setCreateBy(SecurityUtils.getUsername());
        patient.setCreateTime(new Date());
        int rows = medicalPatientMapper.insertMedicalPatient(patient);
        if (rows > 0 && patient.getPatientId() != null)
        {
            medicalPatientDiagnosisService.ensureInitialRow(patient.getPatientId());
        }
        return rows;
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

    private PatientCardVo buildCardVo(MedicalPatient p, MedicalPatientDiagnosis d, ChestXray latestXray)
    {
        PatientCardVo c = new PatientCardVo();
        c.setPatientId(p.getPatientId());
        c.setPatientName(p.getPatientName());
        c.setGender(p.getGender());
        c.setAge(p.getAge());
        c.setPhone(p.getPhone());
        c.setAttendingDoctor(p.getAttendingDoctor());
        c.setPatientCreateTime(p.getCreateTime());
        if (d != null)
        {
            c.setHasImage(nz(d.getHasImage()));
            c.setHasMedicalRecord(nz(d.getHasMedicalRecord()));
            c.setHasLabResult(nz(d.getHasLabResult()));
            c.setDiagnosisStatus(d.getDiagnosisStatus() != null ? d.getDiagnosisStatus() : 0);
            c.setHasDiagnosisReport(nz(d.getHasDiagnosisReport()));
            c.setLastDiagnosisTime(d.getLastDiagnosisTime());
        }
        else
        {
            c.setHasImage(0);
            c.setHasMedicalRecord(0);
            c.setHasLabResult(0);
            c.setDiagnosisStatus(0);
            c.setHasDiagnosisReport(0);
        }
        if (latestXray != null)
        {
            c.setLatestAiDiagnosis(latestXray.getDiagnosis());
            c.setLatestLesionCount(latestXray.getLesionCount());
        }
        return c;
    }

    private static int nz(Integer v)
    {
        return v != null ? v : 0;
    }

    private List<PatientDiagnosisDetailVo.WorkflowStepVo> buildTimeline(MedicalPatient p, MedicalPatientDiagnosis d, Long patientId)
    {
        List<PatientDiagnosisDetailVo.WorkflowStepVo> list = new ArrayList<>();
        addStep(list, 1, "新建患者", true, p.getCreateTime());
        Date ft = d == null ? null : d.getFirstImageUploadTime();
        addStep(list, 2, "上传胸片", d != null && nz(d.getHasImage()) == 1, ft);
        Date recT = medicalPatientDiagnosisService.firstStructuredRecordTime(patientId);
        addStep(list, 3, "录入病历", d != null && nz(d.getHasMedicalRecord()) == 1, recT);
        Date labT = medicalPatientDiagnosisService.firstLabTestTime(patientId);
        addStep(list, 4, "导入检验", d != null && nz(d.getHasLabResult()) == 1, labT);
        boolean aiDone = d != null && d.getDiagnosisStatus() != null && d.getDiagnosisStatus() >= 2;
        addStep(list, 5, "AI诊断", aiDone, d == null ? null : d.getLastDiagnosisTime());
        boolean reportDone = d != null && nz(d.getHasDiagnosisReport()) == 1;
        addStep(list, 6, "生成报告", reportDone, d == null ? null : d.getReportGenerateTime());
        return list;
    }

    private static void addStep(List<PatientDiagnosisDetailVo.WorkflowStepVo> list, int step, String title, boolean done,
        Date doneTime)
    {
        PatientDiagnosisDetailVo.WorkflowStepVo s = new PatientDiagnosisDetailVo.WorkflowStepVo();
        s.setStep(step);
        s.setTitle(title);
        s.setDone(done);
        s.setDoneTime(doneTime);
        list.add(s);
    }

    private static String extractInfectionHint(String diagnosis)
    {
        if (StringUtils.isEmpty(diagnosis))
        {
            return null;
        }
        Matcher m = INFECTION_HINT.matcher(diagnosis);
        return m.find() ? m.group().trim() : null;
    }
}
