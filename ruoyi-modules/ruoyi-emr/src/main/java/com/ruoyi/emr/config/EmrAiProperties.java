package com.ruoyi.emr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "emr.ai")
public class EmrAiProperties
{
    private String detectUrl = "http://127.0.0.1:5100/ai/detect/minio";

    private String reportBaseUrl = "http://127.0.0.1:5300";

    public String getDetectUrl()
    {
        return detectUrl;
    }

    public void setDetectUrl(String detectUrl)
    {
        this.detectUrl = detectUrl;
    }

    public String getReportBaseUrl()
    {
        return reportBaseUrl;
    }

    public void setReportBaseUrl(String reportBaseUrl)
    {
        this.reportBaseUrl = reportBaseUrl;
    }

    public String reportGenerateUrl()
    {
        String b = reportBaseUrl == null ? "" : reportBaseUrl.trim();
        while (b.endsWith("/"))
        {
            b = b.substring(0, b.length() - 1);
        }
        return b + "/xray/report/generate";
    }
}
