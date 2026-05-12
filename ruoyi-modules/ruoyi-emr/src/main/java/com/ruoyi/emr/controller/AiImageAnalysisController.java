package com.ruoyi.emr.controller;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.constant.HttpStatus;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.PageDomain;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.core.web.page.TableSupport;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.emr.config.MinioConfig;
import com.ruoyi.emr.domain.ChestXray;
import com.ruoyi.emr.domain.MedicalRecord;
import com.ruoyi.emr.domain.query.ChestXrayQuery;
import com.ruoyi.emr.service.IAiImageAnalysisService;
import com.ruoyi.emr.service.IChestXrayService;
import com.ruoyi.emr.service.IMedicalPatientDiagnosisService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;

@RestController
@RequestMapping("/aiImage")
public class AiImageAnalysisController extends BaseController
{
    @Autowired
    private IChestXrayService chestXrayService;

    @Autowired
    private IAiImageAnalysisService aiImageAnalysisService;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private IMedicalPatientDiagnosisService medicalPatientDiagnosisService;

    @RequiresPermissions("ai:image:list")
    @GetMapping("/list")
    public TableDataInfo list(ChestXrayQuery query)
    {
        PageDomain pd = TableSupport.buildPageRequest();
        Page<ChestXray> page = new Page<>(pd.getPageNum(), pd.getPageSize());
        IPage<ChestXray> result = chestXrayService.selectPage(page, query);
        TableDataInfo rsp = new TableDataInfo();
        rsp.setCode(HttpStatus.SUCCESS);
        rsp.setMsg("success");
        rsp.setRows(result.getRecords());
        rsp.setTotal(result.getTotal());
        return rsp;
    }

    @RequiresPermissions("ai:image:analyze")
    @PostMapping("/{imageId}/analyze")
    public AjaxResult analyze(@PathVariable Long imageId)
    {
        return success(aiImageAnalysisService.analyze(imageId));
    }

    /**
     * 将前端合成的「原图 + 矩形标注」写入 MinIO，覆盖与 AI 一致的 {@code result/原文件名} 对象。
     */
    @RequiresPermissions("ai:image:analyze")
    @PostMapping("/{imageId}/userOverlay")
    public AjaxResult uploadUserOverlay(@PathVariable Long imageId, @RequestParam("file") MultipartFile file)
    {
        try
        {
            return success(aiImageAnalysisService.uploadUserOverlay(imageId, file));
        }
        catch (ServiceException e)
        {
            return error(e.getMessage());
        }
        catch (Exception e)
        {
            return error("上传失败: " + e.getMessage());
        }
    }

    @RequiresPermissions("ai:image:record")
    @GetMapping("/{imageId}/record")
    public AjaxResult record(@PathVariable Long imageId)
    {
        return success(aiImageAnalysisService.getGeneratedRecord(imageId));
    }

    @RequiresPermissions("ai:image:record")
    @PostMapping("/{imageId}/exportReport")
    public void exportReport(@PathVariable Long imageId, HttpServletResponse response)
    {
        ChestXray xray = chestXrayService.getById(imageId);
        MedicalRecord record = aiImageAnalysisService.getGeneratedRecord(imageId);
        if (xray == null || record == null)
        {
            writeTextError(response, "Report record does not exist");
            return;
        }
        String patientName = safeText(xray.getPatientName(), "\u672a\u77e5\u60a3\u8005");
        String timeText = DateUtils.parseDateToStr("yyyyMMddHHmmss", new Date());
        String fileName = "\u75c5\u5386\u62a5\u544a_" + patientName + "_" + timeText + ".docx";

        try (XWPFDocument doc = new XWPFDocument())
        {
            addTitle(doc, "\u80ba\u708e\u591a\u6a21\u6001\u8f85\u52a9\u8bca\u65ad\u62a5\u544a");
            addMeta(doc, "\u60a3\u8005\u59d3\u540d", patientName);
            addMeta(doc, "\u68c0\u67e5\u65f6\u95f4", DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", xray.getCreateTime()));
            addMeta(doc, "\u75c5\u7076\u6570\u91cf", String.valueOf(xray.getLesionCount() == null ? 0 : xray.getLesionCount()));
            addMeta(doc, "AI\u8bca\u65ad\u7ed3\u679c", safeText(xray.getDiagnosis(), "\u6682\u65e0"));
            addSection(doc, "\u4e3b\u8bc9", record.getChiefComplaint());
            addSection(doc, "\u73b0\u75c5\u53f2", record.getPresentHistory());
            addSection(doc, "\u65e2\u5f80\u53f2", record.getPastHistory());
            addSection(doc, "\u4f53\u683c\u68c0\u67e5", record.getPhysicalExam());
            addSection(doc, "\u521d\u6b65\u8bca\u65ad", record.getInitialDiagnosis());
            addImageSection(doc, xray, record);

            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20"));
            doc.write(response.getOutputStream());
            if (xray.getPatientId() != null)
            {
                medicalPatientDiagnosisService.markReportExported(xray.getPatientId());
            }
        }
        catch (Exception e)
        {
            writeTextError(response, "Export report failed: " + e.getMessage());
        }
    }

    @GetMapping("/image/**")
    public void proxyImage(HttpServletRequest request, HttpServletResponse response)
    {
        String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String raw = fullPath.substring(fullPath.indexOf("/image/") + "/image/".length());
        try
        {
            raw = URLDecoder.decode(raw, StandardCharsets.UTF_8.name());
            raw = normalizeObjectPath(raw);
        }
        catch (Exception ignored) {}
        if (raw.isEmpty())
        {
            response.setStatus(400);
            return;
        }
        try
        {
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(raw)
                .build());
            response.setContentType(guessContentType(raw));
            response.setContentLengthLong(stat.size());
            response.setHeader("Cache-Control", "max-age=86400");
            try (InputStream is = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(raw)
                    .build());
                 OutputStream os = response.getOutputStream())
            {
                byte[] buf = new byte[8192];
                int len;
                while ((len = is.read(buf)) != -1)
                {
                    os.write(buf, 0, len);
                }
                os.flush();
            }
        }
        catch (Exception e)
        {
            response.setStatus(404);
        }
    }

    private String guessContentType(String filename)
    {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
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

        XWPFParagraph contentParagraph = doc.createParagraph();
        XWPFRun contentRun = contentParagraph.createRun();
        contentRun.setText(safeText(content, "暂无"));
    }

    private void addImageSection(XWPFDocument doc, ChestXray xray, MedicalRecord record)
    {
        String imagePath = safeText(record.getAiResultPath(), deriveAiResultPath(xray.getImagePath()));
        addSection(doc, "AI标注图", "");
        if (imagePath == null || imagePath.isEmpty())
        {
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
            XWPFParagraph paragraph = doc.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText("AI标注图读取失败：" + e.getMessage());
        }
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
        if (lower.endsWith(".png")) return XWPFDocument.PICTURE_TYPE_PNG;
        if (lower.endsWith(".gif")) return XWPFDocument.PICTURE_TYPE_GIF;
        if (lower.endsWith(".bmp")) return XWPFDocument.PICTURE_TYPE_BMP;
        return XWPFDocument.PICTURE_TYPE_JPEG;
    }

    private String safeText(String value, String fallback)
    {
        return value == null || value.isEmpty() ? fallback : value;
    }

    private void writeTextError(HttpServletResponse response, String message)
    {
        try
        {
            response.setStatus(500);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":500,\"msg\":\"" + message.replace("\"", "'") + "\"}");
        }
        catch (Exception ignored) {}
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
}
