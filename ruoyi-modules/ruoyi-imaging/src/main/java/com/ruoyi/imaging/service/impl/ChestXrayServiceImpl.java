package com.ruoyi.imaging.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.imaging.domain.ChestXray;
import com.ruoyi.imaging.domain.query.ChestXrayQuery;
import com.ruoyi.imaging.mapper.ChestXrayMapper;
import com.ruoyi.imaging.service.IChestXrayService;

@Service
public class ChestXrayServiceImpl extends ServiceImpl<ChestXrayMapper, ChestXray> implements IChestXrayService
{
    @Override
    public IPage<ChestXray> selectPage(Page<ChestXray> page, ChestXrayQuery query)
    {
        return baseMapper.selectChestXrayPage(page, query);
    }

    @Override
    public List<ChestXray> listExport(ChestXrayQuery query)
    {
        Page<ChestXray> page = new Page<>(1, 5000);
        IPage<ChestXray> result = baseMapper.selectChestXrayPage(page, query);
        return result.getRecords() != null ? result.getRecords() : new ArrayList<>();
    }

    @Override
    public boolean updateDiseases(Long id, String diseases, String labelStr)
    {
        ChestXray entity = getById(id);
        if (entity == null) return false;
        entity.setDiseases(diseases);
        entity.setLabelStr(labelStr);
        return updateById(entity);
    }
}
