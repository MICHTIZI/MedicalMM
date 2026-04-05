package com.ruoyi.imaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.minio.MinioClient;

/**
 * MinIO 配置（与 ruoyi-file 模块同结构，独立在 imaging 内使用）。
 */
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig
{
    private String url;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public String getBucketName() { return bucketName; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }

    @Bean
    public MinioClient minioClient()
    {
        return MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build();
    }
}
