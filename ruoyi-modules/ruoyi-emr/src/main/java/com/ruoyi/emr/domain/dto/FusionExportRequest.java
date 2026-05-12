package com.ruoyi.emr.domain.dto;

import java.io.Serializable;
import java.util.Map;

public class FusionExportRequest implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String doctorSignature;
    private String doctorAdvice;
    private Map<String, Object> fusionResponse;
    private Map<String, Object> imageResult;
    private String caseText;
    private Map<String, Object> labData;

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

    public Map<String, Object> getFusionResponse()
    {
        return fusionResponse;
    }

    public void setFusionResponse(Map<String, Object> fusionResponse)
    {
        this.fusionResponse = fusionResponse;
    }

    public Map<String, Object> getImageResult()
    {
        return imageResult;
    }

    public void setImageResult(Map<String, Object> imageResult)
    {
        this.imageResult = imageResult;
    }

    public String getCaseText()
    {
        return caseText;
    }

    public void setCaseText(String caseText)
    {
        this.caseText = caseText;
    }

    public Map<String, Object> getLabData()
    {
        return labData;
    }

    public void setLabData(Map<String, Object> labData)
    {
        this.labData = labData;
    }
}
