package com.littlewin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.system.domain.dto.DictTypeQueryDTO;
import com.littlewin.system.domain.entity.SysDictType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysDictTypeMapper extends BaseMapper<SysDictType> {
    /**
     * 分页查询字典类型列表
     */
    IPage<SysDictType> selectDictTypePageList(IPage<SysDictType> page, @Param("query") DictTypeQueryDTO queryDTO);
}