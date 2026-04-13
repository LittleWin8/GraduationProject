package com.littlewin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.littlewin.system.domain.entity.SysDictData;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典数据表 Mapper 接口
 */
@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {
}