package com.ruoyi.imaging.domain;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.annotation.Excel;

/**
 * 胸部X光影像实体，对应 chest_xray 表。
 */
@TableName("chest_xray")
public class ChestXray implements Serializable
{
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @Excel(name = "图片路径")
    private String imagePath;

    @Excel(name = "文件名")
    private String imageName;

    @Excel(name = "标签(0/1)")
    private String labelStr;

    @Excel(name = "疾病名称")
    private String diseases;

    @Excel(name = "宽度")
    private Integer width;

    @Excel(name = "高度")
    private Integer height;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "入库时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /* --- 序列分组辅助字段（若无列则忽略） --- */

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getLabelStr() { return labelStr; }
    public void setLabelStr(String labelStr) { this.labelStr = labelStr; }

    public String getDiseases() { return diseases; }
    public void setDiseases(String diseases) { this.diseases = diseases; }

    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }

    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
