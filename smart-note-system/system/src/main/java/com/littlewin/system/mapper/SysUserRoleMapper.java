package com.littlewin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.littlewin.system.domain.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 批量新增用户角色信息
     * @param userRoleList 用户角色列表
     */
    int batchUserRole(List<SysUserRole> userRoleList);
}