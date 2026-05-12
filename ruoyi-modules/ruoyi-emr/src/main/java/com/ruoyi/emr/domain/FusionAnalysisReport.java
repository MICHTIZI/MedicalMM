package com.ruoyi.emr.domain;

import java.io.Serializable;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * Persisted fusion decision report (fusion_analysis_report).
 */
public class FusionAnalysisReport extends BaseEntity implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long reportId;

    private Long patientId;

    private Long imageId;

    private Long medicalRecordId;

    private Long labResultId;

    /** JSON string of fusion service outer envelope */
    private String fusionResponseJson;

    private String imageResultJson;

    private String caseText;

    private String labDataJson;

    @Excel(name = "Doctor signature")
    private String doctorSignature;

    @Excel(name = "Doctor advice")
    private String doctorAdvice;

    private Integer isDeleted;

    /** List filter: join patient name */
    private String patientName;

    /** List display: chest_xray.image_name */
    private String imageName;

    /** List scope: non-admin filter by attending_doctor_id */
    private Long patientAttendingDoctorIdScope;

    public Long getReportId()
    {
        return reportId;
    }

    public void setReportId(Long reportId)
    {
        this.reportId = reportId;
    }

    public Long getPatientId()
    {
        return patientId;
    }

    public void setPatientId(Long patientId)
    {
        this.patientId = patientId;
    }

    public Long getImageId()
    {
        return imageId;
    }

    public void setImageId(Long imageId)
    {
        this.imageId = imageId;
    }

    public Long getMedicalRecordId()
    {
        return medicalRecordId;
    }

    public void setMedicalRecordId(Long medicalRecordId)
    {
        this.medicalRecordId = medicalRecordId;
    }

    public Long getLabResultId()
    {
        return labResultId;
    }

    public void setLabResultId(Long labResultId)
    {
        this.labResultId = labResultId;
    }

    public String getFusionResponseJson()
    {
        return fusionResponseJson;
    }

    public void setFusionResponseJson(String fusionResponseJson)
    {
        this.fusionResponseJson = fusionResponseJson;
    }

    public String getImageResultJson()
    {
        return imageResultJson;
    }

    public void setImageResultJson(String imageResultJson)
    {
        this.imageResultJson = imageResultJson;
    }

    public String getCaseText()
    {
        return caseText;
    }

    public void setCaseText(String caseText)
    {
        this.caseText = caseText;
    }

    public String getLabDataJson()
    {
        return labDataJson;
    }

    public void setLabDataJson(String labDataJson)
    {
        this.labDataJson = labDataJson;
    }

    public String getDoctorSignature()
    {
        return doctorSignature;
    }

    public void setDoctorSignature(String doctorSignature)
    {
        this.doctorSignature = doctorSignature;
    }

    public String getDoctorAdvice()
    {
        return doctorAdvice;
    }

    public void setDoctorAdvice(String doctorAdvice)
    {
        this.doctorAdvice = doctorAdvice;
    }

    public Integer getIsDeleted()
    {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted)
    {
        this.isDeleted = isDeleted;
    }

    public String getPatientName()
    {
        return patientName;
    }

    public void setPatientName(String patientName)
    {
        this.patientName = patientName;
    }

    public String getImageName()
    {
        return imageName;
    }

    public void setImageName(String imageName)
    {
        this.imageName = imageName;
    }

    public Long getPatientAttendingDoctorIdScope()
    {
        return patientAttendingDoctorIdScope;
    }

    public void setPatientAttendingDoctorIdScope(Long patientAttendingDoctorIdScope)
    {
        this.patientAttendingDoctorIdScope = patientAttendingDoctorIdScope;
    }
}
