package com.littlewin.system.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.system.domain.dto.OperationLogQueryDTO;
import com.littlewin.system.domain.vo.OperationLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 操作日志查询 Mapper（放在 system 模块避免循环依赖）
 */
@Mapper
public interface SysLogOperationQueryMapper {

    /**
     * 分页查询操作日志
     */
    IPage<OperationLogVO> selectOperationLogPage(Page<OperationLogVO> page, @Param("query") OperationLogQueryDTO query);
}
