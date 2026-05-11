package com.ruoyi.emr.domain.vo;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
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

    /** MinIO path to AI overlay image; AI service may return {@code ai_result} or {@code ai_result_path}. */
    @JsonAlias({ "ai_result", "ai_result_path", "aiResultPath" })
    private String aiResultPath;

    private String diagnosis;

    @JsonProperty("total_infection_area")
    private Double totalInfectionArea;

    @JsonProperty("infection_rate")
    private Double infectionRate;

    private String severity;

    @JsonProperty("pneumonia_type")
    private String pneumoniaType;

    @JsonProperty("treatment_suggestion")
    private String treatmentSuggestion;

    @JsonProperty("further_examination")
    private String furtherExamination;

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
    public Double getTotalInfectionArea() { return totalInfectionArea; }
    public void setTotalInfectionArea(Double totalInfectionArea) { this.totalInfectionArea = totalInfectionArea; }
    public Double getInfectionRate() { return infectionRate; }
    public void setInfectionRate(Double infectionRate) { this.infectionRate = infectionRate; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getPneumoniaType() { return pneumoniaType; }
    public void setPneumoniaType(String pneumoniaType) { this.pneumoniaType = pneumoniaType; }
    public String getTreatmentSuggestion() { return treatmentSuggestion; }
    public void setTreatmentSuggestion(String treatmentSuggestion) { this.treatmentSuggestion = treatmentSuggestion; }
    public String getFurtherExamination() { return furtherExamination; }
    public void setFurtherExamination(String furtherExamination) { this.furtherExamination = furtherExamination; }
}
