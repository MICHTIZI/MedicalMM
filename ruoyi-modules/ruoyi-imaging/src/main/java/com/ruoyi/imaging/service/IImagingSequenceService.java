package com.ruoyi.imaging.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.imaging.domain.ChestXray;
import com.ruoyi.imaging.domain.ImagingSequence;

public interface IImagingSequenceService extends IService<ImagingSequence>
{
    boolean archive(Long id);

    boolean unarchive(Long id);

    /** 设置序列内影像列表（批量替换） */
    void replaceImages(Long seqId, List<Long> imageIds);

    /** 获取序列内所有影像 */
    List<ChestXray> listImages(Long seqId);
}
