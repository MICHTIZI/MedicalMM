package com.ruoyi.imaging.domain;

/**
 * 14 种胸部疾病标签常量。
 */
public final class DiseaseLabel
{
    private DiseaseLabel() {}

    public static final String[] LABELS = {
        "肺不张",
        "心脏肥大",
        "胸腔积液",
        "浸润影",
        "肺肿块",
        "肺结节",
        "肺炎",
        "气胸",
        "肺实变",
        "水肿",
        "肺气肿",
        "纤维化",
        "胸膜增厚",
        "疝"
    };

    public static boolean isValid(String name)
    {
        if (name == null || name.isEmpty()) return false;
        for (String s : LABELS)
        {
            if (s.equals(name)) return true;
        }
        return false;
    }
}
