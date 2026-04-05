package com.ruoyi.emr.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.emr.domain.EmrTemplate;

public interface IEmrTemplateService extends IService<EmrTemplate>
{
    IPage<EmrTemplate> selectPage(Page<EmrTemplate> page, EmrTemplate query);
}
