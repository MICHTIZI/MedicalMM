package com.ruoyi.emr.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.emr.domain.AiImageAnalysisResult;
import com.ruoyi.emr.service.IAiImageAnalysisResultService;

@RestController
@RequestMapping({ "/aiAnalysisResult", "/emr/aiAnalysisResult" })
public class AiImageAnalysisResultController extends BaseController
{
    @Autowired
    private IAiImageAnalysisResultService aiImageAnalysisResultService;

    @RequiresPermissions("ai:analysis:result:list")
    @GetMapping("/list")
    public TableDataInfo list(AiImageAnalysisResult query)
    {
        startPage();
        List<AiImageAnalysisResult> list = aiImageAnalysisResultService.selectAiImageAnalysisResultList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("ai:analysis:result:query")
    @GetMapping("/{analysisId}")
    public AjaxResult getInfo(@PathVariable Long analysisId)
    {
        return success(aiImageAnalysisResultService.selectAiImageAnalysisResultById(analysisId));
    }

    @RequiresPermissions("ai:analysis:result:remove")
    @Log(title = "AI analysis result", businessType = BusinessType.DELETE)
    @DeleteMapping("/{analysisIds}")
    public AjaxResult remove(@PathVariable Long[] analysisIds)
    {
        return toAjax(aiImageAnalysisResultService.deleteAiImageAnalysisResultByIds(analysisIds));
    }
}
