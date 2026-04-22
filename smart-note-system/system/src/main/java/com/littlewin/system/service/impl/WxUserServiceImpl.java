package com.littlewin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.log.context.LogContext;
import com.littlewin.common.utils.JwtUtils;
import com.littlewin.common.utils.ServletUtils;
import com.littlewin.common.utils.WechatApiUtils;
import com.littlewin.system.domain.entity.SysUser;
import com.littlewin.system.domain.entity.UserAuth;
import com.littlewin.system.domain.entity.UserInfo;
import com.littlewin.system.mapper.SysUserMapper;
import com.littlewin.system.mapper.UserAuthMapper;
import com.littlewin.system.mapper.UserInfoMapper;
import com.littlewin.system.service.WxUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WxUserServiceImpl implements WxUserService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private UserAuthMapper userAuthMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private WechatApiUtils wechatApiUtils;  // 注入工具类

    // WxUserServiceImpl.java
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> login(String code, String nickName, String avatarUrl) {


        // 1. 获取 openid
        WechatApiUtils.WechatSession session = wechatApiUtils.getSessionByCode(code);
        String openid = session.getOpenid();

        // 2. 查询认证表
        UserAuth auth = userAuthMapper.selectOne(new LambdaQueryWrapper<UserAuth>()
                .eq(UserAuth::getAuthType, "wx_openid")
                .eq(UserAuth::getIdentifier, openid));

        Long userId;
        boolean isNewUser = (auth == null);
        Map<String, Object> result = new HashMap<>();

        if (isNewUser) {

            // 如果是新用户，且前端没有传昵称（说明是静默登录尝试）
            if (nickName == null || nickName.isEmpty()) {
                result.put("isNewUser", true);
                result.put("token", null);
                return result;
            }

            // 3. 新用户注册
            SysUser user = new SysUser();
            user.setNickname(nickName);
            user.setAvatar(avatarUrl != null ? avatarUrl : "");
            sysUserMapper.insert(user);
            userId = user.getUserId();

            UserAuth newAuth = new UserAuth();
            newAuth.setUserId(userId);
            newAuth.setAuthType("wx_openid");
            newAuth.setIdentifier(openid);
            userAuthMapper.insert(newAuth);

            UserInfo info = new UserInfo();
            info.setUserId(userId);
            info.setLastLoginIp(ServletUtils.getClientIp());
            info.setLastLoginTime(LocalDateTime.now());
            userInfoMapper.insert(info);

            LogContext.setDesc("微信新用户注册并登录");
        } else {
            // 4. 老用户逻辑 - 直接获取 userId
            userId = auth.getUserId();

            // 如果老用户在登录时带了新的头像昵称，则更新
            if (nickName != null && !nickName.isEmpty()) {
                SysUser user = new SysUser();
                user.setUserId(userId);
                user.setNickname(nickName);
                if (avatarUrl != null) user.setAvatar(avatarUrl);
                sysUserMapper.updateById(user);
            }

            UserInfo info = new UserInfo();
            info.setUserId(userId);
            info.setLastLoginIp(ServletUtils.getClientIp());
            info.setLastLoginTime(LocalDateTime.now());
            userInfoMapper.updateById(info);

            LogContext.setDesc("微信用户登录");
        }

        LogContext.setBusinessId(userId);
        LogContext.setUsername(openid);

        // 5. 返回结果
        result.put("isNewUser", false);
        result.put("token", JwtUtils.createToken(userId.toString()));
        return result;
    }
}