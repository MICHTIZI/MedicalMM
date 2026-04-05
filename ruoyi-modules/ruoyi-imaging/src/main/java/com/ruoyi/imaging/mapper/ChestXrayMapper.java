package com.ruoyi.imaging.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.imaging.domain.ChestXray;
import com.ruoyi.imaging.domain.query.ChestXrayQuery;
import org.apache.ibatis.annotations.Param;

public interface ChestXrayMapper extends BaseMapper<ChestXray>
{
    IPage<ChestXray> selectChestXrayPage(Page<ChestXray> page, @Param("q") ChestXrayQuery query);
}
