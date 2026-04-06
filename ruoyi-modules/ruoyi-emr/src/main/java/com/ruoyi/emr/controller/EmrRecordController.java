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
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.constant.HttpStatus;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.PageDomain;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.core.web.page.TableSupport;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.emr.domain.EmrEntity;
import com.ruoyi.emr.domain.EmrRecord;
import com.ruoyi.emr.domain.query.EmrRecordQuery;
import com.ruoyi.emr.domain.vo.EmrExtractRequest;
import com.ruoyi.emr.domain.vo.EmrRecordDetailVo;
import com.ruoyi.emr.domain.vo.EmrRecordExcel;
import com.ruoyi.emr.domain.vo.EmrRecordSaveVo;
import com.ruoyi.emr.service.IEmrRecordService;

/**
 * 电子病历 REST：列表、详情、保存、归档、实体抽取、Excel 导入导出。
 */
@RestController
@RequestMapping("/record")
public class EmrRecordController extends BaseController
{
    @Autowired
    private IEmrRecordService emrRecordService;

    @RequiresPermissions("emr:record:list")
    @GetMapping("/list")
    public TableDataInfo list(EmrRecordQuery query)
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Page<EmrRecord> page = new Page<>(pageDomain.getPageNum(), pageDomain.getPageSize());
        IPage<EmrRecord> result = emrRecordService.selectRecordPage(page, query);
        TableDataInfo rsp = new TableDataInfo();
        rsp.setCode(HttpStatus.SUCCESS);
        rsp.setMsg("??????");
        rsp.setRows(result.getRecords());
        rsp.setTotal(result.getTotal());
        return rsp;
    }

    @RequiresPermissions("emr:record:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        EmrRecordDetailVo vo = emrRecordService.getDetail(id);
        return vo == null ? error("??????????") : success(vo);
    }

    @RequiresPermissions("emr:record:add")
    @Log(title = "???????", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody EmrRecordSaveVo vo)
    {
        Long id = emrRecordService.saveWithEntities(vo);
        return AjaxResult.success("??????", id);
    }

    @RequiresPermissions("emr:record:edit")
    @Log(title = "???????", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody EmrRecordSaveVo vo)
    {
        return toAjax(emrRecordService.updateWithEntities(vo));
    }

    @RequiresPermissions("emr:record:remove")
    @Log(title = "???????", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        for (Long id : ids)
        {
            emrRecordService.removeWithEntities(id);
        }
        return success();
    }

    @RequiresPermissions("emr:record:archive")
    @Log(title = "??????r", businessType = BusinessType.UPDATE)
    @PutMapping("/archive/{id}")
    public AjaxResult archive(@PathVariable Long id)
    {
        return toAjax(emrRecordService.archive(id));
    }

    @RequiresPermissions("emr:record:unarchive")
    @Log(title = "?????????r", businessType = BusinessType.UPDATE)
    @PutMapping("/unarchive/{id}")
    public AjaxResult unarchive(@PathVariable Long id)
    {
        return toAjax(emrRecordService.unarchive(id));
    }

    @RequiresPermissions("emr:record:add")
    @PostMapping("/extract")
    public AjaxResult extract(@Valid @RequestBody EmrExtractRequest req)
    {
        List<EmrEntity> list = emrRecordService.extractEntities(req.getContent());
        return success(list);
    }

    @RequiresPermissions("emr:record:export")
    @Log(title = "????????", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, EmrRecordQuery query)
    {
        List<EmrRecordExcel> list = emrRecordService.listExport(query);
        ExcelUtil<EmrRecordExcel> util = new ExcelUtil<>(EmrRecordExcel.class);
        util.exportExcel(response, list, "???????");
    }

    @RequiresPermissions("emr:record:import")
    @Log(title = "????????", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file) throws Exception
    {
        ExcelUtil<EmrRecordExcel> util = new ExcelUtil<>(EmrRecordExcel.class);
        List<EmrRecordExcel> list = util.importExcel(file.getInputStream());
        String msg = emrRecordService.importRecords(list);
        return success(msg);
    }
}
