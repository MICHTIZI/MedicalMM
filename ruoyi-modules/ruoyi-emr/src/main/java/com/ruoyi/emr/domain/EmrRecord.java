package com.ruoyi.emr.domain;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

@TableName("emr_record")
public class EmrRecord
{
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    @TableField("structured_json")
    private String structuredJson;

    @TableField("archive_status")
    private String archiveStatus;

    @TableField("archived_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date archivedTime;

    private String disease;

    @TableField("template_id")
    private Long templateId;

    @TableField("record_type")
    private String recordType;

    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
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

    public String getArchiveStatus()
    {
        return archiveStatus;
    }

    public void setArchiveStatus(String archiveStatus)
    {
        this.archiveStatus = archiveStatus;
    }

    public Date getArchivedTime()
    {
        return archivedTime;
    }

    public void setArchivedTime(Date archivedTime)
    {
        this.archivedTime = archivedTime;
    }

    public String getDisease()
    {
        return disease;
    }

    public void setDisease(String disease)
    {
        this.disease = disease;
    }

    public Long getTemplateId()
    {
        return templateId;
    }

    public void setTemplateId(Long templateId)
    {
        this.templateId = templateId;
    }

    public String getRecordType()
    {
        return recordType;
    }

    public void setRecordType(String recordType)
    {
        this.recordType = recordType;
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
