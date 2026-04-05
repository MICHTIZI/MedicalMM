package com.ruoyi.imaging.controller;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.constant.HttpStatus;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.PageDomain;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.core.web.page.TableSupport;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.imaging.config.MinioConfig;
import com.ruoyi.imaging.domain.ChestXray;
import com.ruoyi.imaging.domain.DiseaseLabel;
import com.ruoyi.imaging.domain.query.ChestXrayQuery;
import com.ruoyi.imaging.service.IChestXrayService;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;

@RestController
@RequestMapping("/xray")
public class ChestXrayController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(ChestXrayController.class);

    private static final String MINIO_FOLDER = "chest_224_hd";

    @Autowired
    private IChestXrayService chestXrayService;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    /* =============== 列表 / 详情 =============== */

    @RequiresPermissions("imaging:xray:list")
    @GetMapping("/list")
    public TableDataInfo list(ChestXrayQuery query)
    {
        PageDomain pd = TableSupport.buildPageRequest();
        Page<ChestXray> page = new Page<>(pd.getPageNum(), pd.getPageSize());
        IPage<ChestXray> result = chestXrayService.selectPage(page, query);
        TableDataInfo rsp = new TableDataInfo();
        rsp.setCode(HttpStatus.SUCCESS);
        rsp.setMsg("查询成功");
        rsp.setRows(result.getRecords());
        rsp.setTotal(result.getTotal());
        return rsp;
    }

    @RequiresPermissions("imaging:xray:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        ChestXray entity = chestXrayService.getById(id);
        return entity == null ? error("影像不存在") : success(entity);
    }

    /* =============== 新增 / 修改 / 删除 =============== */

    @RequiresPermissions("imaging:xray:add")
    @Log(title = "医学影像", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ChestXray entity)
    {
        entity.setCreateTime(new Date());
        return toAjax(chestXrayService.save(entity));
    }

    @RequiresPermissions("imaging:xray:edit")
    @Log(title = "医学影像", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ChestXray entity)
    {
        return toAjax(chestXrayService.updateById(entity));
    }

    @RequiresPermissions("imaging:xray:remove")
    @Log(title = "医学影像", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        for (Long id : ids) chestXrayService.removeById(id);
        return success();
    }

    /* =============== 文件上传（MinIO → chest_224_hd/） =============== */

    @RequiresPermissions("imaging:xray:upload")
    @Log(title = "影像上传", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("files") MultipartFile[] files)
    {
        try
        {
            ensureBucket();
        }
        catch (Exception e)
        {
            return error("MinIO 存储桶初始化失败: " + e.getMessage());
        }

        List<ChestXray> saved = new ArrayList<>();
        for (MultipartFile file : files)
        {
            String originalName = file.getOriginalFilename();
            if (originalName == null) originalName = "unknown";

            String storedName = UUID.randomUUID().toString().replace("-", "")
                + "_" + originalName;
            String objectKey = MINIO_FOLDER + "/" + storedName;

            try (InputStream is = file.getInputStream())
            {
                minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectKey)
                    .stream(is, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            }
            catch (Exception e)
            {
                return error("上传文件失败 [" + originalName + "]: " + e.getMessage());
            }

            int w = 224, h = 224;
            try (InputStream imgIs = file.getInputStream())
            {
                BufferedImage img = ImageIO.read(imgIs);
                if (img != null)
                {
                    w = img.getWidth();
                    h = img.getHeight();
                }
            }
            catch (Exception ignored) {}

            ChestXray entity = new ChestXray();
            entity.setImagePath(storedName);
            entity.setImageName(originalName);
            entity.setWidth(w);
            entity.setHeight(h);
            entity.setLabelStr("0");
            entity.setDiseases("");
            entity.setCreateTime(new Date());
            chestXrayService.save(entity);
            saved.add(entity);
        }
        return AjaxResult.success("成功上传 " + saved.size() + " 张影像", saved);
    }

    /* =============== 图片代理（从 MinIO chest_224_hd/ 读取） =============== */

    @GetMapping("/image/**")
    public void proxyImage(HttpServletRequest request, HttpServletResponse response)
    {
        String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String raw = fullPath.substring(fullPath.indexOf("/image/") + "/image/".length());

        try
        {
            raw = URLDecoder.decode(raw, StandardCharsets.UTF_8.name());
        }
        catch (Exception ignored) {}

        if (raw.isEmpty())
        {
            response.setStatus(400);
            return;
        }

        String filename = extractFilename(raw);
        String objectKey = MINIO_FOLDER + "/" + filename;

        log.debug("Image proxy: raw={}, filename={}, objectKey={}", raw, filename, objectKey);

        try
        {
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(objectKey).build());

            String ct = stat.contentType();
            if (ct == null || ct.equals("application/octet-stream"))
            {
                ct = guessContentType(filename);
            }
            response.setContentType(ct);
            response.setContentLengthLong(stat.size());
            response.setHeader("Cache-Control", "max-age=86400");

            try (InputStream is = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectKey).build());
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
            log.warn("Image proxy 404: bucket={}, objectKey={}, error={}", minioConfig.getBucketName(), objectKey, e.getMessage());
            response.setStatus(404);
        }
    }

    /**
     * 从任意格式的 imagePath 中提取纯文件名。
     * 兼容：Windows 路径、Linux 路径、URL、MinIO objectKey、纯文件名。
     */
    private String extractFilename(String path)
    {
        if (path == null || path.isEmpty()) return path;
        int i1 = path.lastIndexOf('/');
        int i2 = path.lastIndexOf('\\');
        int idx = Math.max(i1, i2);
        return idx >= 0 ? path.substring(idx + 1) : path;
    }

    private String guessContentType(String filename)
    {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".dcm") || lower.endsWith(".dicom")) return "application/dicom";
        return "application/octet-stream";
    }

    private void ensureBucket() throws Exception
    {
        String bucket = minioConfig.getBucketName();
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists)
        {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
        String policy = "{"
            + "\"Version\":\"2012-10-17\","
            + "\"Statement\":[{"
            + "\"Effect\":\"Allow\","
            + "\"Principal\":{\"AWS\":[\"*\"]},"
            + "\"Action\":[\"s3:GetObject\"],"
            + "\"Resource\":[\"arn:aws:s3:::" + bucket + "/*\"]"
            + "}]}";
        minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
            .bucket(bucket).config(policy).build());
    }

    /* =============== 标注（diseases + label_str） =============== */

    @RequiresPermissions("imaging:xray:annotate")
    @Log(title = "影像标注", businessType = BusinessType.UPDATE)
    @PutMapping("/annotate/{id}")
    public AjaxResult annotate(@PathVariable Long id, @RequestBody ChestXray body)
    {
        String diseases = body.getDiseases() != null ? body.getDiseases() : "";
        String label = (diseases.isEmpty()) ? "0" : "1";
        return toAjax(chestXrayService.updateDiseases(id, diseases, label));
    }

    @GetMapping("/diseaseLabels")
    public AjaxResult diseaseLabels()
    {
        return success(DiseaseLabel.LABELS);
    }

    /* =============== 导出 =============== */

    @RequiresPermissions("imaging:xray:export")
    @Log(title = "影像导出", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ChestXrayQuery query)
    {
        List<ChestXray> list = chestXrayService.listExport(query);
        ExcelUtil<ChestXray> util = new ExcelUtil<>(ChestXray.class);
        util.exportExcel(response, list, "医学影像");
    }
}
