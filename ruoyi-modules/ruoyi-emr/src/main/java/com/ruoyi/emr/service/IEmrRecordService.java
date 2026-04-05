package com.ruoyi.emr.service;

import java.util.List;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.emr.domain.EmrEntity;
import com.ruoyi.emr.domain.EmrRecord;
import com.ruoyi.emr.domain.query.EmrRecordQuery;
import com.ruoyi.emr.domain.vo.EmrRecordDetailVo;
import com.ruoyi.emr.domain.vo.EmrRecordExcel;
import com.ruoyi.emr.domain.vo.EmrRecordSaveVo;

public interface IEmrRecordService extends IService<EmrRecord>
{
    IPage<EmrRecord> selectRecordPage(Page<EmrRecord> page, EmrRecordQuery query);

    EmrRecordDetailVo getDetail(Long id);

    Long saveWithEntities(EmrRecordSaveVo vo);

    boolean updateWithEntities(EmrRecordSaveVo vo);

    boolean removeWithEntities(Long id);

    boolean archive(Long id);

    boolean unarchive(Long id);

    List<EmrEntity> extractEntities(String contentHtml);

    List<EmrRecordExcel> listExport(EmrRecordQuery query);

    String importRecords(List<EmrRecordExcel> list);
}
