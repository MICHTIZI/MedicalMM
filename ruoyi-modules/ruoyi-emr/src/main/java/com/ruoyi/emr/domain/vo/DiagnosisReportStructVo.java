package com.ruoyi.emr.domain.vo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fixed structure parsed from AI {@code diagnosis_report} text (five sections).
 */
public class DiagnosisReportStructVo
{
    private String rawText;

    private ConclusionBlock conclusion = new ConclusionBlock();

    private ImagingBlock imagingAnalysis = new ImagingBlock();

    private ClinicalBlock clinicalSignificance = new ClinicalBlock();

    private SeverityBlock severityAssessment = new SeverityBlock();

    private String summary;

    public String getRawText()
    {
        return rawText;
    }

    public void setRawText(String rawText)
    {
        this.rawText = rawText;
    }

    public ConclusionBlock getConclusion()
    {
        return conclusion;
    }

    public void setConclusion(ConclusionBlock conclusion)
    {
        this.conclusion = conclusion;
    }

    public ImagingBlock getImagingAnalysis()
    {
        return imagingAnalysis;
    }

    public void setImagingAnalysis(ImagingBlock imagingAnalysis)
    {
        this.imagingAnalysis = imagingAnalysis;
    }

    public ClinicalBlock getClinicalSignificance()
    {
        return clinicalSignificance;
    }

    public void setClinicalSignificance(ClinicalBlock clinicalSignificance)
    {
        this.clinicalSignificance = clinicalSignificance;
    }

    public SeverityBlock getSeverityAssessment()
    {
        return severityAssessment;
    }

    public void setSeverityAssessment(SeverityBlock severityAssessment)
    {
        this.severityAssessment = severityAssessment;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public static class ConclusionBlock
    {
        private String mainDiagnosis;
        private String confidence;
        private String differentialDiagnosis;

        public String getMainDiagnosis()
        {
            return mainDiagnosis;
        }

        public void setMainDiagnosis(String mainDiagnosis)
        {
            this.mainDiagnosis = mainDiagnosis;
        }

        public String getConfidence()
        {
            return confidence;
        }

        public void setConfidence(String confidence)
        {
            this.confidence = confidence;
        }

        public String getDifferentialDiagnosis()
        {
            return differentialDiagnosis;
        }

        public void setDifferentialDiagnosis(String differentialDiagnosis)
        {
            this.differentialDiagnosis = differentialDiagnosis;
        }
    }

    public static class ImagingBlock
    {
        private List<String> lesionFeatures = new ArrayList<>();
        private List<String> overallLungAssessment = new ArrayList<>();
        private List<String> otherFeatures = new ArrayList<>();

        public List<String> getLesionFeatures()
        {
            return lesionFeatures;
        }

        public void setLesionFeatures(List<String> lesionFeatures)
        {
            this.lesionFeatures = lesionFeatures;
        }

        public List<String> getOverallLungAssessment()
        {
            return overallLungAssessment;
        }

        public void setOverallLungAssessment(List<String> overallLungAssessment)
        {
            this.overallLungAssessment = overallLungAssessment;
        }

        public List<String> getOtherFeatures()
        {
            return otherFeatures;
        }

        public void setOtherFeatures(List<String> otherFeatures)
        {
            this.otherFeatures = otherFeatures;
        }
    }

    public static class ClinicalBlock
    {
        private List<String> lesionNatureAnalysis = new ArrayList<>();
        private Map<String, String> riskAssessment = new LinkedHashMap<>();

        public List<String> getLesionNatureAnalysis()
        {
            return lesionNatureAnalysis;
        }

        public void setLesionNatureAnalysis(List<String> lesionNatureAnalysis)
        {
            this.lesionNatureAnalysis = lesionNatureAnalysis;
        }

        public Map<String, String> getRiskAssessment()
        {
            return riskAssessment;
        }

        public void setRiskAssessment(Map<String, String> riskAssessment)
        {
            this.riskAssessment = riskAssessment;
        }
    }

    public static class SeverityBlock
    {
        private String severityLevel;
        private String basis;

        public String getSeverityLevel()
        {
            return severityLevel;
        }

        public void setSeverityLevel(String severityLevel)
        {
            this.severityLevel = severityLevel;
        }

        public String getBasis()
        {
            return basis;
        }

        public void setBasis(String basis)
        {
            this.basis = basis;
        }
    }
}
