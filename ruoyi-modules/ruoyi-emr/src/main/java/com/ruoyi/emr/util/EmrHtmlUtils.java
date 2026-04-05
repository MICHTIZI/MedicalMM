package com.ruoyi.emr.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import com.ruoyi.emr.domain.EmrEntity;

public final class EmrHtmlUtils
{
    private EmrHtmlUtils()
    {
    }

    public static String toPlainText(String htmlOrText)
    {
        if (htmlOrText == null || htmlOrText.isEmpty())
        {
            return "";
        }
        if (!htmlOrText.trim().startsWith("<"))
        {
            return htmlOrText;
        }
        Document doc = Jsoup.parse(htmlOrText);
        Element body = doc.body();
        String t = body.text();
        return t == null ? "" : t;
    }

    /** Highlight by offsets on plain text. */
    public static String highlightPlain(String plain, List<EmrEntity> entities)
    {
        if (plain == null || plain.isEmpty())
        {
            return "";
        }
        if (entities == null || entities.isEmpty())
        {
            return escapeHtml(plain);
        }
        List<EmrEntity> sorted = new ArrayList<>(entities);
        sorted.sort(Comparator.comparing(EmrEntity::getStartPos, Comparator.nullsLast(Integer::compareTo)));
        StringBuilder sb = new StringBuilder();
        int cursor = 0;
        for (EmrEntity e : sorted)
        {
            if (e.getStartPos() == null || e.getEndPos() == null)
            {
                continue;
            }
            int s = e.getStartPos();
            int en = e.getEndPos();
            if (s < 0 || en > plain.length() || s >= en)
            {
                continue;
            }
            if (cursor < s)
            {
                sb.append(escapeHtml(plain.substring(cursor, s)));
            }
            String cls = e.getLabelType() != null ? e.getLabelType().toLowerCase() : "entity";
            sb.append("<mark class=\"emr-entity emr-").append(escapeAttr(cls)).append("\">");
            sb.append(escapeHtml(plain.substring(s, en)));
            sb.append("</mark>");
            cursor = en;
        }
        if (cursor < plain.length())
        {
            sb.append(escapeHtml(plain.substring(cursor)));
        }
        return sb.toString();
    }

    private static String escapeHtml(String s)
    {
        if (s == null)
        {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String escapeAttr(String s)
    {
        if (s == null)
        {
            return "";
        }
        return s.replaceAll("[^a-zA-Z0-9_-]", "");
    }
}
