package com.ruoyi.imaging.domain.query;

import java.io.Serializable;

/**
 * 影像高级检索参数。
 */
public class ChestXrayQuery implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String keyword;
    private String diseases;
    private String imageName;
    private Integer minWidth;
    private Integer maxWidth;
    private String beginTime;
    private String endTime;
    private String labelStr;
    private Long seqId;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getDiseases() { return diseases; }
    public void setDiseases(String diseases) { this.diseases = diseases; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public Integer getMinWidth() { return minWidth; }
    public void setMinWidth(Integer minWidth) { this.minWidth = minWidth; }

    public Integer getMaxWidth() { return maxWidth; }
    public void setMaxWidth(Integer maxWidth) { this.maxWidth = maxWidth; }

    public String getBeginTime() { return beginTime; }
    public void setBeginTime(String beginTime) { this.beginTime = beginTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getLabelStr() { return labelStr; }
    public void setLabelStr(String labelStr) { this.labelStr = labelStr; }

    public Long getSeqId() { return seqId; }
    public void setSeqId(Long seqId) { this.seqId = seqId; }
}
