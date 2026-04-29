package com.littlewin.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.common.core.LoginDTO;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.system.domain.dto.BehaviorLogDTO;
import com.littlewin.system.domain.dto.BehaviorLogQueryDTO;
import com.littlewin.system.domain.dto.OperationLogQueryDTO;
import com.littlewin.system.domain.entity.SysLogBehavior;
import com.littlewin.system.domain.vo.BehaviorLogVO;
import com.littlewin.system.domain.vo.OperationLogVO;
import com.littlewin.system.mapper.SysLogBehaviorMapper;
import com.littlewin.system.mapper.SysLogOperationQueryMapper;
import com.littlewin.system.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 日志服务实现
 */
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final SysLogBehaviorMapper sysLogBehaviorMapper;
    private final SysLogOperationQueryMapper sysLogOperationQueryMapper;

    /** type → actionType 映射 */
    private static final Map<String, Integer> TYPE_MAP = Map.of(
            "view", 1,
            "search", 2
    );

    @Override
    public void reportBehavior(BehaviorLogDTO dto) {
        Integer actionType = TYPE_MAP.get(dto.getType());
        if (actionType == null) {
            throw new ServiceException("无效的行为类型: " + dto.getType());
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new ServiceException("行为内容不能为空");
        }

        LoginDTO loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            throw new ServiceException("用户未登录");
        }

        SysLogBehavior log = new SysLogBehavior();
        log.setUserId(loginUser.getUserId());
        log.setActionType(actionType);
        log.setContent(dto.getContent().trim());
        sysLogBehaviorMapper.insert(log);
    }

    @Override
    public IPage<OperationLogVO> getOperationLogPage(OperationLogQueryDTO query) {
        Page<OperationLogVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return sysLogOperationQueryMapper.selectOperationLogPage(page, query);
    }

    @Override
    public IPage<BehaviorLogVO> getBehaviorLogPage(BehaviorLogQueryDTO query) {
        Page<BehaviorLogVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return sysLogBehaviorMapper.selectBehaviorLogPage(page, query);
    }
}
