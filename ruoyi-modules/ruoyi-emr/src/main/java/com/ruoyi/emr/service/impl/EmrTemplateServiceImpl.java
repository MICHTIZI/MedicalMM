package com.ruoyi.emr.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.emr.domain.EmrTemplate;
import com.ruoyi.emr.mapper.EmrTemplateMapper;
import com.ruoyi.emr.service.IEmrTemplateService;

@Service
public class EmrTemplateServiceImpl extends ServiceImpl<EmrTemplateMapper, EmrTemplate> implements IEmrTemplateService
{
    @Override
    public IPage<EmrTemplate> selectPage(Page<EmrTemplate> page, EmrTemplate query)
    {
        LambdaQueryWrapper<EmrTemplate> w = new LambdaQueryWrapper<>();
        if (query != null)
        {
            if (StringUtils.hasText(query.getTemplateName()))
            {
                w.like(EmrTemplate::getTemplateName, query.getTemplateName());
            }
            if (StringUtils.hasText(query.getTemplateType()))
            {
                w.eq(EmrTemplate::getTemplateType, query.getTemplateType());
            }
        }
        w.orderByDesc(EmrTemplate::getCreateTime);
        return page(page, w);
    }
}
