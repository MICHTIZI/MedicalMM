package com.ruoyi.emr.service;

import java.util.List;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.emr.domain.ChestXray;
import com.ruoyi.emr.domain.query.ChestXrayQuery;

public interface IChestXrayService extends IService<ChestXray>
{
    IPage<ChestXray> selectPage(Page<ChestXray> page, ChestXrayQuery query);

    List<ChestXray> listExport(ChestXrayQuery query);

    boolean updateDiseases(Long id, String diseases, String labelStr);
}
