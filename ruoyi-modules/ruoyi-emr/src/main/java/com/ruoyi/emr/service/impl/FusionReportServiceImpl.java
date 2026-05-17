package com.ruoyi.emr.service.impl;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.emr.config.EmrFusionProperties;
import com.ruoyi.emr.config.MinioConfig;
import com.ruoyi.emr.domain.ChestXray;
import com.ruoyi.emr.domain.MedicalRecord;
import com.ruoyi.emr.domain.dto.FusionAnalyzeRequest;
import com.ruoyi.emr.domain.dto.FusionExportRequest;
import com.ruoyi.emr.mapper.MedicalPatientMapper;
import com.ruoyi.emr.service.IAiImageAnalysisService;
import com.ruoyi.emr.service.IChestXrayService;
import com.ruoyi.emr.service.IFusionReportService;
import com.ruoyi.emr.service.IMedicalPatientDiagnosisService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;

@Service
public class FusionReportServiceImpl implements IFusionReportService
{
    @Autowired
    private EmrFusionProperties fusionProperties;

    @Autowired
    private IChestXrayService chestXrayService;

    @Autowired
    private IAiImageAnalysisService aiImageAnalysisService;

    @Autowired
    private MedicalPatientMapper medicalPatientMapper;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private IMedicalPatientDiagnosisService medicalPatientDiagnosisService;

    private RestTemplate fusionRestTemplate()
    {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(8000);
        f.setReadTimeout(120000);
        return new RestTemplate(f);
    }

    @Override
    public Map<String, Object> fusionAnalyze(Long imageId, FusionAnalyzeRequest body)
    {
        ChestXray xray = chestXrayService.getById(imageId);
        checkXrayAccess(xray);
        if (body == null || body.getImageResult() == null || body.getImageResult().isEmpty())
        {
            throw new ServiceException("请提供影像 AI 分析结果（imageResult）");
        }
        if (StringUtils.isEmpty(body.getCaseText()))
        {
            throw new ServiceException("请提供病历文本（case_text）");
        }
        Map<String, Object> fusionBody = new LinkedHashMap<>();
        fusionBody.put("image_result", body.getImageResult());
        fusionBody.put("case_text", body.getCaseText());
        fusionBody.put("lab_data", body.getLabData() != null ? body.getLabData() : new LinkedHashMap<>());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(fusionBody, headers);
        try
        {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> resp = fusionRestTemplate().postForEntity(fusionProperties.analyzeUrl(), entity, Map.class);
            Map<String, Object> out = resp.getBody();
            if (out == null)
            {
                throw new ServiceException("融合服务返回内容为空");
            }
            Object code = out.get("code");
            boolean ok = false;
            if (code instanceof Number)
            {
                ok = ((Number) code).intValue() == 200;
            }
            else if (code != null)
            {
                ok = "200".equals(String.valueOf(code));
            }
            if (!ok)
            {
                throw new ServiceException(String.valueOf(out.getOrDefault("msg", "融合分析失败")));
            }
            return out;
        }
        catch (RestClientException e)
        {
            throw new ServiceException("无法连接融合分析服务（" + fusionProperties.getBaseUrl() + "）：" + e.getMessage());
        }
    }

    @Override
    public void exportFusionWord(Long imageId, FusionExportRequest request, HttpServletResponse response)
    {
        if (request == null || StringUtils.isEmpty(request.getDoctorSignature()))
        {
            throw new ServiceException("请填写医生签名");
        }
        if (request.getFusionResponse() == null)
        {
            throw new ServiceException("缺少融合分析结果");
        }
        ChestXray xray = chestXrayService.getById(imageId);
        checkXrayAccess(xray);
        MedicalRecord record = aiImageAnalysisService.getGeneratedRecord(imageId);
        String patientName = safeText(xray.getPatientName(), "未知患者");
        String timeText = DateUtils.parseDateToStr("yyyyMMddHHmmss", new Date());
        String fileName = "多模态辅助诊断报告_" + patientName + "_" + timeText + ".docx";

        try (XWPFDocument doc = new XWPFDocument())
        {
            addTitle(doc, "肺炎多模态辅助诊断报告");
            addMeta(doc, "患者姓名", patientName);
            addMeta(doc, "检查时间", DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", xray.getCreateTime()));
            addMeta(doc, "报告生成时间", DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", new Date()));

            addImageSection(doc, xray, record);
            addAiLesionSection(doc, request.getImageResult());
            addSection(doc, "电子病历（文本）", request.getCaseText());
            addLabDataSection(doc, request.getLabData());
            addFusionResultSection(doc, request.getFusionResponse());

            addSection(doc, "医生签名", request.getDoctorSignature());
            addSection(doc, "医生建议（选填）",
                StringUtils.isNotEmpty(request.getDoctorAdvice()) ? request.getDoctorAdvice() : "（未填写）");

            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20"));
            doc.write(response.getOutputStream());
            if (xray.getPatientId() != null)
            {
                medicalPatientDiagnosisService.markReportExported(xray.getPatientId());
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("导出 Word 失败：" + e.getMessage());
        }
    }

    private void addImageSection(XWPFDocument doc, ChestXray xray, MedicalRecord record)
    {
        String imagePath = safeText(record != null ? record.getAiResultPath() : null, deriveAiResultPath(xray.getImagePath()));
        addSection(doc, "AI 标注图（MinIO）", "");
        if (imagePath == null || imagePath.isEmpty())
        {
            addParagraph(doc, "（无路径）");
            return;
        }
        String objectPath = normalizeObjectPath(imagePath);
        try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(objectPath)
                .build()))
        {
            XWPFParagraph paragraph = doc.createParagraph();
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun run = paragraph.createRun();
            run.addPicture(inputStream, pictureType(objectPath), objectPath, Units.toEMU(480), Units.toEMU(360));
        }
        catch (Exception e)
        {
            addParagraph(doc, "读取 AI 标注图失败：" + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void addAiLesionSection(XWPFDocument doc, Map<String, Object> imageResult)
    {
        addSection(doc, "AI 病灶结果", "");
        if (imageResult == null || imageResult.isEmpty())
        {
            addParagraph(doc, "（无）");
            return;
        }
        Object lc = imageResult.get("lesion_count");
        if (lc != null)
        {
            addParagraph(doc, "病灶数量：" + lc);
        }
        Object diag = imageResult.get("diagnosis");
        if (diag != null)
        {
            addParagraph(doc, "诊断意见：" + diag);
        }
        List<Map<String, Object>> list = null;
        Object ll = imageResult.get("lesion_list");
        if (ll instanceof List)
        {
            list = (List<Map<String, Object>>) ll;
        }
        if (list == null || list.isEmpty())
        {
            addParagraph(doc, "（无病灶框数据）");
            return;
        }
        int i = 1;
        for (Map<String, Object> m : list)
        {
            if (m == null)
            {
                continue;
            }
            addParagraph(doc, "病灶 " + i + "：" + str(m.get("full_position")) + " 置信度 "
                    + formatConf(m.get("confidence")) + " 框(" + str(m.get("x1")) + "," + str(m.get("y1")) + ")-("
                    + str(m.get("x2")) + "," + str(m.get("y2")) + ")");
            i++;
        }
    }

    @SuppressWarnings("unchecked")
    private void addLabDataSection(XWPFDocument doc, Map<String, Object> labData)
    {
        addSection(doc, "检验对照数据", "");
        if (labData == null || labData.isEmpty())
        {
            addParagraph(doc, "（无）");
            return;
        }
        for (Map.Entry<String, Object> cat : labData.entrySet())
        {
            addParagraph(doc, "【" + cat.getKey() + "】");
            Object v = cat.getValue();
            if (!(v instanceof Map))
            {
                addParagraph(doc, String.valueOf(v));
                continue;
            }
            Map<String, Object> inner = (Map<String, Object>) v;
            for (Map.Entry<String, Object> e : inner.entrySet())
            {
                addParagraph(doc, e.getKey() + "：" + String.valueOf(e.getValue()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addFusionResultSection(XWPFDocument doc, Map<String, Object> fusionResponse)
    {
        addSection(doc, "多模态融合决策结果", "");
        if (fusionResponse == null)
        {
            addParagraph(doc, "（无）");
            return;
        }
        Object data = fusionResponse.get("data");
        if (!(data instanceof Map))
        {
            addParagraph(doc, fusionResponse.toString());
            return;
        }
        Map<String, Object> d = (Map<String, Object>) data;

        Object ms = d.get("modality_scores");
        if (ms instanceof Map)
        {
            Map<String, Object> m = (Map<String, Object>) ms;
            addParagraph(doc, "单模态评分：");
            appendModalityScore(doc, m, "image", "ImgS");
            appendModalityScore(doc, m, "case", "CaseS");
            appendModalityScore(doc, m, "lab", "LabS");
        }

        Object cc = d.get("consistency_check");
        if (cc instanceof Map)
        {
            Map<String, Object> c = (Map<String, Object>) cc;
            addParagraph(doc, "一致性校验：" + str(c.get("level")));
            Object rules = c.get("conflict_rules");
            if (rules instanceof List)
            {
                int n = 1;
                for (Object o : (List<?>) rules)
                {
                    if (!(o instanceof Map))
                    {
                        continue;
                    }
                    Map<String, Object> r = (Map<String, Object>) o;
                    addParagraph(doc, "  " + n + ". " + str(r.get("rule_name")) + " - " + str(r.get("report_text")));
                    n++;
                }
            }
        }

        Object fc = d.get("fusion_calculation");
        if (fc instanceof Map)
        {
            Map<String, Object> f = (Map<String, Object>) fc;
            addParagraph(doc, "综合评分：原始 " + str(f.get("raw_score")) + " 置信 "
                    + str(f.get("confidence_factor")) + " 最终 " + str(f.get("final_score")));
            Object w = f.get("weights");
            if (w instanceof Map)
            {
                Map<String, Object> wm = (Map<String, Object>) w;
                addParagraph(doc, "权重 ImgS=" + str(wm.get("ImgS")) + " CaseS=" + str(wm.get("CaseS")) + " LabS=" + str(wm.get("LabS")));
            }
        }

        Object diag = d.get("diagnosis_output");
        if (diag instanceof Map)
        {
            Map<String, Object> dg = (Map<String, Object>) diag;
            addParagraph(doc, "诊断输出：" + str(dg.get("grade_cn")) + "(" + str(dg.get("grade")) + ") "
                    + str(dg.get("severity_cn")) + "(" + str(dg.get("severity")) + ")");
            addParagraph(doc, str(dg.get("action")));
        }

        Object sg = d.get("structured_suggestions");
        if (sg instanceof List)
        {
            addParagraph(doc, "结构化建议：");
            int i = 1;
            for (Object o : (List<?>) sg)
            {
                if (!(o instanceof Map))
                {
                    continue;
                }
                Map<String, Object> s = (Map<String, Object>) o;
                addParagraph(doc, "  " + i + ". [" + str(s.get("priority")) + "] [" + str(s.get("category")) + "] " + str(s.get("content")));
                i++;
            }
        }
    }

    private void appendModalityScore(XWPFDocument doc, Map<String, Object> m, String key, String label)
    {
        Object sub = m.get(key);
        if (!(sub instanceof Map))
        {
            return;
        }
        Map<String, Object> mm = (Map<String, Object>) sub;
        addParagraph(doc, "  " + label + ": " + str(mm.get("score")) + "/10");
    }

    private String deriveAiResultPath(String imagePath)
    {
        if (imagePath == null || imagePath.isEmpty())
        {
            return "";
        }
        String normalized = normalizeObjectPath(imagePath);
        String filename = normalized.substring(normalized.lastIndexOf('/') + 1);
        return filename.isEmpty() ? "" : "result/" + filename;
    }

    private int pictureType(String path)
    {
        String lower = path.toLowerCase();
        if (lower.endsWith(".png"))
        {
            return XWPFDocument.PICTURE_TYPE_PNG;
        }
        if (lower.endsWith(".gif"))
        {
            return XWPFDocument.PICTURE_TYPE_GIF;
        }
        if (lower.endsWith(".bmp"))
        {
            return XWPFDocument.PICTURE_TYPE_BMP;
        }
        return XWPFDocument.PICTURE_TYPE_JPEG;
    }

    private String normalizeObjectPath(String path)
    {
        if (path == null)
        {
            return "";
        }
        String normalized = path.replace("\\", "/");
        while (normalized.startsWith("/"))
        {
            normalized = normalized.substring(1);
        }
        String bucketPrefix = minioConfig.getBucketName() + "/";
        if (normalized.startsWith(bucketPrefix))
        {
            normalized = normalized.substring(bucketPrefix.length());
        }
        return normalized;
    }

    private void addTitle(XWPFDocument doc, String text)
    {
        XWPFParagraph paragraph = doc.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        run.setBold(true);
        run.setFontSize(18);
        run.setText(text);
    }

    private void addMeta(XWPFDocument doc, String label, String value)
    {
        XWPFParagraph paragraph = doc.createParagraph();
        XWPFRun labelRun = paragraph.createRun();
        labelRun.setBold(true);
        labelRun.setText(label + "：");
        XWPFRun valueRun = paragraph.createRun();
        valueRun.setText(safeText(value, "暂无"));
    }

    private void addSection(XWPFDocument doc, String title, String content)
    {
        XWPFParagraph titleParagraph = doc.createParagraph();
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setBold(true);
        titleRun.setFontSize(13);
        titleRun.setText(title);

        if (content != null && !content.isEmpty())
        {
            XWPFParagraph contentParagraph = doc.createParagraph();
            XWPFRun contentRun = contentParagraph.createRun();
            contentRun.setText(content);
        }
    }

    private void addParagraph(XWPFDocument doc, String text)
    {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text == null ? "" : text);
    }

    private String safeText(String value, String fallback)
    {
        return value == null || value.isEmpty() ? fallback : value;
    }

    private String str(Object o)
    {
        return o == null ? "" : String.valueOf(o);
    }

    private String formatConf(Object c)
    {
        if (c == null)
        {
            return "-";
        }
        try
        {
            double v = ((Number) c).doubleValue();
            return String.format("%.1f%%", v * 100);
        }
        catch (Exception e)
        {
            return String.valueOf(c);
        }
    }

    private void checkXrayAccess(ChestXray xray)
    {
        if (xray == null)
        {
            throw new ServiceException("影像不存在");
        }
        if (xray.getPatientId() == null)
        {
            throw new ServiceException("影像未绑定患者");
        }
        if (!SecurityUtils.isAdmin())
        {
            Long userId = SecurityUtils.getUserId();
            if (userId == null)
            {
                throw new ServiceException("无访问权限");
            }
            com.ruoyi.emr.domain.MedicalPatient patient = medicalPatientMapper.selectMedicalPatientByPatientId(xray.getPatientId());
            if (patient == null || patient.getAttendingDoctorId() == null || !patient.getAttendingDoctorId().equals(userId))
            {
                throw new ServiceException("无权访问该患者的影像");
            }
        }
    }
}
