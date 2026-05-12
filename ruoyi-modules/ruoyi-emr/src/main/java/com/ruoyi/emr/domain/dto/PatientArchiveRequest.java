package com.ruoyi.emr.domain.dto;

import java.io.Serializable;

/** Optional body for patient archive API. */
public class PatientArchiveRequest implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String archiveRemark;

    public String getArchiveRemark()
    {
        return archiveRemark;
    }

    public void setArchiveRemark(String archiveRemark)
    {
        this.archiveRemark = archiveRemark;
    }
}
