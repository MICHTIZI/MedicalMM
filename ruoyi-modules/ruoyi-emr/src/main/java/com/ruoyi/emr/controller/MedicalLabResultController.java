package com.ruoyi.emr.controller;

import java.util.List;
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
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.emr.domain.MedicalLabResult;
import com.ruoyi.emr.domain.MedicalPatient;
import com.ruoyi.emr.service.IMedicalLabResultService;
import com.ruoyi.emr.service.IMedicalRecordService;

/**
 * Patient lab indicators (medical_lab_results).
 * Base path /lab (after gateway strips /emr) and /emr/lab for direct access without strip.
 */
@RestController
@RequestMapping({ "/lab", "/emr/lab" })
public class MedicalLabResultController extends BaseController
{
    @Autowired
    private IMedicalLabResultService medicalLabResultService;

    @Autowired
    private IMedicalRecordService medicalRecordService;

    @RequiresPermissions("medical:lab:list")
    @GetMapping("/list")
    public TableDataInfo list(MedicalLabResult query)
    {
        startPage();
        List<MedicalLabResult> list = medicalLabResultService.selectMedicalLabResultList(query);
        return getDataTable(list);
    }

    /**
     * Patients for lab binding (scoped like structured record options).
     */
    @RequiresPermissions("medical:lab:list")
    @GetMapping("/patientOptions")
    public AjaxResult patientOptions(MedicalPatient patient)
    {
        return success(medicalRecordService.selectPatientOptions(patient));
    }

    @RequiresPermissions("medical:lab:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(medicalLabResultService.selectMedicalLabResultById(id));
    }

    @RequiresPermissions("medical:lab:add")
    @Log(title = "Lab result", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MedicalLabResult row)
    {
        return toAjax(medicalLabResultService.insertMedicalLabResult(row));
    }

    @RequiresPermissions("medical:lab:edit")
    @Log(title = "Lab result", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MedicalLabResult row)
    {
        return toAjax(medicalLabResultService.updateMedicalLabResult(row));
    }

    @RequiresPermissions("medical:lab:remove")
    @Log(title = "Lab result", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(medicalLabResultService.deleteMedicalLabResultByIds(ids));
    }

    @RequiresPermissions("medical:lab:import")
    @PostMapping("/parseTxt")
    public AjaxResult parseTxt(@RequestParam("file") MultipartFile file)
    {
        return success(medicalLabResultService.parseTxtPreview(file));
    }

    @RequiresPermissions("medical:lab:import")
    @Log(title = "Lab TXT import", businessType = BusinessType.IMPORT)
    @PostMapping("/importTxt")
    public AjaxResult importTxt(@RequestParam("patientId") Long patientId, @RequestParam("file") MultipartFile file)
    {
        return toAjax(medicalLabResultService.importFromTxt(patientId, file));
    }
}
