package com.ruoyi.imaging.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.imaging.domain.ChestXray;
import com.ruoyi.imaging.domain.ImagingSequence;
import com.ruoyi.imaging.domain.SequenceImage;
import com.ruoyi.imaging.mapper.ChestXrayMapper;
import com.ruoyi.imaging.mapper.ImagingSequenceMapper;
import com.ruoyi.imaging.mapper.SequenceImageMapper;
import com.ruoyi.imaging.service.IImagingSequenceService;

@Service
public class ImagingSequenceServiceImpl extends ServiceImpl<ImagingSequenceMapper, ImagingSequence> implements IImagingSequenceService
{
    @Autowired
    private SequenceImageMapper sequenceImageMapper;

    @Autowired
    private ChestXrayMapper chestXrayMapper;

    @Override
    public boolean archive(Long id)
    {
        ImagingSequence seq = getById(id);
        if (seq == null) return false;
        seq.setArchiveStatus("1");
        seq.setUpdateTime(new Date());
        return updateById(seq);
    }

    @Override
    public boolean unarchive(Long id)
    {
        ImagingSequence seq = getById(id);
        if (seq == null) return false;
        seq.setArchiveStatus("0");
        seq.setUpdateTime(new Date());
        return updateById(seq);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceImages(Long seqId, List<Long> imageIds)
    {
        sequenceImageMapper.delete(new LambdaQueryWrapper<SequenceImage>().eq(SequenceImage::getSeqId, seqId));
        if (imageIds == null || imageIds.isEmpty()) return;
        int sort = 0;
        for (Long imgId : imageIds)
        {
            SequenceImage si = new SequenceImage();
            si.setSeqId(seqId);
            si.setImageId(imgId);
            si.setSortOrder(sort++);
            sequenceImageMapper.insert(si);
        }
    }

    @Override
    public List<ChestXray> listImages(Long seqId)
    {
        List<SequenceImage> rels = sequenceImageMapper.selectList(
            new LambdaQueryWrapper<SequenceImage>().eq(SequenceImage::getSeqId, seqId).orderByAsc(SequenceImage::getSortOrder));
        if (rels == null || rels.isEmpty()) return new ArrayList<>();
        List<ChestXray> list = new ArrayList<>();
        for (SequenceImage si : rels)
        {
            ChestXray img = chestXrayMapper.selectById(si.getImageId());
            if (img != null) list.add(img);
        }
        return list;
    }
}
