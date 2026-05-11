package com.ruoyi.emr.domain;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * medical_patient_diagnosis row.
 */
public class MedicalPatientDiagnosis implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long patientId;

    /** 0/1 */
    private Integer hasImage;
    private Integer hasMedicalRecord;
    private Integer hasLabResult;

    /** 0-4 */
    private Integer diagnosisStatus;
    private Integer hasDiagnosisReport;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date firstImageUploadTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastDiagnosisTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reportGenerateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date doctorAuditTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private Integer isDeleted;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getPatientId()
    {
        return patientId;
    }

    public void setPatientId(Long patientId)
    {
        this.patientId = patientId;
    }

    public Integer getHasImage()
    {
        return hasImage;
    }

    public void setHasImage(Integer hasImage)
    {
        this.hasImage = hasImage;
    }

    public Integer getHasMedicalRecord()
    {
        return hasMedicalRecord;
    }

    public void setHasMedicalRecord(Integer hasMedicalRecord)
    {
        this.hasMedicalRecord = hasMedicalRecord;
    }

    public Integer getHasLabResult()
    {
        return hasLabResult;
    }

    public void setHasLabResult(Integer hasLabResult)
    {
        this.hasLabResult = hasLabResult;
    }

    public Integer getDiagnosisStatus()
    {
        return diagnosisStatus;
    }

    public void setDiagnosisStatus(Integer diagnosisStatus)
    {
        this.diagnosisStatus = diagnosisStatus;
    }

    public Integer getHasDiagnosisReport()
    {
        return hasDiagnosisReport;
    }

    public void setHasDiagnosisReport(Integer hasDiagnosisReport)
    {
        this.hasDiagnosisReport = hasDiagnosisReport;
    }

    public Date getFirstImageUploadTime()
    {
        return firstImageUploadTime;
    }

    public void setFirstImageUploadTime(Date firstImageUploadTime)
    {
        this.firstImageUploadTime = firstImageUploadTime;
    }

    public Date getLastDiagnosisTime()
    {
        return lastDiagnosisTime;
    }

    public void setLastDiagnosisTime(Date lastDiagnosisTime)
    {
        this.lastDiagnosisTime = lastDiagnosisTime;
    }

    public Date getReportGenerateTime()
    {
        return reportGenerateTime;
    }

    public void setReportGenerateTime(Date reportGenerateTime)
    {
        this.reportGenerateTime = reportGenerateTime;
    }

    public Date getDoctorAuditTime()
    {
        return doctorAuditTime;
    }

    public void setDoctorAuditTime(Date doctorAuditTime)
    {
        this.doctorAuditTime = doctorAuditTime;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public Date getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
    }

    public Integer getIsDeleted()
    {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted)
    {
        this.isDeleted = isDeleted;
    }
}
