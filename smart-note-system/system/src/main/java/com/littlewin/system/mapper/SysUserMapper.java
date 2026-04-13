package com.littlewin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.system.domain.dto.UserQueryDTO;
import com.littlewin.system.domain.vo.UserListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper extends BaseMapper<Object> { // 此处建议关联你的 SysUser 实体类

    /**
     * 多表关联分页查询用户列表
     * @param page 分页参数
     * @param query 查询过滤条件
     */
    IPage<UserListVO> selectUserPageList(Page<UserListVO> page, @Param("query") UserQueryDTO query);
}