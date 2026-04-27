package com.littlewin.system.service;

import com.littlewin.system.domain.dto.WxUserUpdateDTO;
import com.littlewin.common.core.FileUploadVO;
import com.littlewin.system.domain.vo.UserInfoVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface WxUserService {
    /**
     * 微信用户登录
     * @return 包含 token 和 isNewUser 的键值对
     */
    Map<String, Object> login(String code, String nickName, String avatarUrl);

    /**
     * 微信用户获取个人详细信息
     */
    UserInfoVO getUserInfo();

    /**
     * 更新当前微信用户个人资料
     */
    UserInfoVO updateUserInfo(WxUserUpdateDTO dto);

    /**
     * 上传当前微信用户头像到本地资源目录
     */
    FileUploadVO uploadAvatar(MultipartFile file);
}

