package com.littlewin.common.log.manager;

import com.littlewin.common.log.entity.SysLogOperation;
import com.littlewin.common.log.mapper.SysLogOperationMapper;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LogAsyncManager {
    @Resource
    private SysLogOperationMapper sysLogOperationMapper;

    @Async("layoutThreadPool")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(SysLogOperation log) {
        sysLogOperationMapper.insertOperationLog(log);
    }
}