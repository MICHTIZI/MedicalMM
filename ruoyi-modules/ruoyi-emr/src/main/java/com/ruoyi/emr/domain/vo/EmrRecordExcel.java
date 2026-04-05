package com.ruoyi.emr.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.annotation.Excel;

public class EmrRecordExcel
{
    @Excel(name = "标题")
    private String title;

    @Excel(name = "病历类型", readConverterExp = "admission=入院记录,discharge=出院记录,surgery=手术记录,course=病程记录")
    private String recordType;

    @Excel(name = "疾病关键词")
    private String disease;

    @Excel(name = "归档状态", readConverterExp = "0=未归档,1=已归档")
    private String archiveStatus;

    @Excel(name = "正文内容")
    private String content;

    @Excel(name = "结构化JSON")
    private String structuredJson;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getRecordType()
    {
        return recordType;
    }

    public void setRecordType(String recordType)
    {
        this.recordType = recordType;
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

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getStructuredJson()
    {
        return structuredJson;
    }

    public void setStructuredJson(String structuredJson)
    {
        this.structuredJson = structuredJson;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
