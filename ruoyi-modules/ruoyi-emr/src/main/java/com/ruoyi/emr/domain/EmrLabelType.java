package com.ruoyi.emr.domain;

/**
 * Values for emr_entity.label_type.
 */
public final class EmrLabelType
{
    public static final String DISEASE = "DISEASE";
    public static final String DRUG = "DRUG";
    public static final String SURGERY = "SURGERY";
    public static final String ANATOMY = "ANATOMY";
    public static final String IMAGING = "IMAGING";
    public static final String LAB = "LAB";

    private EmrLabelType()
    {
    }

    public static boolean isValid(String type)
    {
        if (type == null)
        {
            return false;
        }
        return DISEASE.equals(type) || DRUG.equals(type) || SURGERY.equals(type)
            || ANATOMY.equals(type) || IMAGING.equals(type) || LAB.equals(type);
    }
}
