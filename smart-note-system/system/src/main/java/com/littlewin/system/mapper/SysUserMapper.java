package com.littlewin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.littlewin.system.domain.dto.UserQueryDTO;
import com.littlewin.system.domain.entity.SysUser;
import com.littlewin.system.domain.vo.UserDetailsVO;
import com.littlewin.system.domain.vo.UserListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 多表关联分页查询用户列表
     * @param page 分页参数
     * @param query 查询过滤条件
     */
    IPage<UserListVO> selectUserPageList(Page<UserListVO> page, @Param("query") UserQueryDTO query);

    /**
     * 根据ID查询用户完整详情
     */
    UserDetailsVO selectUserDetailsById(@Param("userId") Long userId);
}