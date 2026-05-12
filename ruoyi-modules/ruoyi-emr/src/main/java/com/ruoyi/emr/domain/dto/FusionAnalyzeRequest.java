package com.ruoyi.emr.domain.dto;

import java.io.Serializable;
import java.util.Map;

public class FusionAnalyzeRequest implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Map<String, Object> imageResult;
    private String caseText;
    private Map<String, Object> labData;

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
