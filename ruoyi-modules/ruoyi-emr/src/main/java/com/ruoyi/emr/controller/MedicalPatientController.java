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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.emr.domain.MedicalPatient;
import com.ruoyi.emr.domain.vo.DoctorOptionVo;
import com.ruoyi.emr.service.IMedicalPatientService;

/**
 * Patient management Controller.
 */
@RestController
@RequestMapping("/patient")
public class MedicalPatientController extends BaseController
{
    @Autowired
    private IMedicalPatientService medicalPatientService;

    @RequiresPermissions("medical:patient:list")
    @GetMapping("/list")
    public TableDataInfo list(MedicalPatient patient)
    {
        startPage();
        List<MedicalPatient> list = medicalPatientService.selectMedicalPatientList(patient);
        return getDataTable(list);
    }

    @RequiresPermissions("medical:patient:export")
    @Log(title = "Patient Management", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedicalPatient patient)
    {
        List<MedicalPatient> list = medicalPatientService.selectMedicalPatientList(patient);
        ExcelUtil<MedicalPatient> util = new ExcelUtil<>(MedicalPatient.class);
        util.exportExcel(response, list, "Patient Data");
    }

    @RequiresPermissions("medical:patient:query")
    @GetMapping(value = "/{patientId}")
    public AjaxResult getInfo(@PathVariable("patientId") Long patientId)
    {
        return success(medicalPatientService.selectMedicalPatientByPatientId(patientId));
    }

    @RequiresPermissions("medical:patient:add")
    @Log(title = "Patient Management", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody MedicalPatient patient)
    {
        return toAjax(medicalPatientService.insertMedicalPatient(patient));
    }

    @RequiresPermissions("medical:patient:edit")
    @Log(title = "Patient Management", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody MedicalPatient patient)
    {
        return toAjax(medicalPatientService.updateMedicalPatient(patient));
    }

    @RequiresPermissions("medical:patient:remove")
    @Log(title = "Patient Management", businessType = BusinessType.DELETE)
    @DeleteMapping("/{patientIds}")
    public AjaxResult remove(@PathVariable Long[] patientIds)
    {
        return toAjax(medicalPatientService.deleteMedicalPatientByPatientIds(patientIds));
    }

    @RequiresPermissions("medical:patient:assign")
    @GetMapping("/doctorOptions")
    public AjaxResult doctorOptions()
    {
        List<DoctorOptionVo> list = medicalPatientService.selectDoctorOptions();
        return success(list);
    }
}
