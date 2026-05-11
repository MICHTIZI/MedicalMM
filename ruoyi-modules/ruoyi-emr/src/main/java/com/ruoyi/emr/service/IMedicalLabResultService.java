package com.ruoyi.emr.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.emr.domain.MedicalLabResult;

/**
 * Lab results service.
 */
public interface IMedicalLabResultService
{
    List<MedicalLabResult> selectMedicalLabResultList(MedicalLabResult query);

    MedicalLabResult selectMedicalLabResultById(Long id);

    int insertMedicalLabResult(MedicalLabResult row);

    int updateMedicalLabResult(MedicalLabResult row);

    int deleteMedicalLabResultByIds(Long[] ids);

    int importFromTxt(Long patientId, MultipartFile file);

    /**
     * Parse TXT into a lab row without persisting (for form preview before save).
     */
    MedicalLabResult parseTxtPreview(MultipartFile file);
}
