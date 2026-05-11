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
 * Parses fixed-format lab result TXT exports into {@link MedicalLabResult} (patientId not set).
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
            throw new ServiceException("Import file is empty");
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
            if (tryDate(lab, line, "\u68c0\u9a8c\u65e5\u671f:"))
            {
                continue;
            }
            if (line.startsWith("\u68c0\u9a8c\u533b\u751f:") || line.startsWith("\u68c0\u9a8c\u533b\u751f\uFF1A"))
            {
                lab.setTestDoctor(trimValueAfterColon(line));
                continue;
            }
            if (line.startsWith("\u68c0\u9a8c\u79d1\u5ba4:") || line.startsWith("\u68c0\u9a8c\u79d1\u5ba4\uFF1A"))
            {
                lab.setTestDepartment(trimValueAfterColon(line));
                continue;
            }
            if (line.startsWith("\u4f53\u6e29:") || line.startsWith("\u4f53\u6e29\uFF1A"))
            {
                lab.setTemperature(firstBigDecimal(line));
                continue;
            }
            if (line.startsWith("\u5fc3\u7387:") || line.startsWith("\u5fc3\u7387\uFF1A"))
            {
                lab.setHeartRate(firstInt(line));
                continue;
            }
            if (line.startsWith("\u547c\u5438\u9891\u7387:") || line.startsWith("\u547c\u5438\u9891\u7387\uFF1A"))
            {
                lab.setRespiratoryRate(firstInt(line));
                continue;
            }
            if (line.startsWith("\u6536\u7f29\u538b:") || line.startsWith("\u6536\u7f29\u538b\uFF1A"))
            {
                lab.setSystolicBp(firstInt(line));
                continue;
            }
            if (line.startsWith("\u8212\u5f20\u538b:") || line.startsWith("\u8212\u5f20\u538b\uFF1A"))
            {
                lab.setDiastolicBp(firstInt(line));
                continue;
            }
            if (line.startsWith("\u8840\u6c27\u9971\u548c\u5ea6:") || line.startsWith("\u8840\u6c27\u9971\u548c\u5ea6\uFF1A"))
            {
                lab.setSpo2(firstBigDecimal(line));
                continue;
            }
            if (line.startsWith("\u767d\u7ec6\u80de\u8ba1\u6570:") || line.startsWith("\u767d\u7ec6\u80de\u8ba1\u6570\uFF1A"))
            {
                lab.setWbc(firstBigDecimal(line));
                continue;
            }
            if (line.startsWith("\u4e2d\u6027\u7c92\u7ec6\u80de\u6bd4\u4f8b:") || line.startsWith("\u4e2d\u6027\u7c92\u7ec6\u80de\u6bd4\u4f8b\uFF1A"))
            {
                lab.setNeutrophilRatio(firstBigDecimal(line));
                continue;
            }
            if (line.startsWith("\u6dcb\u5df4\u7ec6\u80de\u6bd4\u4f8b:") || line.startsWith("\u6dcb\u5df4\u7ec6\u80de\u6bd4\u4f8b\uFF1A"))
            {
                lab.setLymphocyteRatio(firstBigDecimal(line));
                continue;
            }
            if (line.startsWith("\u5355\u6838\u7ec6\u80de\u6bd4\u4f8b:") || line.startsWith("\u5355\u6838\u7ec6\u80de\u6bd4\u4f8b\uFF1A"))
            {
                lab.setMonocyteRatio(firstBigDecimal(line));
                continue;
            }
            if (line.startsWith("\u8840\u5c0f\u677f\u8ba1\u6570:") || line.startsWith("\u8840\u5c0f\u677f\u8ba1\u6570\uFF1A"))
            {
                lab.setPlatelet(firstInt(line));
                continue;
            }
            if (line.contains("C\u53cd\u5e94\u86cb\u767d(CRP):") || line.contains("CRP):"))
            {
                lab.setCrp(firstBigDecimal(line));
                continue;
            }
            if (line.contains("\u964d\u9499\u7d20\u539f(PCT):") || line.contains("PCT):"))
            {
                lab.setPct(firstBigDecimal(line));
                continue;
            }
            if (line.contains("\u7ea2\u7ec6\u80de\u6c89\u964d\u7387(ESR):") || line.contains("ESR):"))
            {
                lab.setEsr(firstInt(line));
                continue;
            }
            if (line.contains("\u8840\u6db2\u9178\u78b1\u5ea6"))
            {
                lab.setPh(firstBigDecimal(line));
                continue;
            }
            if (line.contains("\u52a8\u8109\u8840\u6c27\u5206\u538b"))
            {
                lab.setPo2(firstBigDecimal(line));
                continue;
            }
            if (line.contains("\u4e8c\u6c27\u5316\u78b3\u5206\u538b"))
            {
                lab.setPco2(firstBigDecimal(line));
                continue;
            }
            if (line.contains("\u786b\u9178\u6c22\u6839"))
            {
                lab.setHco3(firstBigDecimal(line));
                continue;
            }
        }
        if (lab.getTestDate() == null)
        {
            throw new ServiceException("Test date missing in TXT (need line starting with examination date)");
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
            idx = line.indexOf('\uFF1A');
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
            idx = line.indexOf('\uFF1A');
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
            throw new ServiceException("Test date value is empty");
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
        throw new ServiceException("Cannot parse test date: " + raw);
    }
}
