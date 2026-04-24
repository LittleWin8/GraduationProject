package com.littlewin.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.littlewin.note.domain.entity.SysCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统预设分类 Mapper 接口
 */
@Mapper
public interface SysCategoryMapper extends BaseMapper<SysCategory> {
    // 如果需要自定义复杂的树形结构 SQL 查询，可以在此处扩展
}