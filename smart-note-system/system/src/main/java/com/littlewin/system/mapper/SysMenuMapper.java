package com.littlewin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.littlewin.system.domain.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 查询菜单树（所有菜单，按顺序排序）
     */
    List<SysMenu> selectMenuTree();

    /**
     * 根据角色ID查询按钮权限标识列表
     */
    List<String> selectPermsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色ID查询已分配的菜单ID列表
     */
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}