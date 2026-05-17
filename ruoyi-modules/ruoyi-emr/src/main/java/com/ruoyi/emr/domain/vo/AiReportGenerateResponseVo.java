package com.ruoyi.emr.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response from AI report generation service ({@code /xray/report/generate}).
 */
public class AiReportGenerateResponseVo
{
    private Integer code;

    private String msg;

    private ReportData data;

    public Integer getCode()
    {
        return code;
    }

    public void setCode(Integer code)
    {
        this.code = code;
    }

    public String getMsg()
    {
        return msg;
    }

    public void setMsg(String msg)
    {
        this.msg = msg;
    }

    public ReportData getData()
    {
        return data;
    }

    public void setData(ReportData data)
    {
        this.data = data;
    }

    public static class ReportData
    {
        @JsonProperty("diagnosis_report")
        private String diagnosisReport;

        @JsonProperty("create_time")
        private String createTime;

        public String getDiagnosisReport()
        {
            return diagnosisReport;
        }

        public void setDiagnosisReport(String diagnosisReport)
        {
            this.diagnosisReport = diagnosisReport;
        }

        public String getCreateTime()
        {
            return createTime;
        }

        public void setCreateTime(String createTime)
        {
            this.createTime = createTime;
        }
    }
}
