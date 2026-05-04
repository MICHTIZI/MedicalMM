package com.ruoyi.emr.domain;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * Structured medical record entity for medical_record.
 */
public class MedicalRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "Record ID")
    private Long recordId;

    @Excel(name = "Patient ID")
    private Long patientId;

    @Excel(name = "Patient Name")
    private String patientName;

    @Excel(name = "Chief Complaint")
    private String chiefComplaint;

    @Excel(name = "Present History")
    private String presentHistory;

    @Excel(name = "Past History")
    private String pastHistory;

    @Excel(name = "Physical Exam")
    private String physicalExam;

    @Excel(name = "Initial Diagnosis")
    private String initialDiagnosis;

    @Excel(name = "Operate Doctor ID")
    private Long operateDoctorId;

    @Excel(name = "Operate Doctor")
    private String operateDoctor;

    private Long imageId;

    @Excel(name = "Image Path")
    private String imagePath;

    @Excel(name = "AI Result Path")
    private String aiResultPath;

    public Long getRecordId()
    {
        return recordId;
    }

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
    }

    @NotNull(message = "Patient is required")
    public Long getPatientId()
    {
        return patientId;
    }

    public void setPatientId(Long patientId)
    {
        this.patientId = patientId;
    }

    public String getPatientName()
    {
        return patientName;
    }

    public void setPatientName(String patientName)
    {
        this.patientName = patientName;
    }

    public String getChiefComplaint()
    {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint)
    {
        this.chiefComplaint = chiefComplaint;
    }

    public String getPresentHistory()
    {
        return presentHistory;
    }

    public void setPresentHistory(String presentHistory)
    {
        this.presentHistory = presentHistory;
    }

    public String getPastHistory()
    {
        return pastHistory;
    }

    public void setPastHistory(String pastHistory)
    {
        this.pastHistory = pastHistory;
    }

    public String getPhysicalExam()
    {
        return physicalExam;
    }

    public void setPhysicalExam(String physicalExam)
    {
        this.physicalExam = physicalExam;
    }

    public String getInitialDiagnosis()
    {
        return initialDiagnosis;
    }

    public void setInitialDiagnosis(String initialDiagnosis)
    {
        this.initialDiagnosis = initialDiagnosis;
    }

    public Long getOperateDoctorId()
    {
        return operateDoctorId;
    }

    public void setOperateDoctorId(Long operateDoctorId)
    {
        this.operateDoctorId = operateDoctorId;
    }

    public String getOperateDoctor()
    {
        return operateDoctor;
    }

    public void setOperateDoctor(String operateDoctor)
    {
        this.operateDoctor = operateDoctor;
    }

    @NotNull(message = "Chest X-ray is required")
    public Long getImageId()
    {
        return imageId;
    }

    public void setImageId(Long imageId)
    {
        this.imageId = imageId;
    }

    public String getImagePath()
    {
        return imagePath;
    }

    public void setImagePath(String imagePath)
    {
        this.imagePath = imagePath;
    }

    public String getAiResultPath()
    {
        return aiResultPath;
    }

    public void setAiResultPath(String aiResultPath)
    {
        this.aiResultPath = aiResultPath;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("recordId", getRecordId())
            .append("patientId", getPatientId())
            .append("chiefComplaint", getChiefComplaint())
            .append("presentHistory", getPresentHistory())
            .append("pastHistory", getPastHistory())
            .append("physicalExam", getPhysicalExam())
            .append("initialDiagnosis", getInitialDiagnosis())
            .append("operateDoctorId", getOperateDoctorId())
            .append("operateDoctor", getOperateDoctor())
            .append("imageId", getImageId())
            .append("imagePath", getImagePath())
            .append("aiResultPath", getAiResultPath())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
