package com.littlewin.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.littlewin.system.domain.dto.AdminLoginDTO;

@Mapper
public interface UserAuthMapper {

    AdminLoginDTO selectAdminLoginUser(String username);
}