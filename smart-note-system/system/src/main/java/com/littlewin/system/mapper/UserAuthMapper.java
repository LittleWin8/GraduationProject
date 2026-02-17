package com.littlewin.system.mapper;

import com.littlewin.system.domain.entity.UserAuth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserAuthMapper {

    @Select("""
        SELECT * FROM user_auth
        WHERE identifier = #{identifier}
        AND auth_type = 'password'
        """)
    UserAuth selectByIdentifier(String identifier);
}
