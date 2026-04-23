package com.littlewin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.littlewin.system.domain.entity.SysMenu;
import com.littlewin.system.domain.entity.UserAuth;
import com.littlewin.system.domain.vo.UserInfoVO;
import org.apache.ibatis.annotations.Mapper;
import com.littlewin.common.core.LoginDTO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserAuthMapper extends BaseMapper<UserAuth> {

    /**
     * 查询登录用户信息（基础信息+密码）
     */
    LoginDTO selectAdminLoginUser(String username);

    /**
     * 根据用户ID查询登录用户信息（适配微信用户）
     * 对应 XML 中的 selectWxLoginUserById
     *
     * @param userId 用户ID
     * @return 登录用户数据传输对象
     */
    LoginDTO selectWxLoginUserById(@Param("userId") Long userId);

    /**
     * 检查用户名（user_auth.identifier）
     */
    LoginDTO checkIdentifierUnique(String username);
    /**
     * 根据用户ID查询所有权限标识 (perms)
     */
    List<String> selectMenuPermsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询所有菜单记录 (M目录和C菜单)
     */
    List<SysMenu> selectMenuListByUserId(@Param("userId") Long userId);
    List<SysMenu> selectButtonListByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询该用户拥有的所有角色Key (如: ['admin', 'editor'])
     */
    List<String> selectRoleKeysByUserId(@Param("userId") Long userId);

    /**
     * 联查获取用户所有详细信息（User + Info + Auth）
     */
    UserInfoVO selectFullUserInfoById(@Param("userId") Long userId);

    /**
     * 更新用户最后登录信息
     */
    int updateLoginInfo(@Param("userId") Long userId,
                        @Param("ip") String ip,
                        @Param("loginTime") LocalDateTime loginTime);

    void updateIdentifierByUserId(@Param("userId") Long userId, @Param("identifier") String identifier);
}