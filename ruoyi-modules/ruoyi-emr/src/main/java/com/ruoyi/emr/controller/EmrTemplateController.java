package com.ruoyi.emr.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
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
import com.ruoyi.emr.domain.EmrTemplate;
import com.ruoyi.emr.service.IEmrTemplateService;

/**
 * 病历模板 CRUD 与 Excel 导入导出。
 */
@RestController
@RequestMapping("/template")
public class EmrTemplateController extends BaseController
{
    @Autowired
    private IEmrTemplateService emrTemplateService;

    @RequiresPermissions("emr:template:list")
    @GetMapping("/list")
    public TableDataInfo list(EmrTemplate query)
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Page<EmrTemplate> page = new Page<>(pageDomain.getPageNum(), pageDomain.getPageSize());
        IPage<EmrTemplate> result = emrTemplateService.selectPage(page, query);
        TableDataInfo rsp = new TableDataInfo();
        rsp.setCode(HttpStatus.SUCCESS);
        rsp.setMsg("??????");
        rsp.setRows(result.getRecords());
        rsp.setTotal(result.getTotal());
        return rsp;
    }

    @RequiresPermissions("emr:template:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(emrTemplateService.getById(id));
    }

    @RequiresPermissions("emr:template:add")
    @Log(title = "???????", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody EmrTemplate template)
    {
        template.setCreateTime(new Date());
        template.setUpdateTime(new Date());
        return toAjax(emrTemplateService.save(template));
    }

    @RequiresPermissions("emr:template:edit")
    @Log(title = "???????", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody EmrTemplate template)
    {
        template.setUpdateTime(new Date());
        return toAjax(emrTemplateService.updateById(template));
    }

    @RequiresPermissions("emr:template:remove")
    @Log(title = "???????", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(emrTemplateService.removeByIds(Arrays.asList(ids)));
    }

    @RequiresPermissions("emr:template:export")
    @Log(title = "??????????", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, EmrTemplate query)
    {
        Page<EmrTemplate> page = new Page<>(1, 5000);
        List<EmrTemplate> list = emrTemplateService.selectPage(page, query).getRecords();
        ExcelUtil<EmrTemplate> util = new ExcelUtil<>(EmrTemplate.class);
        util.exportExcel(response, list, "???????");
    }

    @RequiresPermissions("emr:template:import")
    @Log(title = "??????????", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file) throws Exception
    {
        ExcelUtil<EmrTemplate> util = new ExcelUtil<>(EmrTemplate.class);
        List<EmrTemplate> list = util.importExcel(file.getInputStream());
        Date now = new Date();
        for (EmrTemplate t : list)
        {
            t.setId(null);
            t.setCreateTime(now);
            t.setUpdateTime(now);
        }
        emrTemplateService.saveBatch(list);
        return success("??????? " + list.size() + " ?????");
    }
}
