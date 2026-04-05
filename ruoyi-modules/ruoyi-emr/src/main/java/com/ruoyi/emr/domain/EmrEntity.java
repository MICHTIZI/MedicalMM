package com.ruoyi.emr.domain;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

@TableName("emr_entity")
public class EmrEntity
{
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("record_id")
    private Long recordId;

    @TableField("label_type")
    private String labelType;

    @TableField("entity_text")
    private String entityText;

    @TableField("start_pos")
    private Integer startPos;

    @TableField("end_pos")
    private Integer endPos;

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

    public Long getRecordId()
    {
        return recordId;
    }

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
    }

    public String getLabelType()
    {
        return labelType;
    }

    public void setLabelType(String labelType)
    {
        this.labelType = labelType;
    }

    public String getEntityText()
    {
        return entityText;
    }

    public void setEntityText(String entityText)
    {
        this.entityText = entityText;
    }

    public Integer getStartPos()
    {
        return startPos;
    }

    public void setStartPos(Integer startPos)
    {
        this.startPos = startPos;
    }

    public Integer getEndPos()
    {
        return endPos;
    }

    public void setEndPos(Integer endPos)
    {
        this.endPos = endPos;
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
