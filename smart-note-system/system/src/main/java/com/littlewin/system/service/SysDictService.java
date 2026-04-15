package com.littlewin.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.littlewin.system.domain.dto.AdminLoginDTO;
import com.littlewin.system.domain.dto.DictTypeQueryDTO;
import com.littlewin.system.domain.entity.SysDictData;
import com.littlewin.system.domain.entity.SysDictType;

import java.util.List;

public interface SysDictService extends IService<SysDictType> {
    /** 根据字典类型获取数据列表 (前端最常用) */
    List<SysDictData> selectDictDataByType(String dictType);

    /** 获取所有字典类型列表 (后台管理用) */
    IPage<SysDictType> selectDictTypePage(DictTypeQueryDTO queryDTO);

    /**
     * 更新字典类型（封装业务与日志审计）
     * @param dictType 字典数据
     */
    boolean updateDictType(SysDictType dictType);
}
