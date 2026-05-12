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
            throw new ServiceException("imageResult is required");
        }
        if (StringUtils.isEmpty(body.getCaseText()))
        {
            throw new ServiceException("case_text is required");
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
                throw new ServiceException("Fusion service returned empty body");
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
                throw new ServiceException(String.valueOf(out.getOrDefault("msg", "Fusion analyze failed")));
            }
            return out;
        }
        catch (RestClientException e)
        {
            throw new ServiceException("Cannot reach fusion service at " + fusionProperties.getBaseUrl() + ": " + e.getMessage());
        }
    }

    @Override
    public void exportFusionWord(Long imageId, FusionExportRequest request, HttpServletResponse response)
    {
        if (request == null || StringUtils.isEmpty(request.getDoctorSignature()))
        {
            throw new ServiceException("Please fill doctor signature");
        }
        if (request.getFusionResponse() == null)
        {
            throw new ServiceException("Fusion result missing");
        }
        ChestXray xray = chestXrayService.getById(imageId);
        checkXrayAccess(xray);
        MedicalRecord record = aiImageAnalysisService.getGeneratedRecord(imageId);
        String patientName = safeText(xray.getPatientName(), "\u672a\u77e5\u60a3\u8005");
        String timeText = DateUtils.parseDateToStr("yyyyMMddHHmmss", new Date());
        String fileName = "\u591a\u6a21\u6001\u8f85\u52a9\u8bca\u65ad\u62a5\u544a_" + patientName + "_" + timeText + ".docx";

        try (XWPFDocument doc = new XWPFDocument())
        {
            addTitle(doc, "\u80ba\u708e\u591a\u6a21\u6001\u8f85\u52a9\u8bca\u65ad\u62a5\u544a");
            addMeta(doc, "\u60a3\u8005\u59d3\u540d", patientName);
            addMeta(doc, "\u68c0\u67e5\u65f6\u95f4", DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", xray.getCreateTime()));
            addMeta(doc, "\u62a5\u544a\u751f\u6210\u65f6\u95f4", DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", new Date()));

            addImageSection(doc, xray, record);
            addAiLesionSection(doc, request.getImageResult());
            addSection(doc, "\u7535\u5b50\u75c5\u5386\uff08\u6587\u672c\uff09", request.getCaseText());
            addLabDataSection(doc, request.getLabData());
            addFusionResultSection(doc, request.getFusionResponse());

            addSection(doc, "\u533b\u751f\u7b7e\u540d", request.getDoctorSignature());
            addSection(doc, "\u533b\u751f\u5efa\u8bae\uff08\u9009\u586b\uff09",
                StringUtils.isNotEmpty(request.getDoctorAdvice()) ? request.getDoctorAdvice() : "\uff08\u672a\u586b\u5199\uff09");

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
            throw new ServiceException("Export Word failed: " + e.getMessage());
        }
    }

    private void addImageSection(XWPFDocument doc, ChestXray xray, MedicalRecord record)
    {
        String imagePath = safeText(record != null ? record.getAiResultPath() : null, deriveAiResultPath(xray.getImagePath()));
        addSection(doc, "AI \u6807\u6ce8\u56fe\uff08MinIO result\uff09", "");
        if (imagePath == null || imagePath.isEmpty())
        {
            addParagraph(doc, "\uff08\u65e0\u8def\u5f84\uff09");
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
            addParagraph(doc, "AI image read failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void addAiLesionSection(XWPFDocument doc, Map<String, Object> imageResult)
    {
        addSection(doc, "AI \u75c5\u7076\u7ed3\u679c", "");
        if (imageResult == null || imageResult.isEmpty())
        {
            addParagraph(doc, "\uff08\u65e0\uff09");
            return;
        }
        Object lc = imageResult.get("lesion_count");
        if (lc != null)
        {
            addParagraph(doc, "\u75c5\u7076\u6570\u91cf\uff1a" + lc);
        }
        Object diag = imageResult.get("diagnosis");
        if (diag != null)
        {
            addParagraph(doc, "\u8bca\u65ad\u610f\u89c1\uff1a" + diag);
        }
        List<Map<String, Object>> list = null;
        Object ll = imageResult.get("lesion_list");
        if (ll instanceof List)
        {
            list = (List<Map<String, Object>>) ll;
        }
        if (list == null || list.isEmpty())
        {
            addParagraph(doc, "\uff08\u65e0\u75c5\u7076\u6846\u6570\u636e\uff09");
            return;
        }
        int i = 1;
        for (Map<String, Object> m : list)
        {
            if (m == null)
            {
                continue;
            }
            addParagraph(doc, "\u75c5\u7076 " + i + "\uff1a" + str(m.get("full_position")) + " \u7f6e\u4fe1\u5ea6 "
                    + formatConf(m.get("confidence")) + " \u6846(" + str(m.get("x1")) + "," + str(m.get("y1")) + ")-("
                    + str(m.get("x2")) + "," + str(m.get("y2")) + ")");
            i++;
        }
    }

    @SuppressWarnings("unchecked")
    private void addLabDataSection(XWPFDocument doc, Map<String, Object> labData)
    {
        addSection(doc, "\u68c0\u9a8c\u5bf9\u7167\u6570\u636e", "");
        if (labData == null || labData.isEmpty())
        {
            addParagraph(doc, "\uff08\u65e0\uff09");
            return;
        }
        for (Map.Entry<String, Object> cat : labData.entrySet())
        {
            addParagraph(doc, "\u3010" + cat.getKey() + "\u3011");
            Object v = cat.getValue();
            if (!(v instanceof Map))
            {
                addParagraph(doc, String.valueOf(v));
                continue;
            }
            Map<String, Object> inner = (Map<String, Object>) v;
            for (Map.Entry<String, Object> e : inner.entrySet())
            {
                addParagraph(doc, e.getKey() + "\uff1a" + String.valueOf(e.getValue()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addFusionResultSection(XWPFDocument doc, Map<String, Object> fusionResponse)
    {
        addSection(doc, "\u591a\u6a21\u6001\u878d\u5408\u51b3\u7b56\u7ed3\u679c", "");
        if (fusionResponse == null)
        {
            addParagraph(doc, "\uff08\u65e0\uff09");
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
            addParagraph(doc, "\u5355\u6a21\u6001\u8bc4\u5206\uff1a");
            appendModalityScore(doc, m, "image", "ImgS");
            appendModalityScore(doc, m, "case", "CaseS");
            appendModalityScore(doc, m, "lab", "LabS");
        }

        Object cc = d.get("consistency_check");
        if (cc instanceof Map)
        {
            Map<String, Object> c = (Map<String, Object>) cc;
            addParagraph(doc, "\u4e00\u81f4\u6027\u6821\u9a8c\uff1a" + str(c.get("level")));
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
            addParagraph(doc, "\u7efc\u5408\u8bc4\u5206\uff1a\u539f\u59cb " + str(f.get("raw_score")) + " \u7f6e\u4fe1 "
                    + str(f.get("confidence_factor")) + " \u6700\u7ec8 " + str(f.get("final_score")));
            Object w = f.get("weights");
            if (w instanceof Map)
            {
                Map<String, Object> wm = (Map<String, Object>) w;
                addParagraph(doc, "Weights ImgS=" + str(wm.get("ImgS")) + " CaseS=" + str(wm.get("CaseS")) + " LabS=" + str(wm.get("LabS")));
            }
        }

        Object diag = d.get("diagnosis_output");
        if (diag instanceof Map)
        {
            Map<String, Object> dg = (Map<String, Object>) diag;
            addParagraph(doc, "\u8bca\u65ad\u8f93\u51fa\uff1a" + str(dg.get("grade_cn")) + "(" + str(dg.get("grade")) + ") "
                    + str(dg.get("severity_cn")) + "(" + str(dg.get("severity")) + ")");
            addParagraph(doc, str(dg.get("action")));
        }

        Object sg = d.get("structured_suggestions");
        if (sg instanceof List)
        {
            addParagraph(doc, "\u7ed3\u6784\u5316\u5efa\u8bae\uff1a");
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
        labelRun.setText(label + "\uff1a");
        XWPFRun valueRun = paragraph.createRun();
        valueRun.setText(safeText(value, "\u6682\u65e0"));
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
            throw new ServiceException("Image not found");
        }
        if (xray.getPatientId() == null)
        {
            throw new ServiceException("Image not bound to patient");
        }
        if (!SecurityUtils.isAdmin())
        {
            Long userId = SecurityUtils.getUserId();
            if (userId == null)
            {
                throw new ServiceException("No permission");
            }
            com.ruoyi.emr.domain.MedicalPatient patient = medicalPatientMapper.selectMedicalPatientByPatientId(xray.getPatientId());
            if (patient == null || patient.getAttendingDoctorId() == null || !patient.getAttendingDoctorId().equals(userId))
            {
                throw new ServiceException("No permission for this patient image");
            }
        }
    }
}
