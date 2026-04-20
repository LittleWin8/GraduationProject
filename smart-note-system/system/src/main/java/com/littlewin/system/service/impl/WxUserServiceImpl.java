package com.littlewin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.log.context.LogContext;
import com.littlewin.common.utils.JwtUtils;
import com.littlewin.common.utils.ServletUtils;
import com.littlewin.system.domain.dto.WechatLoginResponse;
import com.littlewin.system.domain.entity.SysUser;
import com.littlewin.system.domain.entity.UserAuth;
import com.littlewin.system.domain.entity.UserInfo;
import com.littlewin.system.mapper.SysUserMapper;
import com.littlewin.system.mapper.UserAuthMapper;
import com.littlewin.system.mapper.UserInfoMapper;
import com.littlewin.system.service.WxUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;

@Service
public class WxUserServiceImpl implements WxUserService {

    @Value("${wx.mp.app-id}")
    private String appId;

    @Value("${wx.mp.secret}")
    private String secret;

    @Value("${wx.mp.api.code2session}")
    private String code2sessionUrl;

    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private UserAuthMapper userAuthMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private RestTemplate restTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String login(String code) {

        // 1. 请求微信 API 获取 openid
        String url = UriComponentsBuilder.fromHttpUrl(code2sessionUrl)
                .queryParam("appid", appId)
                .queryParam("secret", secret)
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .build()
                .toUriString();
        WechatLoginResponse response = restTemplate.getForObject(url, WechatLoginResponse.class);

        if (response == null || response.getOpenid() == null) {
            throw new ServiceException("微信登录失败: " + (response != null ? response.getErrmsg() : "远程调用异常"));
        }

        String openid = response.getOpenid();

        // 2. 查询认证表
        UserAuth auth = userAuthMapper.selectOne(new LambdaQueryWrapper<UserAuth>()
                .eq(UserAuth::getAuthType, "wx_openid")
                .eq(UserAuth::getIdentifier, openid));

        Long userId;
        boolean isNewUser = (auth == null);

        if (isNewUser) {
            // 3. 新用户注册：联动初始化三张表
            // 3.1 插入 sys_user (主表)
            SysUser user = new SysUser();
            user.setNickname("微信用户");
            user.setAvatar(""); // 可设置默认头像
            sysUserMapper.insert(user);
            userId = user.getUserId();

            // 3.2 插入 user_auth (认证表)
            UserAuth newAuth = new UserAuth();
            newAuth.setUserId(userId);
            newAuth.setAuthType("wx_openid");
            newAuth.setIdentifier(openid);
            userAuthMapper.insert(newAuth);

            // 3.3 插入 user_info (详情表)
            UserInfo info = new UserInfo();
            info.setUserId(userId);
            info.setLastLoginIp(ServletUtils.getClientIp());
            info.setLastLoginTime(LocalDateTime.now());
            userInfoMapper.insert(info);
        } else {
            // 4. 老用户：更新最后登录信息
            userId = auth.getUserId();
            UserInfo info = new UserInfo();
            info.setUserId(userId);
            info.setLastLoginIp(ServletUtils.getClientIp());
            info.setLastLoginTime(LocalDateTime.now());
            userInfoMapper.updateById(info);
        }

        LogContext.setDesc(isNewUser ? "微信新用户注册并登录" : "微信用户登录");
        // 设置业务 ID（用户 ID），供 LogAspect 抓取
        LogContext.setBusinessId(userId);
        // 设置用户名标识（OpenID 或 默认名）
        LogContext.setUsername("WX_" + openid.substring(0, 8));

        // 5. 生成 Token
        return JwtUtils.createToken(userId.toString());
    }
}