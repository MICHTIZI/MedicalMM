package com.ruoyi.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.emr.domain.ChestXray;
import com.ruoyi.emr.domain.query.ChestXrayQuery;
import org.apache.ibatis.annotations.Param;

public interface ChestXrayMapper extends BaseMapper<ChestXray>
{
    IPage<ChestXray> selectChestXrayPage(Page<ChestXray> page, @Param("q") ChestXrayQuery query);
}
