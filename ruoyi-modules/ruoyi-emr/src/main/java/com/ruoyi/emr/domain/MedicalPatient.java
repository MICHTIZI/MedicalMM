package com.ruoyi.emr.domain;

import java.util.Date;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * Patient entity for medical_patient.
 */
public class MedicalPatient extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "Patient ID")
    private Long patientId;

    @Excel(name = "Patient Name")
    private String patientName;

    @Excel(name = "Gender", readConverterExp = "0=Male,1=Female,2=Unknown")
    private String gender;

    @Excel(name = "Age")
    private Integer age;

    @Excel(name = "Phone")
    private String phone;

    @Excel(name = "Address")
    private String address;

    @Excel(name = "Attending Doctor ID")
    private Long attendingDoctorId;

    @Excel(name = "Attending Doctor")
    private String attendingDoctor;

    /** 0 in progress, 1 archived */
    private Integer isArchived;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date archiveTime;

    private String archiveBy;

    private String archiveRemark;

    /** Query only: active | archived (default active when null) */
    private String archiveScope;

    public Long getPatientId()
    {
        return patientId;
    }

    public void setPatientId(Long patientId)
    {
        this.patientId = patientId;
    }

    @NotBlank(message = "Patient name is required")
    @Size(max = 50, message = "Patient name must be within 50 characters")
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

    @Min(value = 0, message = "Age cannot be less than 0")
    @Max(value = 150, message = "Age cannot be greater than 150")
    public Integer getAge()
    {
        return age;
    }

    public void setAge(Integer age)
    {
        this.age = age;
    }

    @Size(max = 20, message = "Phone must be within 20 characters")
    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    @Size(max = 255, message = "Address must be within 255 characters")
    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public Long getAttendingDoctorId()
    {
        return attendingDoctorId;
    }

    public void setAttendingDoctorId(Long attendingDoctorId)
    {
        this.attendingDoctorId = attendingDoctorId;
    }

    public String getAttendingDoctor()
    {
        return attendingDoctor;
    }

    public void setAttendingDoctor(String attendingDoctor)
    {
        this.attendingDoctor = attendingDoctor;
    }

    public Integer getIsArchived()
    {
        return isArchived;
    }

    public void setIsArchived(Integer isArchived)
    {
        this.isArchived = isArchived;
    }

    public Date getArchiveTime()
    {
        return archiveTime;
    }

    public void setArchiveTime(Date archiveTime)
    {
        this.archiveTime = archiveTime;
    }

    public String getArchiveBy()
    {
        return archiveBy;
    }

    public void setArchiveBy(String archiveBy)
    {
        this.archiveBy = archiveBy;
    }

    public String getArchiveRemark()
    {
        return archiveRemark;
    }

    public void setArchiveRemark(String archiveRemark)
    {
        this.archiveRemark = archiveRemark;
    }

    public String getArchiveScope()
    {
        return archiveScope;
    }

    public void setArchiveScope(String archiveScope)
    {
        this.archiveScope = archiveScope;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("patientId", getPatientId())
            .append("patientName", getPatientName())
            .append("gender", getGender())
            .append("age", getAge())
            .append("phone", getPhone())
            .append("address", getAddress())
            .append("attendingDoctorId", getAttendingDoctorId())
            .append("attendingDoctor", getAttendingDoctor())
            .append("isArchived", getIsArchived())
            .append("archiveTime", getArchiveTime())
            .append("archiveBy", getArchiveBy())
            .append("archiveRemark", getArchiveRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
