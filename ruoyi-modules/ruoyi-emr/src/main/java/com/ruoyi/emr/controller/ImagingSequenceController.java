package com.ruoyi.emr.controller;

import java.util.Arrays;
import java.util.Date;
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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.constant.HttpStatus;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.PageDomain;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.core.web.page.TableSupport;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.emr.domain.ChestXray;
import com.ruoyi.emr.domain.ImagingSequence;
import com.ruoyi.emr.service.IImagingSequenceService;

/**
 * 影像序列管理。
 */
@RestController
@RequestMapping("/sequence")
public class ImagingSequenceController extends BaseController
{
    @Autowired
    private IImagingSequenceService sequenceService;

    @RequiresPermissions("imaging:sequence:list")
    @GetMapping("/list")
    public TableDataInfo list(ImagingSequence query)
    {
        PageDomain pd = TableSupport.buildPageRequest();
        Page<ImagingSequence> page = new Page<>(pd.getPageNum(), pd.getPageSize());
        LambdaQueryWrapper<ImagingSequence> qw = new LambdaQueryWrapper<>();
        if (query.getSeqName() != null && !query.getSeqName().isEmpty())
        {
            qw.like(ImagingSequence::getSeqName, query.getSeqName());
        }
        if (query.getArchiveStatus() != null && !query.getArchiveStatus().isEmpty())
        {
            qw.eq(ImagingSequence::getArchiveStatus, query.getArchiveStatus());
        }
        qw.orderByAsc(ImagingSequence::getSortOrder);
        IPage<ImagingSequence> result = sequenceService.page(page, qw);
        TableDataInfo rsp = new TableDataInfo();
        rsp.setCode(HttpStatus.SUCCESS);
        rsp.setMsg("\u67e5\u8be2\u6210\u529f");
        rsp.setRows(result.getRecords());
        rsp.setTotal(result.getTotal());
        return rsp;
    }

    @RequiresPermissions("imaging:sequence:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(sequenceService.getById(id));
    }

    @RequiresPermissions("imaging:sequence:add")
    @Log(title = "\u5f71\u50cf\u5e8f\u5217", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ImagingSequence seq)
    {
        seq.setArchiveStatus("0");
        seq.setCreateTime(new Date());
        seq.setUpdateTime(new Date());
        return toAjax(sequenceService.save(seq));
    }

    @RequiresPermissions("imaging:sequence:edit")
    @Log(title = "\u5f71\u50cf\u5e8f\u5217", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ImagingSequence seq)
    {
        seq.setUpdateTime(new Date());
        return toAjax(sequenceService.updateById(seq));
    }

    @RequiresPermissions("imaging:sequence:remove")
    @Log(title = "\u5f71\u50cf\u5e8f\u5217", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(sequenceService.removeByIds(Arrays.asList(ids)));
    }

    @RequiresPermissions("imaging:sequence:archive")
    @Log(title = "\u5e8f\u5217\u5f52\u6863", businessType = BusinessType.UPDATE)
    @PutMapping("/archive/{id}")
    public AjaxResult archive(@PathVariable Long id)
    {
        return toAjax(sequenceService.archive(id));
    }

    @RequiresPermissions("imaging:sequence:unarchive")
    @PutMapping("/unarchive/{id}")
    public AjaxResult unarchive(@PathVariable Long id)
    {
        return toAjax(sequenceService.unarchive(id));
    }

    /** 获取序列内影像列表 */
    @RequiresPermissions("imaging:sequence:query")
    @GetMapping("/{id}/images")
    public AjaxResult images(@PathVariable Long id)
    {
        List<ChestXray> list = sequenceService.listImages(id);
        return success(list);
    }

    /** 批量设置序列内影像 */
    @RequiresPermissions("imaging:sequence:edit")
    @PutMapping("/{id}/images")
    public AjaxResult setImages(@PathVariable Long id, @RequestBody List<Long> imageIds)
    {
        sequenceService.replaceImages(id, imageIds);
        return success();
    }
}
