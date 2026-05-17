package com.ruoyi.emr.service.impl;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.emr.config.EmrAiProperties;
import com.ruoyi.emr.config.MinioConfig;
import com.ruoyi.emr.domain.ChestXray;
import com.ruoyi.emr.domain.MedicalRecord;
import com.ruoyi.emr.domain.vo.AiDetectResponseVo;
import com.ruoyi.emr.domain.vo.AiReportGenerateResponseVo;
import com.ruoyi.emr.mapper.AiImageAnalysisMapper;
import com.ruoyi.emr.mapper.MedicalPatientMapper;
import com.ruoyi.emr.service.IAiImageAnalysisService;
import com.ruoyi.emr.service.IChestXrayService;
import com.ruoyi.emr.service.IMedicalPatientDiagnosisService;
import com.ruoyi.emr.support.PatientArchiveGuard;
import com.ruoyi.system.api.model.LoginUser;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;

@Service
public class AiImageAnalysisServiceImpl implements IAiImageAnalysisService
{
    private static final Logger log = LoggerFactory.getLogger(AiImageAnalysisServiceImpl.class);

    @Autowired
    private EmrAiProperties emrAiProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IChestXrayService chestXrayService;

    @Autowired
    private AiImageAnalysisMapper aiImageAnalysisMapper;

    @Autowired
    private MedicalPatientMapper medicalPatientMapper;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private IMedicalPatientDiagnosisService medicalPatientDiagnosisService;

    @Autowired
    private PatientArchiveGuard patientArchiveGuard;

    private RestTemplate aiRestTemplate()
    {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(8000);
        f.setReadTimeout(120000);
        return new RestTemplate(f);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiDetectResponseVo analyze(Long imageId)
    {
        ChestXray xray = chestXrayService.getById(imageId);
        checkXrayAccess(xray);
        patientArchiveGuard.rejectIfArchived(xray.getPatientId());
        String imageName = extractFilename(xray.getImagePath());
        ensureRootImageObject(imageName);

        AiDetectResponseVo detect = callYoloDetect(imageName);
        mergeDiagnosisReport(detect, callDiagnosisReport(detect));

        Integer lesionCount = detect.getLesionCount() == null ? 0 : detect.getLesionCount();
        aiImageAnalysisMapper.updateXrayAiResult(xray.getId(), detect.getAiResultPath(), lesionCount, detect.getDiagnosis());
        saveGeneratedRecord(xray, detect, lesionCount);
        if (xray.getPatientId() != null)
        {
            medicalPatientDiagnosisService.markAiCompleted(xray.getPatientId());
        }
        return detect;
    }

    private AiDetectResponseVo callYoloDetect(String imageName)
    {
        Map<String, String> body = new HashMap<>();
        body.put("minio_image_path", imageName);
        try
        {
            ResponseEntity<AiDetectResponseVo> response = aiRestTemplate().postForEntity(
                emrAiProperties.getDetectUrl(), body, AiDetectResponseVo.class);
            AiDetectResponseVo result = response.getBody();
            if (result == null || result.getCode() == null || result.getCode() != 200)
            {
                throw new ServiceException(result == null ? "AI 病灶检测失败" : result.getMsg());
            }
            return result;
        }
        catch (RestClientException e)
        {
            throw new ServiceException("AI 病灶检测请求失败：" + e.getMessage());
        }
    }

    private AiReportGenerateResponseVo callDiagnosisReport(AiDetectResponseVo detect)
    {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("yolo_detection_data", buildYoloDetectionPayload(detect));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        String url = emrAiProperties.reportGenerateUrl();
        try
        {
            ResponseEntity<String> response = aiRestTemplate().postForEntity(url, entity, String.class);
            String body = response.getBody();
            if (StringUtils.isEmpty(body))
            {
                throw new ServiceException("AI 诊断报告服务返回空响应");
            }
            return parseReportResponse(body);
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (RestClientException e)
        {
            throw new ServiceException("无法连接 AI 报告服务（" + url + "）：" + e.getMessage());
        }
        catch (Exception e)
        {
            throw new ServiceException("AI 诊断报告响应解析失败：" + e.getMessage());
        }
    }

    private AiReportGenerateResponseVo parseReportResponse(String body) throws Exception
    {
        JsonNode root = objectMapper.readTree(body);
        int code = readIntCode(root.get("code"));
        if (code != 200)
        {
            throw new ServiceException(root.path("msg").asText("AI 诊断报告生成失败"));
        }

        AiReportGenerateResponseVo result = new AiReportGenerateResponseVo();
        result.setCode(code);
        result.setMsg(root.path("msg").asText());

        JsonNode data = root.get("data");
        if (data != null && !data.isNull())
        {
            AiReportGenerateResponseVo.ReportData reportData = new AiReportGenerateResponseVo.ReportData();
            reportData.setDiagnosisReport(firstNonBlankText(data, "diagnosis_report", "diagnosisReport"));
            reportData.setCreateTime(firstNonBlankText(data, "create_time", "createTime", "report_create_time"));
            result.setData(reportData);
        }
        else if (StringUtils.isEmpty(firstNonBlankText(root, "diagnosis_report", "diagnosisReport")))
        {
            log.warn("AI report response missing data node, url={}", emrAiProperties.reportGenerateUrl());
        }
        return result;
    }

    private void mergeDiagnosisReport(AiDetectResponseVo detect, AiReportGenerateResponseVo report)
    {
        if (report == null || report.getData() == null)
        {
            log.warn("AI diagnosis report not merged: empty report data");
            return;
        }
        String text = report.getData().getDiagnosisReport();
        String time = report.getData().getCreateTime();
        if (StringUtils.isEmpty(text))
        {
            log.warn("AI diagnosis report text is empty after parse");
            return;
        }
        detect.setDiagnosisReport(text);
        detect.setReportCreateTime(time);
    }

    private static int readIntCode(JsonNode codeNode)
    {
        if (codeNode == null || codeNode.isNull())
        {
            return 0;
        }
        if (codeNode.isInt() || codeNode.isLong())
        {
            return codeNode.asInt();
        }
        if (codeNode.isTextual())
        {
            try
            {
                return Integer.parseInt(codeNode.asText().trim());
            }
            catch (NumberFormatException ignored)
            {
                return 0;
            }
        }
        return 0;
    }

    private static String firstNonBlankText(JsonNode node, String... fieldNames)
    {
        if (node == null || node.isNull())
        {
            return null;
        }
        for (String name : fieldNames)
        {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull() && value.isTextual())
            {
                String text = value.asText();
                if (StringUtils.isNotEmpty(text))
                {
                    return text;
                }
            }
        }
        return null;
    }

    /**
     * Payload for {@code /xray/report/generate}: YOLO detect output without overlay paths.
     */
    private Map<String, Object> buildYoloDetectionPayload(AiDetectResponseVo detect)
    {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("code", detect.getCode());
        m.put("msg", detect.getMsg());
        m.put("lesion_count", detect.getLesionCount());
        m.put("lesion_list", detect.getLesionList());
        if (detect.getInfectionRate() != null)
        {
            m.put("infection_rate", detect.getInfectionRate());
        }
        if (detect.getTotalInfectionArea() != null)
        {
            m.put("total_infection_area", detect.getTotalInfectionArea());
        }
        return m;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadUserOverlay(Long imageId, MultipartFile file)
    {
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("上传文件为空");
        }
        ChestXray xray = chestXrayService.getById(imageId);
        checkXrayAccess(xray);
        patientArchiveGuard.rejectIfArchived(xray.getPatientId());
        String filename = extractFilename(xray.getImagePath());
        if (StringUtils.isEmpty(filename))
        {
            throw new ServiceException("影像路径无效");
        }
        String objectKey = "result/" + filename;
        String contentType = StringUtils.isNotEmpty(file.getContentType()) ? file.getContentType() : "image/jpeg";

        try (InputStream is = file.getInputStream())
        {
            minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(objectKey)
                .stream(is, file.getSize(), -1)
                .contentType(contentType)
                .build());
        }
        catch (Exception e)
        {
            throw new ServiceException("写入 MinIO 失败: " + e.getMessage());
        }

        xray.setAiResultPath(objectKey);
        chestXrayService.updateById(xray);
        aiImageAnalysisMapper.updateRecordAiResultPathByImageId(imageId, objectKey);
        return objectKey;
    }

    @Override
    public MedicalRecord getGeneratedRecord(Long imageId)
    {
        ChestXray xray = chestXrayService.getById(imageId);
        checkXrayAccess(xray);
        return aiImageAnalysisMapper.selectMedicalRecordByImageId(imageId);
    }

    private void saveGeneratedRecord(ChestXray xray, AiDetectResponseVo result, Integer lesionCount)
    {
        MedicalRecord record = buildRecord(xray, result, lesionCount);
        MedicalRecord old = aiImageAnalysisMapper.selectMedicalRecordByImageId(xray.getId());
        if (old == null)
        {
            record.setCreateBy(SecurityUtils.getUsername());
            record.setCreateTime(new Date());
            aiImageAnalysisMapper.insertGeneratedRecord(record);
        }
        else
        {
            record.setRecordId(old.getRecordId());
            record.setUpdateBy(SecurityUtils.getUsername());
            record.setUpdateTime(new Date());
            aiImageAnalysisMapper.updateGeneratedRecord(record);
        }
    }

    private MedicalRecord buildRecord(ChestXray xray, AiDetectResponseVo result, Integer lesionCount)
    {
        MedicalRecord record = new MedicalRecord();
        record.setPatientId(xray.getPatientId());
        record.setOperateDoctorId(SecurityUtils.getUserId());
        record.setOperateDoctor(currentNickName());
        record.setImageId(xray.getId());
        record.setImagePath(xray.getImagePath());
        record.setAiResultPath(result.getAiResultPath());
        record.setRemark("AI影像分析自动生成病历");
        if (lesionCount == 0)
        {
            record.setChiefComplaint("影像筛查未见明确异常，患者暂无明显呼吸系统主诉。");
            record.setPresentHistory("胸片AI分析未发现明确肺部异常病灶，影像表现倾向正常。");
            record.setPastHistory("既往史暂未提供，建议结合患者既往疾病史进一步完善。");
            record.setPhysicalExam("体格检查暂未记录特异性异常，建议结合临床查体结果补充。");
            record.setInitialDiagnosis("正常胸片。");
        }
        else
        {
            record.setChiefComplaint("患者可能存在咳嗽、咳痰、发热等呼吸道症状，需结合临床进一步评估。");
            record.setPresentHistory("胸片AI分析提示肺部存在 " + lesionCount + " 处异常病灶，考虑肺部感染或肺炎可能。");
            record.setPastHistory("既往史暂未提供，需关注慢性呼吸系统疾病、免疫状态及近期感染史。");
            record.setPhysicalExam("建议重点查体双肺呼吸音、啰音、体温及血氧饱和度等指标。");
            if (StringUtils.isNotEmpty(result.getDiagnosisReport()))
            {
                record.setInitialDiagnosis(truncateForRecord(result.getDiagnosisReport(), 500));
            }
            else
            {
                record.setInitialDiagnosis(StringUtils.isNotEmpty(result.getDiagnosis()) ? result.getDiagnosis() : "肺部感染/肺炎。");
            }
        }
        return record;
    }

    private static String truncateForRecord(String text, int maxLen)
    {
        if (text == null)
        {
            return "";
        }
        String t = text.trim();
        if (t.length() <= maxLen)
        {
            return t;
        }
        return t.substring(0, maxLen) + "…";
    }

    private void ensureRootImageObject(String imageName)
    {
        if (StringUtils.isEmpty(imageName))
        {
            throw new ServiceException("影像文件名为空");
        }
        String bucket = minioConfig.getBucketName();
        if (!objectExists(bucket, imageName))
        {
            throw new ServiceException("MinIO 存储桶根路径下不存在该影像：" + bucket + "/" + imageName);
        }
    }

    private boolean objectExists(String bucket, String objectName)
    {
        try
        {
            minioClient.statObject(StatObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build());
            return true;
        }
        catch (Exception e)
        {
            return false;
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
                throw new ServiceException("无权分析其他主治医生所属患者的影像");
            }
        }
    }

    private String currentNickName()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getSysUser() != null && StringUtils.isNotEmpty(loginUser.getSysUser().getNickName()))
        {
            return loginUser.getSysUser().getNickName();
        }
        return SecurityUtils.getUsername();
    }

    private String extractFilename(String path)
    {
        if (path == null || path.isEmpty())
        {
            return path;
        }
        int i1 = path.lastIndexOf('/');
        int i2 = path.lastIndexOf('\\');
        int idx = Math.max(i1, i2);
        return idx >= 0 ? path.substring(idx + 1) : path;
    }
}
