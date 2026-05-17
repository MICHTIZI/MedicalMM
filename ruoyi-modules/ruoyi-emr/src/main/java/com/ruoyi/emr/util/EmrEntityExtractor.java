package com.ruoyi.emr.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.ruoyi.emr.domain.EmrEntity;
import com.ruoyi.emr.domain.EmrLabelType;

/**
 * 基于规则的实体抽取示例；需要时可替换为 NLP 服务。
 */
public final class EmrEntityExtractor
{
    private EmrEntityExtractor()
    {
    }

    public static List<EmrEntity> extract(String plainText)
    {
        List<EmrEntity> list = new ArrayList<>();
        if (plainText == null || plainText.isEmpty())
        {
            return list;
        }
        Date now = new Date();
        addRegex(list, plainText, EmrLabelType.DISEASE,
            Pattern.compile("(?:诊断|入院诊断|出院诊断)[:：]\\s*([^\\n。;；]+)"), now);
        addRegex(list, plainText, EmrLabelType.DRUG,
            Pattern.compile("(?:用药|服用|予|给予)[:：]?\\s*([\\u4e00-\\u9fa5A-Za-z0-9\\-]+)"), now);
        addRegex(list, plainText, EmrLabelType.SURGERY,
            Pattern.compile("(?:手术名称|术式|行)[:：]?\\s*([^\\n。]+?)(术|切除|成形)"), now);
        addRegex(list, plainText, EmrLabelType.ANATOMY,
            Pattern.compile("(?:部位|解剖)[:：]\\s*([^\\n。,，;；]+)"), now);
        addRegex(list, plainText, EmrLabelType.IMAGING,
            Pattern.compile("(?:CT|MRI|DR|X线|超声|影像)[:：]?\\s*([^\\n。]+)"), now);
        addRegex(list, plainText, EmrLabelType.LAB,
            Pattern.compile("(?:检验|实验室|血常规|生化)[:：]?\\s*([^\\n。]+)"), now);
        return list;
    }

    private static void addRegex(List<EmrEntity> list, String text, String labelType, Pattern p, Date now)
    {
        Matcher m = p.matcher(text);
        while (m.find())
        {
            int gs = m.groupCount() >= 1 ? m.start(1) : m.start();
            int ge = m.groupCount() >= 1 ? m.end(1) : m.end();
            if (ge <= gs)
            {
                continue;
            }
            String ent = text.substring(gs, ge).trim();
            if (ent.length() > 80 || ent.length() < 1)
            {
                continue;
            }
            EmrEntity e = new EmrEntity();
            e.setLabelType(labelType);
            e.setEntityText(ent);
            e.setStartPos(gs);
            e.setEndPos(ge);
            e.setCreateTime(now);
            list.add(e);
        }
    }
}
