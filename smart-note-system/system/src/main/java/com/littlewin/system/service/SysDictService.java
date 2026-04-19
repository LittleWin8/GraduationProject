package com.littlewin.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.littlewin.system.domain.dto.DictDataQueryDTO;
import com.littlewin.system.domain.dto.DictTypeQueryDTO;
import com.littlewin.system.domain.entity.SysDictData;
import com.littlewin.system.domain.entity.SysDictType;

import java.util.List;

public interface SysDictService extends IService<SysDictType> {

    /** 获取所有字典类型列表 (后台管理用) */
    IPage<SysDictType> selectDictTypePage(DictTypeQueryDTO queryDTO);

    /**
     * 更新字典类型（封装业务与日志审计）
     * @param dictType 字典数据
     */
    boolean updateDictType(SysDictType dictType);

    /**
     * 新增字典类型
     */
    boolean insertDictType(SysDictType dictType);

    /**
     * 删除字典类型（含业务校验）
     */
    boolean deleteDictTypeByIds(List<Long> dictIds);

    /** 根据字典类型获取数据列表 (前端最常用) */
    List<SysDictData> selectDictDataByType(String dictType);

    /**
     * 分页查询字典数据列表
     * @param queryDTO 查询条件（含 dictType, dictLabel, status 等）
     * @return 分页结果
     */
    IPage<SysDictData> selectDictDataPage(DictDataQueryDTO queryDTO);

    /**
     * 新增字典数据
     * @param dictData 字典数据实体
     * @return 是否成功
     */
    boolean insertDictData(SysDictData dictData);

    /**
     * 修改字典数据
     * @param dictData 字典数据实体
     * @return 是否成功
     */
    boolean updateDictData(SysDictData dictData);

    /**
     * 批量删除字典数据
     * @param ids 主键ID集合
     * @return 是否成功
     */
    boolean deleteDictDataByIds(List<Long> ids);
}
