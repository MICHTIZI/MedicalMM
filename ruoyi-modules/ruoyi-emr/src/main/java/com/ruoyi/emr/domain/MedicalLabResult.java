package com.ruoyi.emr.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * Patient lab result row (medical_lab_results).
 */
public class MedicalLabResult extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long patientId;

    /** Display / query: patient name (join), not persisted on insert */
    private String patientName;

    /** Non-admin list scope: filter by medical_patient.attending_doctor_id */
    private Long patientAttendingDoctorIdScope;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date testDate;

    private String testDoctor;

    private String testDepartment;

    private BigDecimal temperature;

    private Integer heartRate;

    private Integer respiratoryRate;

    private Integer systolicBp;

    private Integer diastolicBp;

    private BigDecimal spo2;

    private BigDecimal wbc;

    private BigDecimal neutrophilRatio;

    private BigDecimal lymphocyteRatio;

    private BigDecimal monocyteRatio;

    private Integer platelet;

    private BigDecimal crp;

    private BigDecimal pct;

    private Integer esr;

    private BigDecimal ph;

    private BigDecimal po2;

    private BigDecimal pco2;

    private BigDecimal hco3;

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

    public String getPatientName()
    {
        return patientName;
    }

    public void setPatientName(String patientName)
    {
        this.patientName = patientName;
    }

    public Long getPatientAttendingDoctorIdScope()
    {
        return patientAttendingDoctorIdScope;
    }

    public void setPatientAttendingDoctorIdScope(Long patientAttendingDoctorIdScope)
    {
        this.patientAttendingDoctorIdScope = patientAttendingDoctorIdScope;
    }

    public Date getTestDate()
    {
        return testDate;
    }

    public void setTestDate(Date testDate)
    {
        this.testDate = testDate;
    }

    public String getTestDoctor()
    {
        return testDoctor;
    }

    public void setTestDoctor(String testDoctor)
    {
        this.testDoctor = testDoctor;
    }

    public String getTestDepartment()
    {
        return testDepartment;
    }

    public void setTestDepartment(String testDepartment)
    {
        this.testDepartment = testDepartment;
    }

    public BigDecimal getTemperature()
    {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature)
    {
        this.temperature = temperature;
    }

    public Integer getHeartRate()
    {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate)
    {
        this.heartRate = heartRate;
    }

    public Integer getRespiratoryRate()
    {
        return respiratoryRate;
    }

    public void setRespiratoryRate(Integer respiratoryRate)
    {
        this.respiratoryRate = respiratoryRate;
    }

    public Integer getSystolicBp()
    {
        return systolicBp;
    }

    public void setSystolicBp(Integer systolicBp)
    {
        this.systolicBp = systolicBp;
    }

    public Integer getDiastolicBp()
    {
        return diastolicBp;
    }

    public void setDiastolicBp(Integer diastolicBp)
    {
        this.diastolicBp = diastolicBp;
    }

    public BigDecimal getSpo2()
    {
        return spo2;
    }

    public void setSpo2(BigDecimal spo2)
    {
        this.spo2 = spo2;
    }

    public BigDecimal getWbc()
    {
        return wbc;
    }

    public void setWbc(BigDecimal wbc)
    {
        this.wbc = wbc;
    }

    public BigDecimal getNeutrophilRatio()
    {
        return neutrophilRatio;
    }

    public void setNeutrophilRatio(BigDecimal neutrophilRatio)
    {
        this.neutrophilRatio = neutrophilRatio;
    }

    public BigDecimal getLymphocyteRatio()
    {
        return lymphocyteRatio;
    }

    public void setLymphocyteRatio(BigDecimal lymphocyteRatio)
    {
        this.lymphocyteRatio = lymphocyteRatio;
    }

    public BigDecimal getMonocyteRatio()
    {
        return monocyteRatio;
    }

    public void setMonocyteRatio(BigDecimal monocyteRatio)
    {
        this.monocyteRatio = monocyteRatio;
    }

    public Integer getPlatelet()
    {
        return platelet;
    }

    public void setPlatelet(Integer platelet)
    {
        this.platelet = platelet;
    }

    public BigDecimal getCrp()
    {
        return crp;
    }

    public void setCrp(BigDecimal crp)
    {
        this.crp = crp;
    }

    public BigDecimal getPct()
    {
        return pct;
    }

    public void setPct(BigDecimal pct)
    {
        this.pct = pct;
    }

    public Integer getEsr()
    {
        return esr;
    }

    public void setEsr(Integer esr)
    {
        this.esr = esr;
    }

    public BigDecimal getPh()
    {
        return ph;
    }

    public void setPh(BigDecimal ph)
    {
        this.ph = ph;
    }

    public BigDecimal getPo2()
    {
        return po2;
    }

    public void setPo2(BigDecimal po2)
    {
        this.po2 = po2;
    }

    public BigDecimal getPco2()
    {
        return pco2;
    }

    public void setPco2(BigDecimal pco2)
    {
        this.pco2 = pco2;
    }

    public BigDecimal getHco3()
    {
        return hco3;
    }

    public void setHco3(BigDecimal hco3)
    {
        this.hco3 = hco3;
    }

    public Integer getIsDeleted()
    {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted)
    {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", id)
            .append("patientId", patientId)
            .append("patientName", patientName)
            .append("testDate", testDate)
            .append("testDoctor", testDoctor)
            .append("testDepartment", testDepartment)
            .toString();
    }
}
