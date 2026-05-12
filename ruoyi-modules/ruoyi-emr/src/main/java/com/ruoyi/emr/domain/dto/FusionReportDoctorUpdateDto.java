package com.ruoyi.emr.domain.dto;

import java.io.Serializable;

public class FusionReportDoctorUpdateDto implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long reportId;

    private String doctorSignature;

    private String doctorAdvice;

    public Long getReportId()
    {
        return reportId;
    }

    public void setReportId(Long reportId)
    {
        this.reportId = reportId;
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
}
