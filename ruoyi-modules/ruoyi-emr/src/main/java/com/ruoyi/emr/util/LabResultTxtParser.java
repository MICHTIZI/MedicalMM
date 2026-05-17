package com.ruoyi.emr.util;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.emr.domain.MedicalLabResult;

/**
 * 将固定格式的检验结果 TXT 解析为 {@link MedicalLabResult}（不含 patientId）。
 */
public final class LabResultTxtParser
{
    private static final Pattern FIRST_NUMBER = Pattern.compile("(\\d+(?:\\.\\d+)?)");

    private LabResultTxtParser()
    {
    }

    public static MedicalLabResult parse(byte[] rawBytes)
    {
        String text = new String(rawBytes, StandardCharsets.UTF_8);
        if (text.startsWith("\uFEFF"))
        {
            text = text.substring(1);
        }
        return parse(text);
    }

    public static MedicalLabResult parse(String text)
    {
        if (StringUtils.isBlank(text))
        {
            throw new ServiceException("导入文件为空");
        }
        MedicalLabResult lab = new MedicalLabResult();
        String[] lines = text.split("\\R");
        for (String line : lines)
        {
            line = line.trim();
            if (line.isEmpty())
            {
                continue;
            }
            if (tryDate(lab, line, "检验日期:") || tryDate(lab, line, "检验日期："))
            {
                continue;
            }
            if (line.startsWith("检验医生:") || line.startsWith("检验医生："))
            {
                lab.setTestDoctor(trimValueAfterColon(line));
                continue;
            }
            if (line.startsWith("检验科室:") || line.startsWith("检验科室："))
            {
                lab.setTestDepartment(trimValueAfterColon(line));
                continue;
            }
            if (line.startsWith("体温:") || line.startsWith("体温："))
            {
                lab.setTemperature(firstBigDecimal(line));
                continue;
            }
            if (line.startsWith("心率:") || line.startsWith("心率："))
            {
                lab.setHeartRate(firstInt(line));
                continue;
            }
            if (line.startsWith("呼吸频率:") || line.startsWith("呼吸频率："))
            {
                lab.setRespiratoryRate(firstInt(line));
                continue;
            }
            if (line.startsWith("收缩压:") || line.startsWith("收缩压："))
            {
                lab.setSystolicBp(firstInt(line));
                continue;
            }
            if (line.startsWith("舒张压:") || line.startsWith("舒张压："))
            {
                lab.setDiastolicBp(firstInt(line));
                continue;
            }
            if (line.startsWith("血氧饱和度:") || line.startsWith("血氧饱和度："))
            {
                lab.setSpo2(firstBigDecimal(line));
                continue;
            }
            if (line.startsWith("白细胞计数:") || line.startsWith("白细胞计数："))
            {
                lab.setWbc(firstBigDecimal(line));
                continue;
            }
            if (line.startsWith("中性粒细胞比例:") || line.startsWith("中性粒细胞比例："))
            {
                lab.setNeutrophilRatio(firstBigDecimal(line));
                continue;
            }
            if (line.startsWith("淋巴细胞比例:") || line.startsWith("淋巴细胞比例："))
            {
                lab.setLymphocyteRatio(firstBigDecimal(line));
                continue;
            }
            if (line.startsWith("单核细胞比例:") || line.startsWith("单核细胞比例："))
            {
                lab.setMonocyteRatio(firstBigDecimal(line));
                continue;
            }
            if (line.startsWith("血小板计数:") || line.startsWith("血小板计数："))
            {
                lab.setPlatelet(firstInt(line));
                continue;
            }
            if (line.contains("C反应蛋白(CRP):") || line.contains("CRP):"))
            {
                lab.setCrp(firstBigDecimal(line));
                continue;
            }
            if (line.contains("降钙素原(PCT):") || line.contains("PCT):"))
            {
                lab.setPct(firstBigDecimal(line));
                continue;
            }
            if (line.contains("红细胞沉降率(ESR):") || line.contains("ESR):"))
            {
                lab.setEsr(firstInt(line));
                continue;
            }
            if (line.contains("血液酸碱度"))
            {
                lab.setPh(firstBigDecimal(line));
                continue;
            }
            if (line.contains("动脉血氧分压"))
            {
                lab.setPo2(firstBigDecimal(line));
                continue;
            }
            if (line.contains("二氧化碳分压"))
            {
                lab.setPco2(firstBigDecimal(line));
                continue;
            }
            if (line.contains("碳酸氢根"))
            {
                lab.setHco3(firstBigDecimal(line));
                continue;
            }
        }
        if (lab.getTestDate() == null)
        {
            throw new ServiceException("检验 TXT 中缺少检验日期（需以「检验日期:」开头的行）");
        }
        return lab;
    }

    private static boolean tryDate(MedicalLabResult lab, String line, String prefix)
    {
        if (!line.startsWith(prefix))
        {
            return false;
        }
        String raw = trimValueAfterColon(line);
        Date d = parseDateTime(raw);
        lab.setTestDate(d);
        return true;
    }

    private static String trimValueAfterColon(String line)
    {
        int idx = line.indexOf(':');
        if (idx < 0)
        {
            idx = line.indexOf('：');
        }
        if (idx < 0)
        {
            return "";
        }
        String v = line.substring(idx + 1).trim();
        int paren = v.indexOf('(');
        if (paren >= 0)
        {
            v = v.substring(0, paren).trim();
        }
        return v;
    }

    private static BigDecimal firstBigDecimal(String line)
    {
        String v = substringAfterColon(line);
        Matcher m = FIRST_NUMBER.matcher(v);
        if (!m.find())
        {
            return null;
        }
        return new BigDecimal(m.group(1));
    }

    private static Integer firstInt(String line)
    {
        String v = substringAfterColon(line);
        Matcher m = FIRST_NUMBER.matcher(v);
        if (!m.find())
        {
            return null;
        }
        return Integer.valueOf((int) Math.round(Double.parseDouble(m.group(1))));
    }

    private static String substringAfterColon(String line)
    {
        int idx = line.indexOf(':');
        if (idx < 0)
        {
            idx = line.indexOf('：');
        }
        if (idx < 0)
        {
            return line;
        }
        return line.substring(idx + 1);
    }

    private static Date parseDateTime(String raw)
    {
        if (StringUtils.isBlank(raw))
        {
            throw new ServiceException("检验日期值为空");
        }
        String[] patterns = {"yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd HH:mm"};
        for (String p : patterns)
        {
            try
            {
                return new SimpleDateFormat(p).parse(raw);
            }
            catch (ParseException ignored)
            {
            }
        }
        throw new ServiceException("无法解析检验日期：" + raw);
    }
}
