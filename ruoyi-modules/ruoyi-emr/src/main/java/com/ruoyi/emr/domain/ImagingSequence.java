package com.ruoyi.emr.domain;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 影像序列（同一患者/检查的影像分组）。
 */
@TableName("imaging_sequence")
public class ImagingSequence implements Serializable
{
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 序列名称 */
    private String seqName;

    /** 患者ID或姓名 */
    private String patientInfo;

    /** 检查描述 */
    private String description;

    /** 归档状态 0=正常 1=已归档 */
    private String archiveStatus;

    /** 排序号 */
    private Integer sortOrder;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSeqName() { return seqName; }
    public void setSeqName(String seqName) { this.seqName = seqName; }

    public String getPatientInfo() { return patientInfo; }
    public void setPatientInfo(String patientInfo) { this.patientInfo = patientInfo; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getArchiveStatus() { return archiveStatus; }
    public void setArchiveStatus(String archiveStatus) { this.archiveStatus = archiveStatus; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
