package com.ruoyi.emr.domain.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * Body for saving fusion result after external analyze call.
 */
public class FusionReportSaveAnalyzeDto implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Map<String, Object> fusionResponse;

    private Map<String, Object> imageResult;

    private String caseText;

    private Map<String, Object> labData;

    private Long medicalRecordId;

    private Long labResultId;

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
}
