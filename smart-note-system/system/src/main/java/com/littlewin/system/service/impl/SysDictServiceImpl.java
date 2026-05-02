package com.littlewin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.littlewin.common.log.enums.LogAction;
import com.littlewin.common.log.enums.LogModule;
import com.littlewin.common.log.annotation.Log;
import com.littlewin.common.log.context.LogContext;
import com.littlewin.system.domain.dto.DictDataQueryDTO;
import com.littlewin.system.domain.dto.DictTypeQueryDTO;
import com.littlewin.system.domain.entity.SysDictData;
import com.littlewin.system.domain.entity.SysDictType;
import com.littlewin.system.mapper.SysDictDataMapper;
import com.littlewin.system.mapper.SysDictTypeMapper;
import com.littlewin.system.service.SysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictService {
    @Autowired
    private SysDictDataMapper dictDataMapper;

    @Autowired
    private SysDictTypeMapper dictTypeMapper;

    @Override
    public IPage<SysDictType> selectDictTypePage(DictTypeQueryDTO queryDTO) {
        // 1. 创建 MyBatis-Plus 分页对象
        Page<SysDictType> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        // 2. 执行 SQL 查询
        return dictTypeMapper.selectDictTypePageList(page, queryDTO);
    }

    // 编辑字典
    @Log(module = LogModule.DICT, action = LogAction.UPDATE)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateDictType(SysDictType dictType) {

        // 1. 获取旧数据（用于获取原字典名进行比对）
        SysDictType oldDict = this.getById(dictType.getDictId());
        if (oldDict == null) return false;

        // 2. 构建动态描述
        StringBuilder desc = new StringBuilder("编辑字典【" + oldDict.getDictName() + "】");
        if (dictType.getDictName() != null && !dictType.getDictName().equals(oldDict.getDictName())) {
            desc.append("，字典名由【").append(oldDict.getDictName())
                    .append("】变更为【").append(dictType.getDictName()).append("】");
        }

        // 3. 将动态生成的描述和业务ID存入上下文
        LogContext.setDesc(desc.toString());
        LogContext.setBusinessId(dictType.getDictId());

        // 4. 执行数据库更新
        return this.updateById(dictType);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    @Log(module = LogModule.DICT, action = LogAction.CREATE)
    public boolean insertDictType(SysDictType dictType) {

        LogContext.setDesc("新增字典类型【" + dictType.getDictName() + "】");

        // 1. 查重：防止字典类型编码重复
        long count = this.count(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dictType.getDictType()));
        if (count > 0) {
            throw new RuntimeException("字典类型【" + dictType.getDictType() + "】已存在");
        }

        // 2. 保存并记录日志
        boolean success = this.save(dictType);
        if (success) {
            LogContext.setBusinessId(dictType.getDictId());
        }
        return success;
    }

    /**
     * 删除字典类型
     */
    @Log(module = LogModule.DICT, action = LogAction.DELETE)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteDictTypeByIds(List<Long> ids) {
        LogContext.setDesc("批量删除字典类型，ID列表：" + ids.toString());

        for (Long id : ids) {
            SysDictType dictType = this.getById(id);
            if (dictType == null) continue;

            // 3. 级联校验：如果该类型下已有字典数据，不允许删除
            long dataCount = dictDataMapper.selectCount(new LambdaQueryWrapper<SysDictData>()
                    .eq(SysDictData::getDictType, dictType.getDictType()));
            if (dataCount > 0) {
                throw new RuntimeException("字典【" + dictType.getDictName() + "】下尚有数据项，请先清空数据");
            }

            this.removeById(id);
        }
        return true;
    }

    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        return dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 1) // 仅查询正常状态
                .orderByAsc(SysDictData::getSortOrder));
    }


    /**
     * 分页查询字典数据
     * 这里强制要求 dictType 不能为空，因为详情页是基于某个类型展示的
     */
    @Override
    public IPage<SysDictData> selectDictDataPage(DictDataQueryDTO queryDTO) {
        // 1. 创建分页对象
        Page<SysDictData> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, queryDTO.getDictType()) // 核心过滤条件
                .like(queryDTO.getDictLabel() != null, SysDictData::getDictLabel, queryDTO.getDictLabel())
                .eq(queryDTO.getStatus() != null, SysDictData::getStatus, queryDTO.getStatus())
                .orderByAsc(SysDictData::getSortOrder); // 默认按排序号升序

        return dictDataMapper.selectPage(page, wrapper);
    }

    /**
     * 新增字典数据
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    @Log(module = LogModule.DICT, action = LogAction.CREATE)
    public boolean insertDictData(SysDictData dictData) {

        LogContext.setDesc(String.format("新增字典项：类型【%s】, 标签【%s】",
                dictData.getDictType(), dictData.getDictLabel()));

        // 1. 校验在该字典类型下，键值(dictValue)是否重复
        long count = dictDataMapper.selectCount(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictData.getDictType())
                .eq(SysDictData::getDictValue, dictData.getDictValue()));
        if (count > 0) {
            throw new RuntimeException("键值【" + dictData.getDictValue() + "】已存在，请勿重复添加");
        }

        // 2. 执行插入
        boolean success = dictDataMapper.insert(dictData) > 0;
        if (success) {
            LogContext.setBusinessId(dictData.getDataId());
        }

        return success;
    }

    /**
     * 修改字典数据
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    @Log(module = LogModule.DICT, action = LogAction.UPDATE)
    public boolean updateDictData(SysDictData dictData) {
        // 1. 获取旧数据用于日志对比
        SysDictData oldData = dictDataMapper.selectById(dictData.getDataId());
        if (oldData == null) return false;

        LogContext.setDesc(String.format("修改字典项【%s】，所属类型【%s】",
                oldData.getDictLabel(), oldData.getDictType()));
        LogContext.setBusinessId(dictData.getDataId());

        return dictDataMapper.updateById(dictData) > 0;
    }

    /**
     * 批量删除字典数据
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    @Log(module = LogModule.DICT, action = LogAction.DELETE)
    public boolean deleteDictDataByIds(List<Long> ids) {
        LogContext.setDesc("批量删除字典项，ID列表：" + ids.toString());

        for (Long id : ids) {
            dictDataMapper.deleteById(id);
        }
        return true;
    }

}
