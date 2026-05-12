package com.ruoyi.emr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "emr.fusion")
public class EmrFusionProperties
{
    private String baseUrl = "http://127.0.0.1:5200";

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public String analyzeUrl()
    {
        String b = baseUrl == null ? "" : baseUrl.trim();
        while (b.endsWith("/"))
        {
            b = b.substring(0, b.length() - 1);
        }
        return b + "/fusion/analyze";
    }
}
