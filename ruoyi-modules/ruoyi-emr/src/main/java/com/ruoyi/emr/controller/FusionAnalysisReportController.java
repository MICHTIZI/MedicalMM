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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.emr.domain.FusionAnalysisReport;
import com.ruoyi.emr.domain.dto.FusionReportDoctorUpdateDto;
import com.ruoyi.emr.domain.dto.FusionReportSaveAnalyzeDto;
import com.ruoyi.emr.service.IFusionAnalysisReportService;

@RestController
@RequestMapping({ "/fusionReport", "/emr/fusionReport" })
public class FusionAnalysisReportController extends BaseController
{
    @Autowired
    private IFusionAnalysisReportService fusionAnalysisReportService;

    @RequiresPermissions("fusion:report:list")
    @GetMapping("/list")
    public TableDataInfo list(FusionAnalysisReport query)
    {
        startPage();
        List<FusionAnalysisReport> list = fusionAnalysisReportService.selectFusionAnalysisReportList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("fusion:report:query")
    @GetMapping("/{reportId}")
    public AjaxResult getInfo(@PathVariable Long reportId)
    {
        return success(fusionAnalysisReportService.selectFusionAnalysisReportById(reportId));
    }

    /**
     * Viewer: load cached fusion report for current chest xray (patient + image).
     */
    @RequiresPermissions("ai:image:record")
    @GetMapping("/byImage/{imageId}")
    public AjaxResult getByImage(@PathVariable Long imageId)
    {
        return success(fusionAnalysisReportService.selectByImageIdForViewer(imageId));
    }

    /**
     * Viewer: persist fusion API result (first time or overwrite same patient+image).
     */
    @RequiresPermissions("ai:image:record")
    @PostMapping("/saveAnalyze/{imageId}")
    public AjaxResult saveAnalyze(@PathVariable Long imageId, @RequestBody FusionReportSaveAnalyzeDto body)
    {
        FusionAnalysisReport saved = fusionAnalysisReportService.saveAnalyzeResult(imageId, body);
        return success(saved);
    }

    @RequiresPermissions("ai:image:record")
    @PutMapping("/doctor")
    public AjaxResult updateDoctor(@RequestBody FusionReportDoctorUpdateDto body)
    {
        return toAjax(fusionAnalysisReportService.updateDoctorFields(body));
    }

    @RequiresPermissions("fusion:report:edit")
    @Log(title = "Fusion report", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FusionAnalysisReport row)
    {
        return toAjax(fusionAnalysisReportService.updateFusionAnalysisReport(row));
    }

    @RequiresPermissions("fusion:report:remove")
    @Log(title = "Fusion report", businessType = BusinessType.DELETE)
    @DeleteMapping("/{reportIds}")
    public AjaxResult remove(@PathVariable Long[] reportIds)
    {
        return toAjax(fusionAnalysisReportService.deleteFusionAnalysisReportByIds(reportIds));
    }
}
