package com.ruoyi.imaging.domain;

/**
 * 14 种胸部疾病标签常量。
 */
public final class DiseaseLabel
{
    private DiseaseLabel() {}

    public static final String[] LABELS = {
        "\u80BA\u4E0D\u5F20",          // 肺不张
        "\u5FC3\u810F\u80A5\u5927",    // 心脏肥大
        "\u80F8\u8154\u79EF\u6DB2",    // 胸腔积液
        "\u6D78\u6DA6\u5F71",          // 浸润影
        "\u80BA\u80BF\u5757",          // 肺肿块
        "\u80BA\u7ED3\u8282",          // 肺结节
        "\u80BA\u708E",                // 肺炎
        "\u6C14\u80F8",                // 气胸
        "\u80BA\u5B9E\u53D8",          // 肺实变
        "\u6C34\u80BF",                // 水肿
        "\u80BA\u6C14\u80BF",          // 肺气肿
        "\u7EA4\u7EF4\u5316",          // 纤维化
        "\u80F8\u819C\u589E\u539A",    // 胸膜增厚
        "\u75DD"                       // 疝
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
