package com.ruoyi.emr.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.emr.config.MinioConfig;
import com.ruoyi.emr.domain.ChestXray;
import com.ruoyi.emr.domain.MedicalRecord;
import com.ruoyi.emr.domain.vo.AiDetectResponseVo;
import com.ruoyi.emr.mapper.AiImageAnalysisMapper;
import com.ruoyi.emr.mapper.MedicalPatientMapper;
import com.ruoyi.emr.service.IAiImageAnalysisService;
import com.ruoyi.emr.service.IChestXrayService;
import com.ruoyi.system.api.model.LoginUser;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;

@Service
public class AiImageAnalysisServiceImpl implements IAiImageAnalysisService
{
    private static final String AI_URL = "http://127.0.0.1:5100/ai/detect/minio";

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiDetectResponseVo analyze(Long imageId)
    {
        ChestXray xray = chestXrayService.getById(imageId);
        checkXrayAccess(xray);
        String imageName = extractFilename(xray.getImagePath());
        ensureRootImageObject(imageName);
        Map<String, String> body = new HashMap<>();
        body.put("minio_image_path", imageName);

        ResponseEntity<AiDetectResponseVo> response;
        try
        {
            response = new RestTemplate().postForEntity(AI_URL, body, AiDetectResponseVo.class);
        }
        catch (RestClientException e)
        {
            throw new ServiceException("AI detection request failed: " + e.getMessage());
        }
        AiDetectResponseVo result = response.getBody();
        if (result == null || result.getCode() == null || result.getCode() != 200)
        {
            throw new ServiceException(result == null ? "AI detection failed" : result.getMsg());
        }

        Integer lesionCount = result.getLesionCount() == null ? 0 : result.getLesionCount();
        aiImageAnalysisMapper.updateXrayAiResult(xray.getId(), result.getAiResultPath(), lesionCount, result.getDiagnosis());
        saveGeneratedRecord(xray, result, lesionCount);
        return result;
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
            record.setInitialDiagnosis(StringUtils.isNotEmpty(result.getDiagnosis()) ? result.getDiagnosis() : "肺部感染/肺炎。");
        }
        return record;
    }

    private void ensureRootImageObject(String imageName)
    {
        if (StringUtils.isEmpty(imageName))
        {
            throw new ServiceException("Image path is empty");
        }
        String bucket = minioConfig.getBucketName();
        if (!objectExists(bucket, imageName))
        {
            throw new ServiceException("Image object not found in MinIO bucket root " + bucket + ": " + imageName);
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
            throw new ServiceException("Image does not exist");
        }
        if (xray.getPatientId() == null)
        {
            throw new ServiceException("Image is not bound to a patient");
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
                throw new ServiceException("No permission to analyze images of another doctor's patient");
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
