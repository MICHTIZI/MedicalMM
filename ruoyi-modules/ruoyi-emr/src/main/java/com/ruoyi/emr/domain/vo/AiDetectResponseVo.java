package com.ruoyi.emr.domain.vo;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Fixed response contract from AI detection service.
 */
public class AiDetectResponseVo
{
    private Integer code;
    private String msg;

    @JsonProperty("lesion_count")
    private Integer lesionCount;

    @JsonProperty("lesion_list")
    private List<AiLesionVo> lesionList;

    private String original;

    @JsonProperty("ai_result_path")
    private String aiResultPath;

    private String diagnosis;

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public Integer getLesionCount() { return lesionCount; }
    public void setLesionCount(Integer lesionCount) { this.lesionCount = lesionCount; }
    public List<AiLesionVo> getLesionList() { return lesionList; }
    public void setLesionList(List<AiLesionVo> lesionList) { this.lesionList = lesionList; }
    public String getOriginal() { return original; }
    public void setOriginal(String original) { this.original = original; }
    public String getAiResultPath() { return aiResultPath; }
    public void setAiResultPath(String aiResultPath) { this.aiResultPath = aiResultPath; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
}
