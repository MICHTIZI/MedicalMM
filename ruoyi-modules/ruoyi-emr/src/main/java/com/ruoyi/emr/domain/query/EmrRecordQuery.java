package com.ruoyi.emr.domain.query;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class EmrRecordQuery implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String keyword;

    private String disease;

    private String archiveStatus;

    private String recordType;

    private Date beginTime;

    private Date endTime;

    private List<EntityFilterItem> entityFilters;

    /** Optional GET params: up to 3 entity filters */
    private String e1Type;
    private String e1Keyword;
    private String e2Type;
    private String e2Keyword;
    private String e3Type;
    private String e3Keyword;

    public String getKeyword()
    {
        return keyword;
    }

    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    public String getDisease()
    {
        return disease;
    }

    public void setDisease(String disease)
    {
        this.disease = disease;
    }

    public String getArchiveStatus()
    {
        return archiveStatus;
    }

    public void setArchiveStatus(String archiveStatus)
    {
        this.archiveStatus = archiveStatus;
    }

    public String getRecordType()
    {
        return recordType;
    }

    public void setRecordType(String recordType)
    {
        this.recordType = recordType;
    }

    public Date getBeginTime()
    {
        return beginTime;
    }

    public void setBeginTime(Date beginTime)
    {
        this.beginTime = beginTime;
    }

    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    public List<EntityFilterItem> getEntityFilters()
    {
        return entityFilters;
    }

    public void setEntityFilters(List<EntityFilterItem> entityFilters)
    {
        this.entityFilters = entityFilters;
    }

    public String getE1Type()
    {
        return e1Type;
    }

    public void setE1Type(String e1Type)
    {
        this.e1Type = e1Type;
    }

    public String getE1Keyword()
    {
        return e1Keyword;
    }

    public void setE1Keyword(String e1Keyword)
    {
        this.e1Keyword = e1Keyword;
    }

    public String getE2Type()
    {
        return e2Type;
    }

    public void setE2Type(String e2Type)
    {
        this.e2Type = e2Type;
    }

    public String getE2Keyword()
    {
        return e2Keyword;
    }

    public void setE2Keyword(String e2Keyword)
    {
        this.e2Keyword = e2Keyword;
    }

    public String getE3Type()
    {
        return e3Type;
    }

    public void setE3Type(String e3Type)
    {
        this.e3Type = e3Type;
    }

    public String getE3Keyword()
    {
        return e3Keyword;
    }

    public void setE3Keyword(String e3Keyword)
    {
        this.e3Keyword = e3Keyword;
    }
}
