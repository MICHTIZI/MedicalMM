package com.ruoyi.emr.util;

import com.ruoyi.emr.config.MinioConfig;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;

public final class MinioObjectKeyResolver
{
    private static final String LEGACY_FOLDER = "chest_224_hd";

    private MinioObjectKeyResolver()
    {
    }

    public static String extractFilename(String path)
    {
        if (path == null || path.isEmpty())
        {
            return path;
        }
        String normalized = path.replace('\\', '/');
        int idx = normalized.lastIndexOf('/');
        return idx >= 0 ? normalized.substring(idx + 1) : normalized;
    }

    public static String normalizeObjectPath(String path)
    {
        if (path == null)
        {
            return "";
        }
        String normalized = path.replace('\\', '/');
        while (normalized.startsWith("/"))
        {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    public static String resolveOriginalObjectKey(MinioClient client, MinioConfig config, String imagePath)
    {
        String raw = normalizeObjectPath(imagePath);
        if (raw.isEmpty())
        {
            return raw;
        }
        String filename = extractFilename(raw);
        String bucket = config.getBucketName();
        if (objectExists(client, bucket, raw))
        {
            return raw;
        }
        if (objectExists(client, bucket, filename))
        {
            return filename;
        }
        String legacy = LEGACY_FOLDER + "/" + filename;
        if (objectExists(client, bucket, legacy))
        {
            return legacy;
        }
        return filename;
    }

    public static String resolveAnnotatedObjectKey(MinioClient client, MinioConfig config, String aiResultPath, String imagePath)
    {
        String raw = normalizeObjectPath(aiResultPath);
        if (!raw.isEmpty() && objectExists(client, config.getBucketName(), raw))
        {
            return raw;
        }
        String filename = extractFilename(imagePath);
        if (filename == null || filename.isEmpty())
        {
            return "";
        }
        String derived = "result/" + filename;
        if (objectExists(client, config.getBucketName(), derived))
        {
            return derived;
        }
        return raw.isEmpty() ? derived : raw;
    }

    private static boolean objectExists(MinioClient client, String bucket, String objectKey)
    {
        if (objectKey == null || objectKey.isEmpty())
        {
            return false;
        }
        try
        {
            client.statObject(StatObjectArgs.builder().bucket(bucket).object(objectKey).build());
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
