package com.ruoyi.emr.domain.vo;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Patient card payload for workstation grid.
 */
public class PatientCardVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long patientId;
    private String patientName;
    private String gender;
    private Integer age;
    private String phone;
    private String attendingDoctor;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date patientCreateTime;

    private Integer hasImage;
    private Integer hasMedicalRecord;
    private Integer hasLabResult;

    /** 0-4 diagnosis_status */
    private Integer diagnosisStatus;
    private Integer hasDiagnosisReport;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastDiagnosisTime;

    private String latestAiDiagnosis;
    private Integer latestLesionCount;

    private Integer isArchived;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date archiveTime;

    private String archiveBy;
    private String archiveRemark;

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

    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public Integer getAge()
    {
        return age;
    }

    public void setAge(Integer age)
    {
        this.age = age;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getAttendingDoctor()
    {
        return attendingDoctor;
    }

    public void setAttendingDoctor(String attendingDoctor)
    {
        this.attendingDoctor = attendingDoctor;
    }

    public Date getPatientCreateTime()
    {
        return patientCreateTime;
    }

    public void setPatientCreateTime(Date patientCreateTime)
    {
        this.patientCreateTime = patientCreateTime;
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

    public Date getLastDiagnosisTime()
    {
        return lastDiagnosisTime;
    }

    public void setLastDiagnosisTime(Date lastDiagnosisTime)
    {
        this.lastDiagnosisTime = lastDiagnosisTime;
    }

    public String getLatestAiDiagnosis()
    {
        return latestAiDiagnosis;
    }

    public void setLatestAiDiagnosis(String latestAiDiagnosis)
    {
        this.latestAiDiagnosis = latestAiDiagnosis;
    }

    public Integer getLatestLesionCount()
    {
        return latestLesionCount;
    }

    public void setLatestLesionCount(Integer latestLesionCount)
    {
        this.latestLesionCount = latestLesionCount;
    }

    public Integer getIsArchived()
    {
        return isArchived;
    }

    public void setIsArchived(Integer isArchived)
    {
        this.isArchived = isArchived;
    }

    public Date getArchiveTime()
    {
        return archiveTime;
    }

    public void setArchiveTime(Date archiveTime)
    {
        this.archiveTime = archiveTime;
    }

    public String getArchiveBy()
    {
        return archiveBy;
    }

    public void setArchiveBy(String archiveBy)
    {
        this.archiveBy = archiveBy;
    }

    public String getArchiveRemark()
    {
        return archiveRemark;
    }

    public void setArchiveRemark(String archiveRemark)
    {
        this.archiveRemark = archiveRemark;
    }

    /** All three modalities satisfied */
    public boolean isDataReady()
    {
        return intOne(hasImage) && intOne(hasMedicalRecord) && intOne(hasLabResult);
    }

    private static boolean intOne(Integer v)
    {
        return v != null && v == 1;
    }
}
