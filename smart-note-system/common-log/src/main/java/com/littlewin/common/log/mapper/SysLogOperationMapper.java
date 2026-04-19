package com.littlewin.common.log.mapper;

import com.littlewin.common.log.entity.SysLogOperation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysLogOperationMapper {
    int insertOperationLog(SysLogOperation log);
}