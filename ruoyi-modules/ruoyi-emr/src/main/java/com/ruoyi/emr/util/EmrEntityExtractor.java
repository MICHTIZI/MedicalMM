package com.ruoyi.emr.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.ruoyi.emr.domain.EmrEntity;
import com.ruoyi.emr.domain.EmrLabelType;

/**
 * Rule-based NER demo; replace with NLP service if needed.
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
            Pattern.compile("(?:\u8bca\u65ad|\u5165\u9662\u8bca\u65ad|\u51fa\u9662\u8bca\u65ad)[:\\uFF1a]\\s*([^\\n\u3002;\\uFF1B]+)"), now);
        addRegex(list, plainText, EmrLabelType.DRUG,
            Pattern.compile("(?:\u7528\u836f|\u670d\u7528|\u4e88|\u7ed9\u4e88)[:\\uFF1a]?\\s*([\\u4e00-\\u9fa5A-Za-z0-9\\-]+)"), now);
        addRegex(list, plainText, EmrLabelType.SURGERY,
            Pattern.compile("(?:\u624b\u672f\u540d\u79f0|\u672f\u5f0f|\u884c)[:\\uFF1a]?\\s*([^\\n\u3002]+?)(\u672f|\u5207\u9664|\u6210\u5f62)"), now);
        addRegex(list, plainText, EmrLabelType.ANATOMY,
            Pattern.compile("(?:\u90e8\u4f4d|\u89e3\u5256)[:\\uFF1a]\\s*([^\\n\u3002,\\uFF0c;\\uFF1B]+)"), now);
        addRegex(list, plainText, EmrLabelType.IMAGING,
            Pattern.compile("(?:CT|MRI|DR|X\u7ebf|\u8d85\u58f0|\u5f71\u50cf)[:\\uFF1a]?\\s*([^\\n\u3002]+)"), now);
        addRegex(list, plainText, EmrLabelType.LAB,
            Pattern.compile("(?:\u68c0\u9a8c|\u5b9e\u9a8c\u5ba4|\u8840\u5e38\u89c4|\u751f\u5316)[:\\uFF1a]?\\s*([^\\n\u3002]+)"), now);
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
