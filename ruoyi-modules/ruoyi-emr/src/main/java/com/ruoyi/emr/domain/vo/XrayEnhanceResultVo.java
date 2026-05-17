package com.ruoyi.emr.domain.vo;

public class XrayEnhanceResultVo
{
    private String originalObjectKey;
    private String annotatedObjectKey;
    private boolean annotatedAvailable;

    public String getOriginalObjectKey()
    {
        return originalObjectKey;
    }

    public void setOriginalObjectKey(String originalObjectKey)
    {
        this.originalObjectKey = originalObjectKey;
    }

    public String getAnnotatedObjectKey()
    {
        return annotatedObjectKey;
    }

    public void setAnnotatedObjectKey(String annotatedObjectKey)
    {
        this.annotatedObjectKey = annotatedObjectKey;
    }

    public boolean isAnnotatedAvailable()
    {
        return annotatedAvailable;
    }

    public void setAnnotatedAvailable(boolean annotatedAvailable)
    {
        this.annotatedAvailable = annotatedAvailable;
    }
}
