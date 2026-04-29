package com.littlewin.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.littlewin.system.domain.dto.BehaviorLogDTO;
import com.littlewin.system.domain.dto.BehaviorLogQueryDTO;
import com.littlewin.system.domain.dto.OperationLogQueryDTO;
import com.littlewin.system.domain.vo.BehaviorLogVO;
import com.littlewin.system.domain.vo.OperationLogVO;

/**
 * 日志服务接口
 */
public interface LogService {

    /**
     * 上报行为日志
     */
    void reportBehavior(BehaviorLogDTO dto);

    /**
     * 操作日志分页查询
     */
    IPage<OperationLogVO> getOperationLogPage(OperationLogQueryDTO query);

    /**
     * 行为日志分页查询
     */
    IPage<BehaviorLogVO> getBehaviorLogPage(BehaviorLogQueryDTO query);
}
