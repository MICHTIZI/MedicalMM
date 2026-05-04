package com.ruoyi.emr.controller;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.constant.HttpStatus;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.PageDomain;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.core.web.page.TableSupport;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.emr.config.MinioConfig;
import com.ruoyi.emr.domain.ChestXray;
import com.ruoyi.emr.domain.query.ChestXrayQuery;
import com.ruoyi.emr.service.IAiImageAnalysisService;
import com.ruoyi.emr.service.IChestXrayService;
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

    @RequiresPermissions("ai:image:record")
    @GetMapping("/{imageId}/record")
    public AjaxResult record(@PathVariable Long imageId)
    {
        return success(aiImageAnalysisService.getGeneratedRecord(imageId));
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
