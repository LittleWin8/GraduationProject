package com.littlewin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.littlewin.system.domain.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    /**
     * 根据用户ID查询所属角色列表
     */
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);
}
