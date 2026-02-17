package com.littlewin.system.mapper;

import com.littlewin.system.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper {

    @Select("""
        SELECT * FROM sys_user
        WHERE user_id = #{userId}
        """)
    SysUser selectById(Long userId);
}
