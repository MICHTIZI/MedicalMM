package com.ruoyi.emr.domain.query;

import java.io.Serializable;

public class EntityFilterItem implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String labelType;

    private String keyword;

    public String getLabelType()
    {
        return labelType;
    }

    public void setLabelType(String labelType)
    {
        this.labelType = labelType;
    }

    public String getKeyword()
    {
        return keyword;
    }

    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }
}
