package com.littlewin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.littlewin.common.enums.LogAction;
import com.littlewin.common.enums.LogStatus;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.system.domain.dto.AdminLoginDTO;
import com.littlewin.system.domain.dto.DictDataQueryDTO;
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
    public IPage<SysDictType> selectDictTypePage(DictTypeQueryDTO queryDTO) {
        // 1. 创建 MyBatis-Plus 分页对象
        Page<SysDictType> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        // 2. 执行 SQL 查询
        return dictTypeMapper.selectDictTypePageList(page, queryDTO);
    }

    // 编辑字典
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


    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean insertDictType(SysDictType dictType) {
        // 1. 查重：防止字典类型编码重复
        long count = this.count(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dictType.getDictType()));
        if (count > 0) {
            throw new RuntimeException("字典类型[" + dictType.getDictType() + "]已存在");
        }

        // 2. 保存并记录日志
        boolean success = this.save(dictType);
        if (success) {
            String userIdStr = SecurityUtils.getUserId();
            AdminLoginDTO user = userAuthMapper.selectAdminLoginUser(userIdStr);
            sysLogService.recordDictLog(user, dictType.getDictId(), LogAction.CREATE,
                    LogStatus.SUCCESS, "新增字典【" + dictType.getDictName() + "】", null);
        }
        return success;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteDictTypeByIds(List<Long> ids) {
        String userIdStr = SecurityUtils.getUserId();
        AdminLoginDTO user = userAuthMapper.selectAdminLoginUser(userIdStr);

        for (Long id : ids) {
            SysDictType dictType = this.getById(id);
            if (dictType == null) continue;

            // 3. 级联校验：如果该类型下已有字典数据，不允许删除
            long dataCount = dictDataMapper.selectCount(new LambdaQueryWrapper<SysDictData>()
                    .eq(SysDictData::getDictType, dictType.getDictType()));
            if (dataCount > 0) {
                throw new RuntimeException("字典【" + dictType.getDictName() + "】下尚有数据项，请先清空数据");
            }

            this.removeById(id);
            sysLogService.recordDictLog(user, id, LogAction.DELETE,
                    LogStatus.SUCCESS, "删除字典【" + dictType.getDictName() + "】", null);
        }
        return true;
    }

    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        return dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 1) // 仅查询正常状态
                .orderByAsc(SysDictData::getSortOrder));
    }


    /**
     * 分页查询字典数据
     * 这里强制要求 dictType 不能为空，因为详情页是基于某个类型展示的
     */
    @Override
    public IPage<SysDictData> selectDictDataPage(DictDataQueryDTO queryDTO) {
        // 1. 创建分页对象
        Page<SysDictData> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, queryDTO.getDictType()) // 核心过滤条件
                .like(queryDTO.getDictLabel() != null, SysDictData::getDictLabel, queryDTO.getDictLabel())
                .eq(queryDTO.getStatus() != null, SysDictData::getStatus, queryDTO.getStatus())
                .orderByAsc(SysDictData::getSortOrder); // 默认按排序号升序

        return dictDataMapper.selectPage(page, wrapper);
    }

    /**
     * 新增字典数据
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean insertDictData(SysDictData dictData) {
        // 1. 校验在该字典类型下，键值(dictValue)是否重复
        long count = dictDataMapper.selectCount(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictData.getDictType())
                .eq(SysDictData::getDictValue, dictData.getDictValue()));
        if (count > 0) {
            throw new RuntimeException("键值【" + dictData.getDictValue() + "】已存在，请勿重复添加");
        }

        // 2. 执行插入
        boolean success = dictDataMapper.insert(dictData) > 0;

        // 3. 记录日志
        if (success) {
            String userIdStr = SecurityUtils.getUserId();
            AdminLoginDTO user = userAuthMapper.selectAdminLoginUser(userIdStr);
            String desc = String.format("新增字典项：类型[%s], 标签[%s], 键值[%s]",
                    dictData.getDictType(), dictData.getDictLabel(), dictData.getDictValue());

            sysLogService.recordDictLog(user, dictData.getDataId(), LogAction.CREATE,
                    LogStatus.SUCCESS, desc, null);
        }
        // 执行插入
        return success;
    }

    /**
     * 修改字典数据
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateDictData(SysDictData dictData) {
        // 1. 获取旧数据用于日志对比
        SysDictData oldData = dictDataMapper.selectById(dictData.getDataId());

        String userIdStr = SecurityUtils.getUserId();
        AdminLoginDTO user = userAuthMapper.selectAdminLoginUser(userIdStr);
        String desc = String.format("修改字典项【%s】，所属类型[%s]",
                oldData.getDictLabel(), oldData.getDictType());

        try {
            // 2. 执行更新
            boolean success = dictDataMapper.updateById(dictData) > 0;
            if (success) {
                sysLogService.recordDictLog(user, dictData.getDataId(), LogAction.UPDATE,
                        LogStatus.SUCCESS, desc, null);
            }
            return success;
        } catch (Exception e) {
            sysLogService.recordDictLog(user, dictData.getDataId(), LogAction.UPDATE,
                    LogStatus.FAIL, desc, e.getMessage());
            throw e;
        }
    }

    /**
     * 批量删除字典数据
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteDictDataByIds(List<Long> ids) {
        String userIdStr = SecurityUtils.getUserId();
        AdminLoginDTO user = userAuthMapper.selectAdminLoginUser(userIdStr);

        // 循环删除以记录详细日志（如果数据量极大，建议权衡性能改用批量日志）
        for (Long id : ids) {
            SysDictData data = dictDataMapper.selectById(id);
            if (data != null) {
                dictDataMapper.deleteById(id);
                String desc = String.format("删除字典项：类型[%s], 标签[%s]",
                        data.getDictType(), data.getDictLabel());
                sysLogService.recordDictLog(user, id, LogAction.DELETE,
                        LogStatus.SUCCESS, desc, null);
            }
        }
        return true;
    }

}
