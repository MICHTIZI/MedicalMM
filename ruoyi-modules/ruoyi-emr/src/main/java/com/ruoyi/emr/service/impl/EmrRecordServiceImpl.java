package com.ruoyi.emr.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.emr.domain.EmrEntity;
import com.ruoyi.emr.domain.EmrLabelType;
import com.ruoyi.emr.domain.EmrRecord;
import com.ruoyi.emr.domain.query.EntityFilterItem;
import com.ruoyi.emr.domain.query.EmrRecordQuery;
import com.ruoyi.emr.domain.vo.EmrRecordDetailVo;
import com.ruoyi.emr.domain.vo.EmrRecordExcel;
import com.ruoyi.emr.domain.vo.EmrRecordSaveVo;
import com.ruoyi.emr.mapper.EmrEntityMapper;
import com.ruoyi.emr.mapper.EmrRecordMapper;
import com.ruoyi.emr.service.IEmrRecordService;
import com.ruoyi.emr.util.EmrEntityExtractor;
import com.ruoyi.emr.util.EmrHtmlUtils;

/**
 * 电子病历主表与实体联动：分页检索、详情高亮、归档、导入导出等。
 */
@Service
public class EmrRecordServiceImpl extends ServiceImpl<EmrRecordMapper, EmrRecord> implements IEmrRecordService
{
    @Autowired
    private EmrEntityMapper emrEntityMapper;

    @Override
    public IPage<EmrRecord> selectRecordPage(Page<EmrRecord> page, EmrRecordQuery query)
    {
        normalizeEntityFilters(query);
        return baseMapper.selectRecordPage(page, query);
    }

    private void normalizeEntityFilters(EmrRecordQuery query)
    {
        if (query.getEntityFilters() == null)
        {
            query.setEntityFilters(new ArrayList<>());
        }
        addFilter(query, query.getE1Type(), query.getE1Keyword());
        addFilter(query, query.getE2Type(), query.getE2Keyword());
        addFilter(query, query.getE3Type(), query.getE3Keyword());
    }

    private void addFilter(EmrRecordQuery query, String type, String kw)
    {
        if (StringUtils.isEmpty(type) || StringUtils.isEmpty(kw))
        {
            return;
        }
        if (!EmrLabelType.isValid(type))
        {
            return;
        }
        EntityFilterItem f = new EntityFilterItem();
        f.setLabelType(type);
        f.setKeyword(kw);
        query.getEntityFilters().add(f);
    }

    @Override
    public EmrRecordDetailVo getDetail(Long id)
    {
        EmrRecord record = getById(id);
        if (record == null)
        {
            return null;
        }
        List<EmrEntity> entities = emrEntityMapper.selectList(
            new LambdaQueryWrapper<EmrEntity>().eq(EmrEntity::getRecordId, id).orderByAsc(EmrEntity::getStartPos));
        String plain = EmrHtmlUtils.toPlainText(record.getContent());
        EmrRecordDetailVo vo = new EmrRecordDetailVo();
        vo.setRecord(record);
        vo.setEntities(entities);
        vo.setPlainText(plain);
        vo.setHighlightedHtml(EmrHtmlUtils.highlightPlain(plain, entities));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveWithEntities(EmrRecordSaveVo vo)
    {
        EmrRecord r = vo.getRecord();
        if (r.getArchiveStatus() == null)
        {
            r.setArchiveStatus("0");
        }
        r.setCreateTime(new Date());
        r.setArchivedTime(null);
        validateEntities(vo.getEntities());
        save(r);
        Long id = r.getId();
        replaceEntities(id, vo.getEntities());
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateWithEntities(EmrRecordSaveVo vo)
    {
        EmrRecord r = vo.getRecord();
        if (r.getId() == null)
        {
            return false;
        }
        EmrRecord old = getById(r.getId());
        if (old == null)
        {
            return false;
        }
        if ("1".equals(old.getArchiveStatus()))
        {
            throw new ServiceException("???r???????????");
        }
        validateEntities(vo.getEntities());
        r.setCreateTime(old.getCreateTime());
        if (!"1".equals(r.getArchiveStatus()))
        {
            r.setArchivedTime(null);
        }
        updateById(r);
        replaceEntities(r.getId(), vo.getEntities());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeWithEntities(Long id)
    {
        EmrRecord old = getById(id);
        if (old == null)
        {
            return false;
        }
        if ("1".equals(old.getArchiveStatus()))
        {
            throw new ServiceException("???r???????????");
        }
        emrEntityMapper.delete(new LambdaQueryWrapper<EmrEntity>().eq(EmrEntity::getRecordId, id));
        return removeById(id);
    }

    @Override
    public boolean archive(Long id)
    {
        EmrRecord r = getById(id);
        if (r == null)
        {
            return false;
        }
        r.setArchiveStatus("1");
        r.setArchivedTime(new Date());
        return updateById(r);
    }

    @Override
    public boolean unarchive(Long id)
    {
        EmrRecord r = getById(id);
        if (r == null)
        {
            return false;
        }
        r.setArchiveStatus("0");
        r.setArchivedTime(null);
        return updateById(r);
    }

    @Override
    public List<EmrEntity> extractEntities(String contentHtml)
    {
        String plain = EmrHtmlUtils.toPlainText(contentHtml);
        return EmrEntityExtractor.extract(plain);
    }

    @Override
    public List<EmrRecordExcel> listExport(EmrRecordQuery query)
    {
        normalizeEntityFilters(query);
        Page<EmrRecord> page = new Page<>(1, 5000);
        IPage<EmrRecord> p = baseMapper.selectRecordPage(page, query);
        List<EmrRecordExcel> rows = new ArrayList<>();
        for (EmrRecord r : p.getRecords())
        {
            EmrRecordExcel e = new EmrRecordExcel();
            e.setTitle(r.getTitle());
            e.setRecordType(r.getRecordType());
            e.setDisease(r.getDisease());
            e.setArchiveStatus(r.getArchiveStatus());
            e.setContent(r.getContent());
            e.setStructuredJson(r.getStructuredJson());
            e.setCreateTime(r.getCreateTime());
            rows.add(e);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importRecords(List<EmrRecordExcel> list)
    {
        if (list == null || list.isEmpty())
        {
            return "???????????";
        }
        int ok = 0;
        for (EmrRecordExcel row : list)
        {
            EmrRecord r = new EmrRecord();
            r.setTitle(StringUtils.isEmpty(row.getTitle()) ? "??????" : row.getTitle());
            r.setContent(row.getContent() == null ? "" : row.getContent());
            r.setStructuredJson(row.getStructuredJson());
            r.setDisease(row.getDisease());
            r.setRecordType(row.getRecordType());
            r.setArchiveStatus(row.getArchiveStatus() == null ? "0" : row.getArchiveStatus());
            r.setCreateTime(row.getCreateTime() != null ? row.getCreateTime() : new Date());
            save(r);
            ok++;
        }
        return "??????? " + ok + " ??????";
    }

    private void replaceEntities(Long recordId, List<EmrEntity> entities)
    {
        emrEntityMapper.delete(new LambdaQueryWrapper<EmrEntity>().eq(EmrEntity::getRecordId, recordId));
        if (entities == null || entities.isEmpty())
        {
            return;
        }
        Date now = new Date();
        for (EmrEntity e : entities)
        {
            if (!EmrLabelType.isValid(e.getLabelType()))
            {
                continue;
            }
            e.setId(null);
            e.setRecordId(recordId);
            e.setCreateTime(e.getCreateTime() != null ? e.getCreateTime() : now);
            emrEntityMapper.insert(e);
        }
    }

    private void validateEntities(List<EmrEntity> entities)
    {
        if (entities == null)
        {
            return;
        }
        for (EmrEntity e : entities)
        {
            if (e.getLabelType() != null && !EmrLabelType.isValid(e.getLabelType()))
            {
                throw new ServiceException("?????????????" + e.getLabelType());
            }
        }
    }
}
