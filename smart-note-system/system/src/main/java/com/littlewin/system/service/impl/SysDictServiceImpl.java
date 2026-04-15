package com.littlewin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.littlewin.common.enums.LogAction;
import com.littlewin.common.enums.LogStatus;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.system.domain.dto.AdminLoginDTO;
import com.littlewin.system.domain.dto.DictTypeQueryDTO;
import com.littlewin.system.domain.entity.SysDictData;
import com.littlewin.system.domain.entity.SysDictType;
import com.littlewin.system.mapper.SysDictDataMapper;
import com.littlewin.system.mapper.SysDictTypeMapper;
import com.littlewin.system.mapper.UserAuthMapper;
import com.littlewin.system.service.SysDictService;
import com.littlewin.system.service.SysLogService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictService {
    @Autowired
    private SysDictDataMapper dictDataMapper;

    @Autowired
    private SysDictTypeMapper dictTypeMapper;

    @Resource
    private UserAuthMapper userAuthMapper;

    @Resource
    private SysLogService sysLogService;

    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        return dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 1) // 仅查询正常状态
                .orderByAsc(SysDictData::getSortOrder));
    }

    @Override
    public IPage<SysDictType> selectDictTypePage(DictTypeQueryDTO queryDTO) {
        // 1. 创建 MyBatis-Plus 分页对象
        Page<SysDictType> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        // 2. 执行 SQL 查询
        return dictTypeMapper.selectDictTypePageList(page, queryDTO);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateDictType(SysDictType dictType) {
        // 1. 获取旧数据（用于获取原字典名进行比对）
        SysDictType oldDict = this.getById(dictType.getDictId());
        if (oldDict == null) return false;

        // 2. 获取当前登录用户
        String userIdStr = SecurityUtils.getUserId();
        AdminLoginDTO user = userAuthMapper.selectAdminLoginUser(userIdStr);

        // 3. 构建日志描述
        StringBuilder desc = new StringBuilder("编辑字典【" + oldDict.getDictName() + "】");

        // 如果前端传了新的字典名，且与旧名不同，记录变更
        if (dictType.getDictName() != null && !dictType.getDictName().equals(oldDict.getDictName())) {
            desc.append("，字典名由[").append(oldDict.getDictName())
                    .append("]变更为[").append(dictType.getDictName()).append("]");
        }

        try {
            // 4. 执行数据库更新
            boolean success = this.updateById(dictType);

            if (success) {
                // 5. 记录日志
                sysLogService.recordDictLog(user, dictType.getDictId(), LogAction.UPDATE,
                        LogStatus.SUCCESS, desc.toString(), null);
            }
            return success;
        } catch (Exception e) {
            // 失败也记录日志
            sysLogService.recordDictLog(user, dictType.getDictId(), LogAction.UPDATE,
                    LogStatus.FAIL, desc.toString(), e.getMessage());
            throw e;
        }
    }
}
