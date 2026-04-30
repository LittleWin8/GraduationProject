package com.littlewin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.littlewin.common.constants.RedisKeyConstants;
import com.littlewin.common.core.FileUploadVO;
import com.littlewin.common.core.LoginDTO;
import com.littlewin.common.core.Upload;
import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.log.context.LogContext;
import com.littlewin.common.redis.RedisService;
import com.littlewin.common.utils.*;
import com.littlewin.system.domain.dto.WxUserUpdateDTO;
import com.littlewin.system.domain.entity.SysUser;
import com.littlewin.system.domain.entity.UserAuth;
import com.littlewin.system.domain.entity.UserInfo;
import com.littlewin.system.domain.vo.UserInfoVO;
import com.littlewin.system.mapper.SysUserMapper;
import com.littlewin.system.mapper.UserAuthMapper;
import com.littlewin.system.mapper.UserInfoMapper;
import com.littlewin.system.service.WxUserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 微信小程序用户服务实现类
 * 核心功能：
 * 1. 微信登录（openid 注册/登录 + JWT 生成）
 * 2. 用户资料 CRUD
 * 3. 头像本地存储
 *
 * @see FileUploadUtils 实际执行文件存储的工具类
 */
@Service
public class WxUserServiceImpl implements WxUserService {

    @Resource
    private Upload uploadConfig;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private UserAuthMapper userAuthMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private WechatApiUtils wechatApiUtils;

    @Resource
    private RedisService redisService;

    private static final Map<String, Integer> uploadLimitMap = new ConcurrentHashMap<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> login(String code, String nickName, String avatarUrl) {
        WechatApiUtils.WechatSession session = wechatApiUtils.getSessionByCode(code);
        String openid = session.getOpenid();

        UserAuth auth = userAuthMapper.selectOne(new LambdaQueryWrapper<UserAuth>()
                .eq(UserAuth::getAuthType, "wx_openid")
                .eq(UserAuth::getIdentifier, openid));

        Long userId;
        boolean isNewUser = (auth == null);
        Map<String, Object> result = new HashMap<>();
        String normalizedNickName = nickName == null ? "" : nickName.trim();

        if (isNewUser) {
            if (normalizedNickName.isEmpty()) {
                result.put("isNewUser", true);
                result.put("token", null);
                return result;
            }

            SysUser user = new SysUser();
            user.setNickname(normalizedNickName);
            user.setAvatar(avatarUrl != null ? avatarUrl.trim() : "");
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
            userId = auth.getUserId();

            if (!normalizedNickName.isEmpty()) {
                SysUser user = new SysUser();
                user.setUserId(userId);
                user.setNickname(normalizedNickName);
                if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                    user.setAvatar(avatarUrl.trim());
                }
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

        result.put("isNewUser", false);
        result.put("token", JwtUtils.createToken(userId.toString()));
        return result;
    }

    @Override
    public UserInfoVO getUserInfo() {
        Long userId = getCurrentUserId();
        UserInfoVO userInfo = userAuthMapper.selectFullUserInfoById(userId);
        if (userInfo == null) throw new ServiceException("获取详细资料失败");
        return userInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVO updateUserInfo(WxUserUpdateDTO dto) {
        Long userId = getCurrentUserId();
        if (dto == null) throw new ServiceException("请求参数不能为空");

        String nickname = firstNotBlank(dto.getName(), dto.getNickname());
        if (nickname != null) {
            if (nickname.length() > 50) throw new ServiceException("昵称不能超过50个字符");
            SysUser user = new SysUser();
            user.setUserId(userId);
            user.setNickname(nickname);
            sysUserMapper.updateById(user);
        }

        if (dto.getAvatar() != null) {
            if (dto.getAvatar().length() > 255) throw new ServiceException("头像地址过长");
            SysUser user = new SysUser();
            user.setUserId(userId);
            user.setAvatar(dto.getAvatar().trim());
            sysUserMapper.updateById(user);
        }

        UserInfo info = buildUserInfoForUpdate(userId, dto);
        if (hasUserInfoUpdates(info)) {
            ensureUserInfoExists(userId);
            userInfoMapper.updateById(info);
        }

        LogContext.setBusinessId(userId);
        LogContext.setDesc("更新小程序用户个人资料");
        return getUserInfo();
    }

    @Override
    public FileUploadVO uploadAvatar(MultipartFile file) {
        checkUploadLimit();
        return saveAvatarFile(file, "wx");
    }

    private FileUploadVO saveAvatarFile(MultipartFile file, String filePrefix) {
        if (file == null || file.isEmpty()) throw new ServiceException("请选择要上传的头像");
        if (file.getSize() > uploadConfig.getMaxAvatarSize()) {
            throw new ServiceException("头像大小不能超过 " + (uploadConfig.getMaxAvatarSize() / 1024 / 1024) + "MB");
        }

        String originalName = file.getOriginalFilename() == null ? "avatar" : file.getOriginalFilename();
        String suffix = FileUploadUtils.getFileSuffix(originalName).toLowerCase();

        if (!uploadConfig.getAllowedImageSuffixes().contains(suffix)) {
            throw new ServiceException("仅支持 " + String.join("、", uploadConfig.getAllowedImageSuffixes()) + " 图片格式");
        }

        // 简单的文件头校验
        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ServiceException("非法文件类型");
            }
        } catch (Exception e) {
            throw new ServiceException("文件格式异常");
        }

        String relativePath = FileUploadUtils.upload(
                file,
                uploadConfig.getLocalRootPath(),
                uploadConfig.getUserAvatarPath(),
                filePrefix
        );
        String url = "/api/wx/user/files" + relativePath;

        String fileName = relativePath.substring(relativePath.lastIndexOf("/") + 1);
        return new FileUploadVO(url, fileName, originalName, file.getSize());
    }

    private Long getCurrentUserId() {
        LoginDTO authUser = SecurityUtils.getLoginUser();
        if (authUser == null || authUser.getUserId() == null) throw new ServiceException("用户不存在或未登录");
        return authUser.getUserId();
    }

    private UserInfo buildUserInfoForUpdate(Long userId, WxUserUpdateDTO dto) {
        UserInfo info = new UserInfo();
        info.setUserId(userId);

        if (dto.getGender() != null) {
            if (dto.getGender() < 0 || dto.getGender() > 2) throw new ServiceException("性别参数不合法");
            info.setGender(dto.getGender());
        }
        if (dto.getPhone() != null) {
            String phone = dto.getPhone().trim();
            if (phone.length() > 20) throw new ServiceException("手机号不能超过20个字符");
            info.setPhone(phone);
        }
        if (dto.getEmail() != null) {
            String email = dto.getEmail().trim();
            if (email.length() > 100) throw new ServiceException("邮箱不能超过100个字符");
            info.setEmail(email);
        }
        if (dto.getBirthday() != null && !dto.getBirthday().trim().isEmpty()) {
            try {
                info.setBirthday(LocalDate.parse(dto.getBirthday().trim()));
            } catch (Exception e) {
                throw new ServiceException("生日格式应为 yyyy-MM-dd");
            }
        }
        if (dto.getCity() != null) {
            String city = dto.getCity().trim();
            if (city.length() > 50) throw new ServiceException("地区不能超过50个字符");
            info.setCity(city);
        }
        if (dto.getSignature() != null) {
            String signature = dto.getSignature().trim();
            if (signature.length() > 255) throw new ServiceException("个性签名不能超过255个字符");
            info.setSignature(signature);
        }
        return info;
    }

    private boolean hasUserInfoUpdates(UserInfo info) {
        return info.getGender() != null
                || info.getPhone() != null
                || info.getEmail() != null
                || info.getBirthday() != null
                || info.getCity() != null
                || info.getSignature() != null;
    }

    private void ensureUserInfoExists(Long userId) {
        UserInfo existing = userInfoMapper.selectById(userId);
        if (existing != null) return;
        UserInfo info = new UserInfo();
        info.setUserId(userId);
        userInfoMapper.insert(info);
    }

    private String firstNotBlank(String first, String second) {
        if (first != null && !first.trim().isEmpty()) return first.trim();
        if (second != null && !second.trim().isEmpty()) return second.trim();
        return null;
    }

    private void checkUploadLimit() {
        String ip = ServletUtils.getClientIp();
        int count = uploadLimitMap.getOrDefault(ip, 0);
        if (count > 20) { // 同一个IP每天/每小时限制上传20张
            throw new ServiceException("上传过于频繁，请稍后再试");
        }
        uploadLimitMap.put(ip, count + 1);
    }

    @Override
    public void addTokenToBlacklist(String token) {
        String jti = JwtUtils.getTokenId(token);
        long remaining = JwtUtils.getRemainingExpiration(token);
        if (remaining > 0) {
            redisService.set(RedisKeyConstants.TOKEN_BLACKLIST + jti, "1", remaining, TimeUnit.MILLISECONDS);
        }
    }

    @Scheduled(fixedRate = 3600000) // 每小时执行一次（3600000毫秒）
    public void clearUploadLimitMap() {
        uploadLimitMap.clear();
    }
}
