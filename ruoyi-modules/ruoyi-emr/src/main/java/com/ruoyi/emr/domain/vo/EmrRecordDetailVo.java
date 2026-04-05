package com.ruoyi.emr.domain.vo;

import java.io.Serializable;
import java.util.List;
import com.ruoyi.emr.domain.EmrEntity;
import com.ruoyi.emr.domain.EmrRecord;

public class EmrRecordDetailVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private EmrRecord record;

    private List<EmrEntity> entities;

    private String plainText;

    private String highlightedHtml;

    public EmrRecord getRecord()
    {
        return record;
    }

    public void setRecord(EmrRecord record)
    {
        this.record = record;
    }

    public List<EmrEntity> getEntities()
    {
        return entities;
    }

    public void setEntities(List<EmrEntity> entities)
    {
        this.entities = entities;
    }

    public String getPlainText()
    {
        return plainText;
    }

    public void setPlainText(String plainText)
    {
        this.plainText = plainText;
    }

    public String getHighlightedHtml()
    {
        return highlightedHtml;
    }

    public void setHighlightedHtml(String highlightedHtml)
    {
        this.highlightedHtml = highlightedHtml;
    }
}
