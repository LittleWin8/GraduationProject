package com.littlewin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.littlewin.system.domain.entity.SysDictData;
import com.littlewin.system.domain.entity.SysDictType;
import com.littlewin.system.mapper.SysDictDataMapper;
import com.littlewin.system.mapper.SysDictTypeMapper;
import com.littlewin.system.service.SysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysDictServiceImpl implements SysDictService {

    @Autowired
    private SysDictDataMapper dictDataMapper;

    @Autowired
    private SysDictTypeMapper dictTypeMapper;

    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        return dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 1) // 仅查询正常状态
                .orderByAsc(SysDictData::getSortOrder));
    }

    @Override
    public List<SysDictType> selectDictTypeList() {
        return dictTypeMapper.selectList(null);
    }
}
