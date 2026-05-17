package com.ruoyi.emr.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.emr.config.MinioConfig;
import com.ruoyi.emr.domain.ChestXray;
import com.ruoyi.emr.domain.vo.XrayEnhanceResultVo;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.emr.mapper.MedicalPatientMapper;
import com.ruoyi.emr.service.IChestXrayService;
import com.ruoyi.emr.service.IXrayImageEnhanceService;
import com.ruoyi.emr.util.MinioObjectKeyResolver;
import com.ruoyi.emr.util.XrayImageEnhancer;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;

@Service
public class XrayImageEnhanceServiceImpl implements IXrayImageEnhanceService
{
    /** Bump when enhance algorithm changes so MinIO cache is regenerated. */
    private static final String ENHANCE_KEY_PREFIX = "algo-v2/";

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private IChestXrayService chestXrayService;

    @Autowired
    private MedicalPatientMapper medicalPatientMapper;

    @Override
    public XrayEnhanceResultVo ensureEnhanced(Long imageId)
    {
        ChestXray xray = chestXrayService.getById(imageId);
        checkXrayAccess(xray);
        ensureEnhanceBuckets();

        String sourceOriginal = MinioObjectKeyResolver.resolveOriginalObjectKey(
            minioClient, minioConfig, xray.getImagePath());
        if (StringUtils.isEmpty(sourceOriginal))
        {
            throw new ServiceException("\u65e0\u6cd5\u89e3\u6790\u539f\u56fe MinIO \u8def\u5f84");
        }
        String enhancedOriginal = ensureOne(
            minioConfig.getBucketName(),
            sourceOriginal,
            minioConfig.getEnhancedOriginalBucket(),
            ENHANCE_KEY_PREFIX + sourceOriginal);

        String aiPath = StringUtils.isNotEmpty(xray.getAiResultPath())
            ? xray.getAiResultPath()
            : null;
        String sourceAnnotated = MinioObjectKeyResolver.resolveAnnotatedObjectKey(
            minioClient, minioConfig, aiPath, xray.getImagePath());

        XrayEnhanceResultVo vo = new XrayEnhanceResultVo();
        vo.setOriginalObjectKey(enhancedOriginal);
        if (StringUtils.isNotEmpty(sourceAnnotated)
            && objectExists(minioConfig.getBucketName(), sourceAnnotated))
        {
            String enhancedAnnotated = ensureOne(
                minioConfig.getBucketName(),
                sourceAnnotated,
                minioConfig.getEnhancedAnnotatedBucket(),
                ENHANCE_KEY_PREFIX + sourceAnnotated);
            vo.setAnnotatedObjectKey(enhancedAnnotated);
            vo.setAnnotatedAvailable(true);
        }
        else
        {
            vo.setAnnotatedAvailable(false);
        }
        return vo;
    }

    @Override
    public void invalidateEnhanced(Long imageId)
    {
        ChestXray xray = chestXrayService.getById(imageId);
        if (xray == null)
        {
            return;
        }
        String sourceOriginal = MinioObjectKeyResolver.resolveOriginalObjectKey(
            minioClient, minioConfig, xray.getImagePath());
        if (StringUtils.isNotEmpty(sourceOriginal))
        {
            removeQuietly(minioConfig.getEnhancedOriginalBucket(), ENHANCE_KEY_PREFIX + sourceOriginal);
        }
        String aiPath = xray.getAiResultPath();
        String sourceAnnotated = MinioObjectKeyResolver.resolveAnnotatedObjectKey(
            minioClient, minioConfig, aiPath, xray.getImagePath());
        if (StringUtils.isNotEmpty(sourceAnnotated))
        {
            removeQuietly(minioConfig.getEnhancedAnnotatedBucket(), ENHANCE_KEY_PREFIX + sourceAnnotated);
        }
    }

    private String ensureOne(String sourceBucket, String sourceKey, String targetBucket, String targetKey)
    {
        if (objectExists(targetBucket, targetKey))
        {
            return targetKey;
        }
        try (InputStream is = minioClient.getObject(GetObjectArgs.builder()
                .bucket(sourceBucket)
                .object(sourceKey)
                .build()))
        {
            BufferedImage src = ImageIO.read(is);
            if (src == null)
            {
                throw new ServiceException("\u65e0\u6cd5\u8bfb\u53d6\u5f71\u50cf\uff1a" + sourceKey);
            }
            BufferedImage enhanced = XrayImageEnhancer.enhance(src);
            String format = guessFormat(sourceKey);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            if (!ImageIO.write(enhanced, format, bos))
            {
                ImageIO.write(enhanced, "png", bos);
                format = "png";
            }
            byte[] bytes = bos.toByteArray();
            String contentType = "png".equalsIgnoreCase(format) ? "image/png" : "image/jpeg";
            try (ByteArrayInputStream upload = new ByteArrayInputStream(bytes))
            {
                minioClient.putObject(PutObjectArgs.builder()
                    .bucket(targetBucket)
                    .object(targetKey)
                    .stream(upload, bytes.length, -1)
                    .contentType(contentType)
                    .build());
            }
            return targetKey;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("\u5f71\u50cf\u589e\u5f3a\u5931\u8d25\uff1a" + e.getMessage());
        }
    }

    private void ensureEnhanceBuckets()
    {
        ensureBucket(minioConfig.getEnhancedOriginalBucket());
        ensureBucket(minioConfig.getEnhancedAnnotatedBucket());
    }

    private void ensureBucket(String bucket)
    {
        try
        {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists)
            {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        }
        catch (Exception e)
        {
            throw new ServiceException("MinIO enhance bucket init failed: " + e.getMessage());
        }
    }

    private boolean objectExists(String bucket, String objectKey)
    {
        try
        {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(objectKey).build());
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private void removeQuietly(String bucket, String objectKey)
    {
        try
        {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
        }
        catch (Exception ignored)
        {
        }
    }

    private String guessFormat(String objectKey)
    {
        String lower = objectKey.toLowerCase();
        if (lower.endsWith(".png"))
        {
            return "png";
        }
        return "jpg";
    }

    private void checkXrayAccess(ChestXray xray)
    {
        if (xray == null)
        {
            throw new ServiceException("\u5f71\u50cf\u4e0d\u5b58\u5728");
        }
        if (xray.getPatientId() == null)
        {
            throw new ServiceException("\u5f71\u50cf\u672a\u7ed1\u5b9a\u60a3\u8005");
        }
        if (!SecurityUtils.isAdmin())
        {
            Long userId = SecurityUtils.getUserId();
            if (userId == null)
            {
                throw new ServiceException("\u65e0\u8bbf\u95ee\u6743\u9650");
            }
            com.ruoyi.emr.domain.MedicalPatient patient = medicalPatientMapper.selectMedicalPatientByPatientId(xray.getPatientId());
            if (patient == null || patient.getAttendingDoctorId() == null || !patient.getAttendingDoctorId().equals(userId))
            {
                throw new ServiceException("\u65e0\u6743\u8bbf\u95ee\u5176\u4ed6\u4e3b\u6cbb\u533b\u751f\u6240\u5c5e\u60a3\u8005\u7684\u5f71\u50cf");
            }
        }
    }
}
