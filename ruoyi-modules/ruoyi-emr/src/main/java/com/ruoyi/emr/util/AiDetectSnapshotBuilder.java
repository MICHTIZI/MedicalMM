package com.ruoyi.emr.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.ruoyi.emr.domain.vo.AiDetectResponseVo;
import com.ruoyi.emr.domain.vo.AiLesionVo;

/**
 * Builds fixed-format YOLO detect JSON for persistence (subset of detect API contract).
 */
public final class AiDetectSnapshotBuilder
{
    private AiDetectSnapshotBuilder()
    {
    }

    public static Map<String, Object> build(AiDetectResponseVo detect)
    {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("code", detect.getCode() != null ? detect.getCode() : 200);
        root.put("msg", detect.getMsg() != null ? detect.getMsg() : "OK");
        List<AiLesionVo> lesions = detect.getLesionList() != null ? detect.getLesionList() : new ArrayList<>();
        root.put("lesion_count", detect.getLesionCount() != null ? detect.getLesionCount() : lesions.size());
        root.put("lesion_list", copyLesionList(lesions));
        if (detect.getOriginal() != null)
        {
            root.put("original", detect.getOriginal());
        }
        if (detect.getAiResultPath() != null)
        {
            root.put("ai_result", detect.getAiResultPath());
        }
        if (detect.getDiagnosis() != null)
        {
            root.put("diagnosis", detect.getDiagnosis());
        }
        if (detect.getTotalInfectionArea() != null)
        {
            root.put("total_infection_area", detect.getTotalInfectionArea());
        }
        if (detect.getInfectionRate() != null)
        {
            root.put("infection_rate", detect.getInfectionRate());
        }
        return root;
    }

    private static List<Map<String, Object>> copyLesionList(List<AiLesionVo> lesions)
    {
        List<Map<String, Object>> list = new ArrayList<>();
        for (AiLesionVo l : lesions)
        {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("x1", l.getX1());
            m.put("y1", l.getY1());
            m.put("x2", l.getX2());
            m.put("y2", l.getY2());
            m.put("confidence", l.getConfidence());
            m.put("width", l.getWidth());
            m.put("height", l.getHeight());
            m.put("area", l.getArea());
            m.put("position", l.getPosition());
            m.put("lobe", l.getLobe());
            m.put("full_position", l.getFullPosition());
            list.add(m);
        }
        return list;
    }
}
