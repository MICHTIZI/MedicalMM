package com.ruoyi.emr.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.emr.domain.MedicalPatient;
import com.ruoyi.emr.domain.MedicalRecord;
import com.ruoyi.emr.domain.vo.XrayOptionVo;
import com.ruoyi.emr.service.IMedicalRecordService;

/**
 * Structured medical record Controller.
 */
@RestController
@RequestMapping("/medicalRecord")
public class MedicalRecordController extends BaseController
{
    @Autowired
    private IMedicalRecordService medicalRecordService;

    @RequiresPermissions("medical:record:list")
    @GetMapping("/list")
    public TableDataInfo list(MedicalRecord record)
    {
        startPage();
        List<MedicalRecord> list = medicalRecordService.selectMedicalRecordList(record);
        return getDataTable(list);
    }

    @RequiresPermissions("medical:record:export")
    @Log(title = "Structured Medical Record", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedicalRecord record)
    {
        List<MedicalRecord> list = medicalRecordService.selectMedicalRecordList(record);
        ExcelUtil<MedicalRecord> util = new ExcelUtil<>(MedicalRecord.class);
        util.exportExcel(response, list, "Structured Medical Record");
    }

    @RequiresPermissions("medical:record:query")
    @GetMapping("/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return success(medicalRecordService.selectMedicalRecordByRecordId(recordId));
    }

    @RequiresPermissions("medical:record:add")
    @Log(title = "Structured Medical Record", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody MedicalRecord record)
    {
        return toAjax(medicalRecordService.insertMedicalRecord(record));
    }

    @RequiresPermissions("medical:record:edit")
    @Log(title = "Structured Medical Record", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody MedicalRecord record)
    {
        return toAjax(medicalRecordService.updateMedicalRecord(record));
    }

    @RequiresPermissions("medical:record:remove")
    @Log(title = "Structured Medical Record", businessType = BusinessType.DELETE)
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(medicalRecordService.deleteMedicalRecordByRecordIds(recordIds));
    }

    @RequiresPermissions("medical:record:list")
    @GetMapping("/patientOptions")
    public AjaxResult patientOptions(MedicalPatient patient)
    {
        return success(medicalRecordService.selectPatientOptions(patient));
    }

    @RequiresPermissions("medical:record:image")
    @GetMapping("/xrayOptions")
    public AjaxResult xrayOptions(@RequestParam(value = "keyword", required = false) String keyword)
    {
        List<XrayOptionVo> list = medicalRecordService.selectXrayOptions(keyword);
        return success(list);
    }
}
