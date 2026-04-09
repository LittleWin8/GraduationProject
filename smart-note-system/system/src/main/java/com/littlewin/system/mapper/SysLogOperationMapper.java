package com.littlewin.system.mapper;

import com.littlewin.system.domain.entity.SysLogOperation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysLogOperationMapper {
    int insertOperationLog(SysLogOperation log);
}