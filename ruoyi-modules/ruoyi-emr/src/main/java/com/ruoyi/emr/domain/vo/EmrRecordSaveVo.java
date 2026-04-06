package com.ruoyi.emr.domain.vo;

import java.io.Serializable;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import com.ruoyi.emr.domain.EmrEntity;
import com.ruoyi.emr.domain.EmrRecord;

public class EmrRecordSaveVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    @NotNull(message = "病历主表不能为空")
    @Valid
    private EmrRecord record;

    @Valid
    private List<EmrEntity> entities;

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
}
