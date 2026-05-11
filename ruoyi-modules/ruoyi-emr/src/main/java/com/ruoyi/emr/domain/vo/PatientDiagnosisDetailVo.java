package com.ruoyi.emr.domain.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import com.ruoyi.emr.domain.MedicalPatientDiagnosis;

/**
 * Drawer payload for patient + diagnosis aggregates.
 */
public class PatientDiagnosisDetailVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private PatientCardVo card;

    private MedicalPatientSnapshot patientSnapshot;

    private MedicalPatientDiagnosis diagnosisSnapshot;

    private List<WorkflowStepVo> timeline;

    private AiSummaryVo latestAi;

    public PatientCardVo getCard()
    {
        return card;
    }

    public void setCard(PatientCardVo card)
    {
        this.card = card;
    }

    public MedicalPatientSnapshot getPatientSnapshot()
    {
        return patientSnapshot;
    }

    public void setPatientSnapshot(MedicalPatientSnapshot patientSnapshot)
    {
        this.patientSnapshot = patientSnapshot;
    }

    public MedicalPatientDiagnosis getDiagnosisSnapshot()
    {
        return diagnosisSnapshot;
    }

    public void setDiagnosisSnapshot(MedicalPatientDiagnosis diagnosisSnapshot)
    {
        this.diagnosisSnapshot = diagnosisSnapshot;
    }

    public List<WorkflowStepVo> getTimeline()
    {
        return timeline;
    }

    public void setTimeline(List<WorkflowStepVo> timeline)
    {
        this.timeline = timeline;
    }

    public AiSummaryVo getLatestAi()
    {
        return latestAi;
    }

    public void setLatestAi(AiSummaryVo latestAi)
    {
        this.latestAi = latestAi;
    }

    public static class MedicalPatientSnapshot implements Serializable
    {
        private static final long serialVersionUID = 1L;
        private Long patientId;
        private String patientName;
        private String gender;
        private Integer age;
        private String phone;
        private String address;
        private String attendingDoctor;
        private java.util.Date createTime;

        public Long getPatientId()
        {
            return patientId;
        }

        public void setPatientId(Long patientId)
        {
            this.patientId = patientId;
        }

        public String getPatientName()
        {
            return patientName;
        }

        public void setPatientName(String patientName)
        {
            this.patientName = patientName;
        }

        public String getGender()
        {
            return gender;
        }

        public void setGender(String gender)
        {
            this.gender = gender;
        }

        public Integer getAge()
        {
            return age;
        }

        public void setAge(Integer age)
        {
            this.age = age;
        }

        public String getPhone()
        {
            return phone;
        }

        public void setPhone(String phone)
        {
            this.phone = phone;
        }

        public String getAddress()
        {
            return address;
        }

        public void setAddress(String address)
        {
            this.address = address;
        }

        public String getAttendingDoctor()
        {
            return attendingDoctor;
        }

        public void setAttendingDoctor(String attendingDoctor)
        {
            this.attendingDoctor = attendingDoctor;
        }

        public java.util.Date getCreateTime()
        {
            return createTime;
        }

        public void setCreateTime(java.util.Date createTime)
        {
            this.createTime = createTime;
        }
    }

    public static class WorkflowStepVo implements Serializable
    {
        private static final long serialVersionUID = 1L;
        private int step;
        private String title;
        private boolean done;
        private Date doneTime;

        public int getStep()
        {
            return step;
        }

        public void setStep(int step)
        {
            this.step = step;
        }

        public String getTitle()
        {
            return title;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        public boolean isDone()
        {
            return done;
        }

        public void setDone(boolean done)
        {
            this.done = done;
        }

        public Date getDoneTime()
        {
            return doneTime;
        }

        public void setDoneTime(Date doneTime)
        {
            this.doneTime = doneTime;
        }
    }

    public static class AiSummaryVo implements Serializable
    {
        private static final long serialVersionUID = 1L;
        private String diagnosisText;
        private Integer lesionCount;
        private Date imageTime;
        private String infectionHint;

        public String getDiagnosisText()
        {
            return diagnosisText;
        }

        public void setDiagnosisText(String diagnosisText)
        {
            this.diagnosisText = diagnosisText;
        }

        public String getInfectionHint()
        {
            return infectionHint;
        }

        public void setInfectionHint(String infectionHint)
        {
            this.infectionHint = infectionHint;
        }

        public Integer getLesionCount()
        {
            return lesionCount;
        }

        public void setLesionCount(Integer lesionCount)
        {
            this.lesionCount = lesionCount;
        }

        public Date getImageTime()
        {
            return imageTime;
        }

        public void setImageTime(Date imageTime)
        {
            this.imageTime = imageTime;
        }
    }
}
