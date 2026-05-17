package com.ruoyi.emr.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * Persisted AI lesion analysis result ({@code ai_image_analysis_result}).
 */
public class AiImageAnalysisResult extends BaseEntity implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long analysisId;

    private Long imageId;

    private Long patientId;

    private Integer lesionCount;

    private BigDecimal infectionRate;

    private BigDecimal totalInfectionArea;

    private String detectDiagnosis;

    private String aiResultPath;

    private String detectResultJson;

    private String diagnosisReportRaw;

    private String diagnosisReportJson;

    private String reportCreateTime;

    private Integer isDeleted;

    /** List filter */
    private String patientName;

    private String imageName;

    private Long patientAttendingDoctorIdScope;

    public Long getAnalysisId()
    {
        return analysisId;
    }

    public void setAnalysisId(Long analysisId)
    {
        this.analysisId = analysisId;
    }

    public Long getImageId()
    {
        return imageId;
    }

    public void setImageId(Long imageId)
    {
        this.imageId = imageId;
    }

    public Long getPatientId()
    {
        return patientId;
    }

    public void setPatientId(Long patientId)
    {
        this.patientId = patientId;
    }

    public Integer getLesionCount()
    {
        return lesionCount;
    }

    public void setLesionCount(Integer lesionCount)
    {
        this.lesionCount = lesionCount;
    }

    public BigDecimal getInfectionRate()
    {
        return infectionRate;
    }

    public void setInfectionRate(BigDecimal infectionRate)
    {
        this.infectionRate = infectionRate;
    }

    public BigDecimal getTotalInfectionArea()
    {
        return totalInfectionArea;
    }

    public void setTotalInfectionArea(BigDecimal totalInfectionArea)
    {
        this.totalInfectionArea = totalInfectionArea;
    }

    public String getDetectDiagnosis()
    {
        return detectDiagnosis;
    }

    public void setDetectDiagnosis(String detectDiagnosis)
    {
        this.detectDiagnosis = detectDiagnosis;
    }

    public String getAiResultPath()
    {
        return aiResultPath;
    }

    public void setAiResultPath(String aiResultPath)
    {
        this.aiResultPath = aiResultPath;
    }

    public String getDetectResultJson()
    {
        return detectResultJson;
    }

    public void setDetectResultJson(String detectResultJson)
    {
        this.detectResultJson = detectResultJson;
    }

    public String getDiagnosisReportRaw()
    {
        return diagnosisReportRaw;
    }

    public void setDiagnosisReportRaw(String diagnosisReportRaw)
    {
        this.diagnosisReportRaw = diagnosisReportRaw;
    }

    public String getDiagnosisReportJson()
    {
        return diagnosisReportJson;
    }

    public void setDiagnosisReportJson(String diagnosisReportJson)
    {
        this.diagnosisReportJson = diagnosisReportJson;
    }

    public String getReportCreateTime()
    {
        return reportCreateTime;
    }

    public void setReportCreateTime(String reportCreateTime)
    {
        this.reportCreateTime = reportCreateTime;
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
