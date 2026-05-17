package com.ruoyi.emr.service;

import com.ruoyi.emr.domain.vo.XrayEnhanceResultVo;

public interface IXrayImageEnhanceService
{
    XrayEnhanceResultVo ensureEnhanced(Long imageId);

    void invalidateEnhanced(Long imageId);
}
