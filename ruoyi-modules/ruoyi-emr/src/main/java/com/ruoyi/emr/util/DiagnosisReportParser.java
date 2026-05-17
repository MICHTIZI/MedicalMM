package com.ruoyi.emr.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.emr.domain.vo.DiagnosisReportStructVo;

/**
 * Parses five-section Chinese diagnosis report text into {@link DiagnosisReportStructVo}.
 */
public final class DiagnosisReportParser
{
    private static final Pattern SECTION_HEAD = Pattern.compile("(?m)^[\u4e00\u4e8c\u4e09\u56db\u4e94]\u3001(.+)$");
    private static final Pattern NUMBERED_SUB = Pattern.compile("(?m)^\\d+\\.\\s*(.+)$");
    private static final Pattern KV_LINE = Pattern.compile("^([^\\uFF1A:\\n]+)[\\uFF1A:](.+)$");

    private DiagnosisReportParser()
    {
    }

    public static DiagnosisReportStructVo parse(String raw)
    {
        DiagnosisReportStructVo vo = new DiagnosisReportStructVo();
        if (StringUtils.isEmpty(raw))
        {
            return vo;
        }
        String text = raw.trim();
        vo.setRawText(text);

        Map<String, String> sections = splitSections(text);
        parseConclusion(vo, sections.get("\u4e00"));
        parseImaging(vo, sections.get("\u4e8c"));
        parseClinical(vo, sections.get("\u4e09"));
        parseSeverity(vo, sections.get("\u56db"));
        String summary = sections.get("\u4e94");
        if (StringUtils.isNotEmpty(summary))
        {
            vo.setSummary(summary.trim());
        }
        return vo;
    }

    private static Map<String, String> splitSections(String text)
    {
        Map<String, String> map = new LinkedHashMap<>();
        Matcher m = SECTION_HEAD.matcher(text);
        List<int[]> spans = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        while (m.find())
        {
            String title = m.group();
            String num = title.substring(0, 1);
            keys.add(num);
            spans.add(new int[] { m.start(), m.end() });
        }
        for (int i = 0; i < spans.size(); i++)
        {
            int bodyStart = spans.get(i)[1];
            int bodyEnd = (i + 1 < spans.size()) ? spans.get(i + 1)[0] : text.length();
            String body = text.substring(bodyStart, bodyEnd).trim();
            map.put(keys.get(i), body);
        }
        return map;
    }

    private static void parseConclusion(DiagnosisReportStructVo vo, String body)
    {
        if (StringUtils.isEmpty(body))
        {
            return;
        }
        DiagnosisReportStructVo.ConclusionBlock block = vo.getConclusion();
        for (String line : body.split("\\n"))
        {
            String t = line.trim();
            if (t.isEmpty())
            {
                continue;
            }
            Matcher kv = KV_LINE.matcher(t);
            if (!kv.matches())
            {
                continue;
            }
            String label = kv.group(1).trim();
            String value = kv.group(2).trim();
            if (label.contains("\u4e3b\u8981\u8bca\u65ad"))
            {
                block.setMainDiagnosis(value);
            }
            else if (label.contains("\u8bca\u65ad\u7f6e\u4fe1\u5ea6"))
            {
                block.setConfidence(value);
            }
            else if (label.contains("\u9274\u522b\u8bca\u65ad"))
            {
                block.setDifferentialDiagnosis(value);
            }
        }
    }

    private static void parseImaging(DiagnosisReportStructVo vo, String body)
    {
        if (StringUtils.isEmpty(body))
        {
            return;
        }
        DiagnosisReportStructVo.ImagingBlock block = vo.getImagingAnalysis();
        assignNumberedSubsections(body, title -> {
            if (title.contains("\u75c5\u7076\u7279\u5f81"))
            {
                return block.getLesionFeatures();
            }
            if (title.contains("\u6574\u4f53\u80ba") || title.contains("\u6574\u4f53\u80ba\u90e8"))
            {
                return block.getOverallLungAssessment();
            }
            if (title.contains("\u5176\u4ed6\u7279\u5f81"))
            {
                return block.getOtherFeatures();
            }
            return null;
        });
    }

    private static void parseClinical(DiagnosisReportStructVo vo, String body)
    {
        if (StringUtils.isEmpty(body))
        {
            return;
        }
        DiagnosisReportStructVo.ClinicalBlock block = vo.getClinicalSignificance();
        Matcher m = NUMBERED_SUB.matcher(body);
        List<int[]> spans = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        while (m.find())
        {
            titles.add(m.group(1).trim());
            spans.add(new int[] { m.start(), m.end() });
        }
        if (spans.isEmpty())
        {
            return;
        }
        for (int i = 0; i < spans.size(); i++)
        {
            int contentStart = spans.get(i)[1];
            int contentEnd = (i + 1 < spans.size()) ? spans.get(i + 1)[0] : body.length();
            String title = titles.get(i);
            String content = body.substring(contentStart, contentEnd).trim();
            if (title.contains("\u75c5\u53d8\u6027\u8d28"))
            {
                block.setLesionNatureAnalysis(extractBullets(content));
            }
            else if (title.contains("\u4e34\u5e8a\u98ce\u9669") || title.contains("\u98ce\u9669\u8bc4\u4f30"))
            {
                block.setRiskAssessment(extractRiskKv(content));
            }
        }
    }

    private static void parseSeverity(DiagnosisReportStructVo vo, String body)
    {
        if (StringUtils.isEmpty(body))
        {
            return;
        }
        DiagnosisReportStructVo.SeverityBlock block = vo.getSeverityAssessment();
        for (String line : body.split("\\n"))
        {
            String t = line.trim();
            if (t.isEmpty())
            {
                continue;
            }
            Matcher kv = KV_LINE.matcher(t);
            if (!kv.matches())
            {
                continue;
            }
            String label = kv.group(1).trim();
            String value = kv.group(2).trim();
            if (label.contains("\u4e25\u91cd\u7a0b\u5ea6") || label.contains("\u5206\u7ea7"))
            {
                block.setSeverityLevel(value);
            }
            else if (label.contains("\u4f9d\u636e"))
            {
                block.setBasis(value);
            }
        }
    }

    private interface SubsectionListTarget
    {
        List<String> targetFor(String subsectionTitle);
    }

    private static void assignNumberedSubsections(String body, SubsectionListTarget target)
    {
        Matcher m = NUMBERED_SUB.matcher(body);
        List<int[]> spans = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        while (m.find())
        {
            titles.add(m.group(1).trim());
            spans.add(new int[] { m.start(), m.end() });
        }
        for (int i = 0; i < spans.size(); i++)
        {
            int contentStart = spans.get(i)[1];
            int contentEnd = (i + 1 < spans.size()) ? spans.get(i + 1)[0] : body.length();
            String title = titles.get(i);
            String content = body.substring(contentStart, contentEnd).trim();
            List<String> list = target.targetFor(title);
            if (list != null)
            {
                list.addAll(extractBullets(content));
            }
        }
    }

    private static List<String> extractBullets(String content)
    {
        List<String> items = new ArrayList<>();
        for (String line : content.split("\\n"))
        {
            String t = line.trim();
            if (t.startsWith("-") || t.startsWith("\u2022"))
            {
                items.add(t.replaceFirst("^[-\u2022]\\s*", "").trim());
            }
            else if (!t.isEmpty() && items.isEmpty())
            {
                Matcher kv = KV_LINE.matcher(t);
                if (!kv.matches())
                {
                    items.add(t);
                }
            }
        }
        return items;
    }

    private static Map<String, String> extractRiskKv(String content)
    {
        Map<String, String> map = new LinkedHashMap<>();
        for (String line : content.split("\\n"))
        {
            String t = line.trim();
            if (t.isEmpty())
            {
                continue;
            }
            if (t.startsWith("-") || t.startsWith("\u2022"))
            {
                t = t.replaceFirst("^[-\u2022]\\s*", "").trim();
            }
            Matcher kv = KV_LINE.matcher(t);
            if (kv.matches())
            {
                map.put(kv.group(1).trim(), kv.group(2).trim());
            }
        }
        return map;
    }
}
