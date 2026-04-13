package com.littlewin.system.service;

import com.littlewin.system.domain.entity.SysDictData;
import com.littlewin.system.domain.entity.SysDictType;

import java.util.List;

public interface SysDictService {
    /** 根据字典类型获取数据列表 (前端最常用) */
    List<SysDictData> selectDictDataByType(String dictType);

    /** 获取所有字典类型列表 (后台管理用) */
    List<SysDictType> selectDictTypeList();
}
