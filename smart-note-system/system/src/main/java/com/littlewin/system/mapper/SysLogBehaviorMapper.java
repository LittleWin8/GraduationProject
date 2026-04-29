package com.littlewin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.system.domain.dto.BehaviorLogQueryDTO;
import com.littlewin.system.domain.entity.SysLogBehavior;
import com.littlewin.system.domain.vo.BehaviorLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 行为日志 Mapper
 */
@Mapper
public interface SysLogBehaviorMapper extends BaseMapper<SysLogBehavior> {

    /**
     * 分页查询行为日志（关联 sys_user 获取昵称）
     */
    IPage<BehaviorLogVO> selectBehaviorLogPage(Page<BehaviorLogVO> page, @Param("query") BehaviorLogQueryDTO query);
}
